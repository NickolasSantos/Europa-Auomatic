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
        ctx.assertTrue(d.rpgClass == RpgClass.WARRIOR, "classe deveria ser guerreiro, é " + d.rpgClass);
        ctx.assertTrue(p.getMaxHealth() == 24f, "vida máxima esperada 24, obtida " + p.getMaxHealth());

        // Não pode trocar de classe sem o tomo
        run(p, "europa class mage");
        ctx.assertTrue(d.rpgClass == RpgClass.WARRIOR, "troca de classe sem tomo deveria falhar");

        // Subclasse bloqueada antes do nível 10
        run(p, "europa subclass berserker");
        ctx.assertTrue(d.subclass == RpgSubclass.NONE, "subclasse não deveria ser liberada no nível 1");

        Progression.addXp(p, Progression.xpForNextLevel(1));
        ctx.assertTrue(d.level == 2 && d.xp == 0, "esperado nível 2 / xp 0, obtido " + d.level + "/" + d.xp);

        run(p, "europa admin setlevel @s 10");
        ctx.assertTrue(d.level == 10, "setlevel falhou");
        run(p, "europa subclass mage_is_not_valid");
        run(p, "europa subclass pyromancer");
        ctx.assertTrue(d.subclass == RpgSubclass.NONE, "subclasse de outra classe deveria falhar");
        run(p, "europa subclass berserker");
        ctx.assertTrue(d.subclass == RpgSubclass.BERSERKER, "subclasse deveria ser berserker");

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
        ctx.assertTrue(RpgClass.all().size() == 12 && RpgSubclass.all().size() == 24, "esperadas 12 classes e 24 subclasses");
        for (RpgSubclass sub : RpgSubclass.all()) {
            ctx.getWorld().getEntitiesByClass(ZombieEntity.class, p.getBoundingBox().expand(60), e -> true).forEach(e -> e.discard());
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
        ctx.assertTrue(Quests.ALL.size() == 10, "deveriam existir 10 missões");
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
        ctx.assertTrue(com.europa.rpg.blessing.Blessings.pledge(p, "kaelith"), "troca de patrono");
        ctx.assertTrue(d.favor == 0 && "kaelith".equals(d.patron), "troca deveria zerar o favor");
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void professionsAndSync(TestContext ctx) {
        ServerPlayerEntity p = player(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        var mining = com.europa.rpg.profession.Profession.MINING;
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
        ctx.assertTrue(back.rpgClass == RpgClass.BARD && back.talents.get("flow") == 3 && back.favor == 321
                && "ysmera".equals(back.patron) && back.profLevels.get("mining") == 3, "codec não preservou o estado");
        ctx.complete();
    }
}
