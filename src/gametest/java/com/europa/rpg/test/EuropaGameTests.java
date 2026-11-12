package com.europa.rpg.test;

import com.europa.rpg.ability.AbilityHandler;
import com.europa.rpg.entity.CatacombLordEntity;
import com.europa.rpg.quest.QuestManager;
import com.europa.rpg.quest.Quests;
import com.europa.rpg.registry.ModBlocks;
import com.europa.rpg.registry.ModEntities;
import com.europa.rpg.registry.ModItems;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.rpg.RpgSubclass;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EuropaGameTests implements FabricGameTest {

    private static ServerPlayerEntity player(TestContext ctx) {
        ServerPlayerEntity p = ctx.createMockCreativeServerPlayerInWorld();
        Vec3d pos = ctx.getAbsolute(new Vec3d(1.5, 1, 1.5));
        p.refreshPositionAndAngles(pos.x, pos.y, pos.z, -45f, 0f);
        p.setAttached(RpgAttachments.PLAYER_DATA, new PlayerRpgData());
        return p;
    }

    private static int run(ServerPlayerEntity p, String command) {
        p.getServer().getCommandManager().executeWithPrefix(p.getCommandSource().withLevel(4), command);
        return 0;
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void classChoiceAndLeveling(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        run(p, "europa class warrior");
        PlayerRpgData d = RpgAttachments.get(p);
        ctx.assertTrue(d.rpgClass == RpgClass.byId("warrior"), "classe deveria ser guerreiro, é " + d.rpgClass);
        ctx.assertTrue(p.getMaxHealth() == 24f, "vida máxima esperada 24, obtida " + p.getMaxHealth());

        // Não pode trocar de classe sem o tomo
        run(p, "europa class mage");
        ctx.assertTrue(d.rpgClass == RpgClass.byId("warrior"), "troca de classe sem tomo deveria falhar");

        // Subclasse bloqueada antes do nível 10
        run(p, "europa subclass guardian");
        ctx.assertTrue(d.subclass == RpgSubclass.NONE, "subclasse não deveria ser liberada no nível 1");

        Progression.addXp(p, Progression.xpForNextLevel(1));
        ctx.assertTrue(d.level == 2 && d.xp == 0, "esperado nível 2 / xp 0, obtido " + d.level + "/" + d.xp);

        run(p, "europa admin setlevel @s 10");
        ctx.assertTrue(d.level == 10, "setlevel falhou");
        run(p, "europa subclass mage_is_not_valid");
        run(p, "europa subclass elementalist");
        ctx.assertTrue(d.subclass == RpgSubclass.NONE, "subclasse de outra classe deveria falhar");
        run(p, "europa subclass guardian");
        ctx.assertTrue(d.subclass == RpgSubclass.byId("guardian"), "subclasse deveria ser guardião");

        // Tomo do renascimento
        p.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.REBIRTH_TOME));
        ModItems.REBIRTH_TOME.use(p.getWorld(), p, Hand.MAIN_HAND);
        ctx.assertTrue(d.rpgClass == RpgClass.NONE && d.subclass == RpgSubclass.NONE && d.level == 10, "tomo deveria limpar classe e manter nível");
        ctx.assertTrue(p.getMaxHealth() == 20f, "atributos deveriam voltar ao padrão, vida = " + p.getMaxHealth());
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
    public void allAbilitiesCast(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        d.level = 20;
        ctx.assertTrue(RpgClass.all().size() == 32 && RpgSubclass.all().size() == 96, "esperadas 32 classes e 96 subclasses");
        for (RpgSubclass sub : RpgSubclass.all()) {
            ctx.getWorld().getEntitiesByClass(ZombieEntity.class, p.getBoundingBox().expand(60), e -> true).forEach(e -> e.discard());
            ctx.getWorld().getEntitiesByClass(net.minecraft.entity.mob.MobEntity.class, p.getBoundingBox().expand(60),
                    e -> e.getCommandTags().contains(com.europa.rpg.ability.Summons.TAG)).forEach(e -> e.discard());
            ZombieEntity zombie = ctx.spawnMob(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
            ctx.spawnMob(EntityType.ZOMBIE, new BlockPos(5, 1, 3)).setAiDisabled(true);
            zombie.setAiDisabled(true);
            p.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, zombie.getEyePos());
            d.rpgClass = sub.parent;
            d.subclass = sub;
            d.cooldowns.clear();
            d.mana = Progression.maxMana(d);
            Progression.applyAttributes(p);
            long now = p.getServer().getOverworld().getTime();
            AbilityHandler.tryCast(p, AbilityHandler.PRIMARY);
            ctx.assertTrue(d.cooldownRemaining("primary", now) > 0, "habilidade primária não executou para " + sub.parent);
            // Ladino: o passo sombrio teleporta; reposiciona antes da suprema
            p.refreshPositionAndAngles(ctx.getAbsolute(new Vec3d(1.5, 1, 1.5)).x, ctx.getAbsolute(new Vec3d(1.5, 1, 1.5)).y,
                    ctx.getAbsolute(new Vec3d(1.5, 1, 1.5)).z, -45f, 0f);
            if (zombie.isAlive()) p.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, zombie.getEyePos());
            else ctx.spawnMob(EntityType.ZOMBIE, new BlockPos(4, 1, 4)).setAiDisabled(true);
            AbilityHandler.tryCast(p, AbilityHandler.ULTIMATE);
            ctx.assertTrue(d.cooldownRemaining("ultimate", now) > 0, "suprema não executou para " + sub);
            // Cooldown deve bloquear segundo uso
            long before = d.cooldowns.get("primary");
            AbilityHandler.tryCast(p, AbilityHandler.PRIMARY);
            ctx.assertTrue(d.cooldowns.get("primary") == before, "cooldown não respeitado para " + sub.parent);
        }
        // Deixa efeitos atrasados (meteoro, investida) executarem sem exceção
        ctx.runAtTick(60, ctx::complete);
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void killAndCollectQuests(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        run(p, "europa class ranger");

        ctx.assertTrue(QuestManager.accept(p, "undead_patrol"), "aceitar missão falhou");
        ctx.assertFalse(QuestManager.accept(p, "deep_forge"), "missão bloqueada por pré-requisito foi aceita");
        ZombieEntity z = ctx.spawnMob(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
        for (int i = 0; i < 10; i++) QuestManager.onKill(p, z);
        ctx.assertTrue(d.completedQuests.contains("undead_patrol"), "missão de abate não concluiu");
        ctx.assertTrue(p.getInventory().count(net.minecraft.item.Items.IRON_INGOT) >= 4, "recompensa não entregue");
        ctx.assertTrue(d.level >= 2, "XP da missão deveria subir de nível, nível = " + d.level);

        d.level = 4;
        ctx.assertTrue(QuestManager.accept(p, "mithril_prospector"), "aceitar missão de coleta falhou");
        p.getInventory().insertStack(new ItemStack(ModItems.RAW_MITHRIL, 5));
        QuestManager.turnIn(p, "mithril_prospector");
        ctx.assertTrue(d.activeQuests.get("mithril_prospector") == 5, "entrega parcial deveria registrar 5");
        p.getInventory().insertStack(new ItemStack(ModItems.RAW_MITHRIL, 10));
        QuestManager.turnIn(p, "mithril_prospector");
        ctx.assertTrue(d.completedQuests.contains("mithril_prospector"), "missão de coleta não concluiu");
        ctx.assertTrue(p.getInventory().count(ModItems.RAW_MITHRIL) == 7, "deveria consumir só 3 restantes, sobrou " + p.getInventory().count(ModItems.RAW_MITHRIL));
        ctx.assertTrue(p.getInventory().count(ModItems.MITHRIL_PICKAXE) == 1, "picareta de recompensa ausente");
        ctx.assertTrue(Quests.ALL.size() == 18, "deveriam existir 18 missões (10 + 8 contratos)");
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void altarSummonsBoss(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        BlockPos altar = new BlockPos(3, 1, 3);
        ctx.setBlockState(altar, ModBlocks.BOSS_ALTAR.getDefaultState());
        p.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.CATACOMB_KEY));
        ctx.useBlock(altar, p);
        ctx.expectEntity(ModEntities.CATACOMB_LORD);
        CatacombLordEntity boss = ctx.getWorld().getEntitiesByType(ModEntities.CATACOMB_LORD, e -> true).getFirst();
        ctx.assertTrue(boss.getMaxHealth() == 350f, "vida do chefe deveria ser 350");
        // Segundo uso com o chefe vivo não deve invocar outro
        ctx.useBlock(altar, p);
        ctx.assertTrue(ctx.getWorld().getEntitiesByType(ModEntities.CATACOMB_LORD, e -> true).size() == 1, "invocou chefe duplicado");
        boss.discard();
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void talentTreeRules(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        ctx.assertFalse(com.europa.rpg.rpg.RpgActions.unlockTalent(p, "vigor"), "talento sem classe deveria falhar");
        run(p, "europa class monk");
        ctx.assertFalse(com.europa.rpg.rpg.RpgActions.unlockTalent(p, "vigor"), "talento sem pontos deveria falhar (nível 1)");
        d.level = 20; // 19 pontos
        com.europa.rpg.rpg.Progression.applyAttributes(p);
        ctx.assertFalse(com.europa.rpg.rpg.RpgActions.unlockTalent(p, "iron_skin"), "tier 2 sem tier 1 deveria falhar");
        float before = p.getMaxHealth();
        for (int i = 0; i < 5; i++) ctx.assertTrue(com.europa.rpg.rpg.RpgActions.unlockTalent(p, "vigor"), "vigor rank " + (i + 1));
        ctx.assertFalse(com.europa.rpg.rpg.RpgActions.unlockTalent(p, "vigor"), "rank acima do máximo");
        ctx.assertTrue(p.getMaxHealth() == before + 10, "vigor 5 deveria dar +10 de vida: " + before + " -> " + p.getMaxHealth());
        ctx.assertTrue(com.europa.rpg.rpg.RpgActions.unlockTalent(p, "iron_skin"), "tier 2 após tier 1");
        com.europa.rpg.rpg.RpgActions.unlockTalent(p, "steadfast");
        com.europa.rpg.rpg.RpgActions.unlockTalent(p, "second_wind");
        ctx.assertTrue(com.europa.rpg.talent.Talents.pointsInBranch(d, com.europa.rpg.talent.TalentNode.Branch.DEFENSE) == 8, "8 pontos em defesa");
        ctx.assertFalse(com.europa.rpg.rpg.RpgActions.unlockTalent(p, "last_stand"), "capstone exige 10 pontos no ramo");
        com.europa.rpg.rpg.RpgActions.unlockTalent(p, "iron_skin");
        com.europa.rpg.rpg.RpgActions.unlockTalent(p, "iron_skin");
        ctx.assertTrue(com.europa.rpg.rpg.RpgActions.unlockTalent(p, "last_stand"), "capstone com 10 pontos");
        ctx.assertTrue(com.europa.rpg.talent.Talents.availablePoints(d) == 19 - 11, "pontos restantes");

        // Celeridade reduz recarga efetiva
        d.talents.put("celerity", 5);
        var def = com.europa.rpg.ability.Abilities.get(d.rpgClass.primaryAbility);
        ctx.assertTrue(com.europa.rpg.ability.AbilityHandler.effectiveCooldown(d, def) < def.cooldown(), "celeridade deveria reduzir a recarga");

        // Última Resistência impede a morte uma vez
        p.setHealth(1f);
        boolean died = net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH.invoker()
                .allowDeath(p, p.getDamageSources().generic(), 100f);
        ctx.assertFalse(died, "Última Resistência deveria impedir a morte");
        ctx.assertTrue(p.getHealth() == 4f, "vida após Última Resistência deveria ser 4");
        ctx.assertTrue(net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH.invoker()
                .allowDeath(p, p.getDamageSources().generic(), 100f), "segunda morte dentro da recarga deveria ser permitida");
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void blessingsAndOfferings(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        ctx.assertFalse(com.europa.rpg.blessing.Blessings.pledge(p, "thalor"), "jurar longe do santuário deveria falhar");
        ctx.setBlockState(new BlockPos(2, 1, 2), ModBlocks.SHRINE.getDefaultState());
        ctx.assertTrue(com.europa.rpg.blessing.Blessings.pledge(p, "thalor"), "jurar perto do santuário");
        ctx.assertTrue(com.europa.rpg.blessing.Blessings.tierOf(d) == 1, "tier inicial 1");
        float armorBefore = (float) p.getArmor();
        ctx.assertFalse(com.europa.rpg.blessing.Blessings.offer(p, new ItemStack(net.minecraft.item.Items.WHEAT), false), "oferenda errada deveria ser rejeitada");
        ItemStack diamonds = new ItemStack(net.minecraft.item.Items.DIAMOND, 10);
        ctx.assertTrue(com.europa.rpg.blessing.Blessings.offer(p, diamonds, true), "oferenda de diamantes");
        ctx.assertTrue(d.favor == 150, "favor esperado 150, obtido " + d.favor);
        ctx.assertTrue(com.europa.rpg.blessing.Blessings.tierOf(d) == 2, "tier 2 com 150 de favor");
        ctx.assertTrue(p.getArmor() >= armorBefore + 2, "tier 2 de Thalor deveria dar +2 de armadura");
        com.europa.rpg.blessing.Blessings.tick(p, d, 20);
        ctx.assertTrue(p.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.HASTE), "Thalor tier 1 deveria dar Pressa");
        // Trocar de patrono zera o favor
        ctx.assertTrue(com.europa.rpg.blessing.Blessings.pledge(p, "tharion"), "troca de patrono");
        ctx.assertTrue(d.favor == 0 && "tharion".equals(d.patron), "troca deveria zerar o favor");
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void professionsAndSync(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        var mining = com.europa.rpg.profession.Profession.ANOMALY_GEOLOGIST;
        com.europa.rpg.profession.Professions.addXp(p, mining, com.europa.rpg.profession.Profession.xpForNext(1) + com.europa.rpg.profession.Profession.xpForNext(2));
        ctx.assertTrue(com.europa.rpg.profession.Professions.level(d, mining) == 3, "mineração deveria estar no nível 3");

        // Poção marcada pelo suporte de poções rende XP de alquimia apenas uma vez
        ItemStack potion = net.minecraft.component.type.PotionContentsComponent.createStack(net.minecraft.item.Items.POTION, net.minecraft.potion.Potions.SWIFTNESS);
        com.europa.rpg.profession.BrewMarker.mark(potion);
        ctx.assertTrue(com.europa.rpg.profession.BrewMarker.consume(potion), "marca deveria existir");
        ctx.assertFalse(com.europa.rpg.profession.BrewMarker.consume(potion), "marca deveria ser consumida");

        // Ida e volta do codec usado na sincronização completa
        run(p, "europa class bard");
        d.talents.put("flow", 3);
        d.patron = "ysmera";
        d.favor = 321;
        var nbt = PlayerRpgData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, d).getOrThrow();
        PlayerRpgData back = PlayerRpgData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, nbt).getOrThrow();
        ctx.assertTrue(back.rpgClass == RpgClass.byId("bard") && back.talents.get("flow") == 3 && back.favor == 321
                && "ysmera".equals(back.patron) && back.profLevels.get("anomaly_geologist") == 3, "codec não preservou o estado");
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 100)
    public void racesAndResources(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        ctx.assertTrue(com.europa.rpg.race.Race.all().size() == 18, "esperadas 18 raças");
        float hp = p.getMaxHealth();
        ctx.assertTrue(com.europa.rpg.rpg.RpgActions.chooseRace(p, "dwarf"), "escolher anão");
        ctx.assertFalse(com.europa.rpg.rpg.RpgActions.chooseRace(p, "elf"), "segunda raça deveria falhar");
        ctx.assertTrue(Math.abs(p.getScale() - 0.85f) < 0.01f, "escala do anão deveria ser 0.85, é " + p.getScale());
        ctx.assertTrue(p.getMaxHealth() == hp + 4, "anão deveria ter +4 de vida");

        // Todas as habilidades raciais executam
        for (com.europa.rpg.race.Race r : com.europa.rpg.race.Race.all()) {
            d.race = r.id();
            d.cooldowns.clear();
            long now = p.getServer().getOverworld().getTime();
            com.europa.rpg.ability.AbilityHandler.tryCast(p, com.europa.rpg.ability.AbilityHandler.RACIAL);
            ctx.assertTrue(d.cooldownRemaining("racial", now) > 0, "habilidade racial não executou: " + r.id());
        }

        // Recursos por classe
        run(p, "europa class barbarian");
        ctx.assertTrue(com.europa.rpg.rpg.Progression.resource(d) == com.europa.rpg.rpg.Resource.FURY, "bárbaro usa Fúria");
        ctx.assertTrue(d.mana == 0, "Fúria começa vazia");
        com.europa.rpg.rpg.Progression.gain(p, com.europa.rpg.rpg.Resource.FURY, 6f);
        ctx.assertTrue(d.mana == 6f, "Fúria deveria subir para 6");
        p.setAttached(RpgAttachments.PLAYER_DATA, new PlayerRpgData());
        d = RpgAttachments.get(p);
        run(p, "europa class rogue");
        var step = com.europa.rpg.ability.Abilities.get("shadow_step");
        ctx.assertTrue(com.europa.rpg.rpg.Progression.maxMana(d) == 5f, "Oportunidade tem 5 pontos");
        ctx.assertTrue(com.europa.rpg.ability.AbilityHandler.effectiveCost(d, step) == 2, "Passo Sombrio custa 2 de Oportunidade");

        // Catálogo traduzido no servidor (en_us carregado pelo Fabric)
        var lang = net.minecraft.util.Language.getInstance();
        for (RpgClass c : RpgClass.all()) ctx.assertTrue(lang.hasTranslation(c.translationKey()), "sem tradução: " + c.id);
        for (RpgSubclass sc : RpgSubclass.all()) ctx.assertTrue(lang.hasTranslation(sc.translationKey()), "sem tradução: " + sc.id);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void rarityEconomyAndFactions(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        var random = p.getRandom();

        ItemStack sword = new ItemStack(net.minecraft.item.Items.DIAMOND_SWORD);
        com.europa.rpg.item.RarityRoller.apply(sword, com.europa.rpg.item.Rarity.LEGENDARY, random);
        ctx.assertTrue(com.europa.rpg.item.RarityRoller.rarityOf(sword) == com.europa.rpg.item.Rarity.LEGENDARY, "raridade gravada");
        var mods = sword.get(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS);
        long base = mods.modifiers().stream().filter(m -> m.modifier().id().getNamespace().equals("minecraft")).count();
        ctx.assertTrue(base >= 2, "modificadores base da espada deveriam ser preservados");
        ItemStack again = sword.copy();
        com.europa.rpg.item.RarityRoller.roll(again, random, 0);
        ctx.assertTrue(com.europa.rpg.item.RarityRoller.rarityOf(again) == com.europa.rpg.item.Rarity.LEGENDARY, "não deveria re-sortear");

        // Mercado: preço cai com a oferta (estado persiste entre execuções; isola o teste)
        com.europa.rpg.economy.MarketState.get(p.getServer()).pressure.clear();
        ItemStack diamonds = new ItemStack(net.minecraft.item.Items.DIAMOND, 10);
        int first = com.europa.rpg.economy.Market.sell(p, diamonds, true);
        ctx.assertTrue(first >= 70 && diamonds.isEmpty(), "venda de 10 diamantes deveria render ~80, rendeu " + first);
        int second = com.europa.rpg.economy.Market.sell(p, new ItemStack(net.minecraft.item.Items.DIAMOND, 10), true);
        ctx.assertTrue(second < first, "segunda venda deveria valer menos (" + second + " >= " + first + ")");
        int balance = com.europa.rpg.economy.Market.balance(p);
        ctx.assertTrue(balance == first + second, "saldo deveria somar as vendas");
        ctx.assertTrue(com.europa.rpg.economy.Market.buy(p, "bread"), "comprar pão");
        ctx.assertTrue(com.europa.rpg.economy.Market.balance(p) == balance - 8, "compra deveria debitar 8 com troco correto");

        // Facções: loja travada por reputação
        ctx.assertFalse(com.europa.rpg.economy.Market.buy(p, "wan_key"), "chave exige Honrado");
        com.europa.rpg.economy.Factions.addRep(p, "wanderers", 1000);
        ctx.assertTrue(com.europa.rpg.economy.Factions.rank(com.europa.rpg.economy.Factions.rep(d, "wanderers")) == 2, "1000 = Honrado");
        com.europa.rpg.economy.Market.give(p, 200);
        ctx.assertTrue(com.europa.rpg.economy.Market.buy(p, "wan_key"), "comprar chave com Honrado");
        ctx.assertTrue(p.getInventory().count(ModItems.CATACOMB_KEY) == 1, "chave entregue");

        // XP de profissão rende reputação
        int obs = com.europa.rpg.economy.Factions.rep(d, "observatory");
        com.europa.rpg.profession.Professions.addXp(p, com.europa.rpg.profession.Profession.RUIN_ARCHAEOLOGIST, 100);
        ctx.assertTrue(com.europa.rpg.economy.Factions.rep(d, "observatory") >= obs + 20, "arqueologia deveria render reputação no Observatório");
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void legacyDataMigration(TestContext ctx) {
        var nbt = new net.minecraft.nbt.NbtCompound();
        nbt.putString("class", "warrior");
        nbt.putString("patron", "solenne");
        var prof = new net.minecraft.nbt.NbtCompound();
        prof.putInt("mining", 7);
        prof.putInt("woodcutting", 3);
        prof.putInt("farming", 9);
        nbt.put("prof_levels", prof);
        PlayerRpgData d = PlayerRpgData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, nbt).getOrThrow();
        ctx.assertTrue("aureon".equals(d.patron), "solenne deveria migrar para aureon");
        ctx.assertTrue(d.profLevels.get("anomaly_geologist") == 7, "mining -> anomaly_geologist");
        ctx.assertTrue(d.profLevels.get("biome_gardener") == 9, "woodcutting/farming -> biome_gardener (maior nível)");
        var old = new net.minecraft.nbt.NbtCompound();
        old.putString("class", "runesmith");
        ctx.assertFalse(PlayerRpgData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, old).getOrThrow().hasClass(), "classe removida vira 'sem classe'");
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void gearEvolution(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        ItemStack sword = new ItemStack(ModItems.ADAMANTITE_SWORD);
        ctx.assertTrue(com.europa.rpg.item.GearProgress.visualStage(sword) == 0, "fase inicial base");
        com.europa.rpg.item.GearProgress.addXp(p, sword, 160);
        ctx.assertTrue(com.europa.rpg.item.GearProgress.level(sword) == 3, "160 XP = nível 3, obtido " + com.europa.rpg.item.GearProgress.level(sword));
        ctx.assertTrue(com.europa.rpg.item.RarityRoller.rarityOf(sword) == com.europa.rpg.item.Rarity.UNCOMMON, "nível 3 sobe raridade");
        ctx.assertTrue(com.europa.rpg.item.GearProgress.visualStage(sword) == 1, "nível 3 = Desperto");
        com.europa.rpg.item.GearProgress.addXp(p, sword, 5000);
        ctx.assertTrue(com.europa.rpg.item.GearProgress.level(sword) == 5, "nível máximo 5");
        ctx.assertTrue(com.europa.rpg.item.RarityRoller.rarityOf(sword) == com.europa.rpg.item.Rarity.RARE, "nível 5 sobe outra raridade");
        ctx.assertTrue(com.europa.rpg.item.GearProgress.visualStage(sword) == 2, "nível 5 = Ascendido");
        var nbt = sword.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA).copyNbt().getCompound("europa_gear");
        ctx.assertTrue(nbt.getList("affixes", 8).size() == 4, "4 subidas de nível = 4 afixos");
        com.europa.rpg.item.GearProgress.addXp(p, new ItemStack(net.minecraft.item.Items.BREAD), 100); // não-equipamento ignorado
        ctx.complete();
    }
}
