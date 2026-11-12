package com.europa.rpg.profession;

import com.europa.rpg.economy.Factions;
import com.europa.rpg.item.RarityRoller;
import com.europa.rpg.registry.ModItems;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Ganho de XP, níveis e bônus das 12 profissões. */
public final class Professions {
    /** Últimos valores de estatísticas por jogador (pesca, criação, encantamentos, distância). Não persistido. */
    private static final Map<UUID, Map<String, Integer>> STAT_BASELINE = new HashMap<>();
    private static final Set<String> MECHANISM_WORDS = Set.of("redstone", "piston", "observer", "repeater", "comparator",
            "dispenser", "dropper", "hopper", "rail", "lever", "daylight_detector", "target", "crafter", "note_block",
            "tripwire_hook", "sculk_sensor", "lightning_rod", "tnt", "minecart");

    private Professions() {}

    public static int level(PlayerRpgData d, Profession p) {
        return d.profLevels.getOrDefault(p.id(), 1);
    }

    public static int xp(PlayerRpgData d, Profession p) {
        return d.profXp.getOrDefault(p.id(), 0);
    }

    /** Chance (0..1) do bônus principal da profissão. */
    public static double perkChance(PlayerRpgData d, Profession p) {
        int lvl = level(d, p);
        return switch (p) {
            case ARTIFACT_RESTORER, RUIN_ARCHAEOLOGIST -> lvl * 0.015;
            case ESSENCE_COLLECTOR -> lvl * 0.005;
            case ECHO_WEAVER -> lvl * 0.002;
            default -> lvl * 0.01;
        };
    }

    public static double fishingLuck(PlayerRpgData d) {
        return 0.04 * (level(d, Profession.PHENOMENON_TAMER) - 1);
    }

    public static double travelSpeed(PlayerRpgData d) {
        return 0.0004 * (level(d, Profession.DIMENSIONAL_NAVIGATOR) - 1);
    }

    public static void addXp(ServerPlayerEntity player, Profession p, int amount) {
        if (amount <= 0) return;
        PlayerRpgData d = RpgAttachments.get(player);
        Factions.onProfessionXp(player, p, amount);
        int lvl = level(d, p);
        if (lvl >= Profession.MAX_LEVEL) return;
        int xp = xp(d, p) + amount;
        int oldTier = Profession.tier(lvl);
        boolean up = false;
        while (lvl < Profession.MAX_LEVEL && xp >= Profession.xpForNext(lvl)) {
            xp -= Profession.xpForNext(lvl);
            lvl++;
            up = true;
        }
        d.profLevels.put(p.id(), lvl);
        d.profXp.put(p.id(), lvl >= Profession.MAX_LEVEL ? 0 : xp);
        if (up) {
            player.sendMessage(Text.translatable("rpg.europa.profession.level_up", Text.translatable(p.translationKey()), lvl).formatted(Formatting.AQUA));
            if (Profession.tier(lvl) > oldTier) {
                player.sendMessage(Text.translatable("rpg.europa.profession.tier_up", Text.translatable(p.translationKey()),
                        Text.translatable(Profession.tierKey(lvl))).formatted(Formatting.GOLD, Formatting.BOLD));
            }
            player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.6f);
            Progression.applyAttributes(player);
        }
    }

    public static void init() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayerEntity sp && world instanceof ServerWorld sw && !sp.isCreative()) {
                onBlockBroken(sp, sw, pos, state, blockEntity);
            }
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                addXp(player, Profession.DIMENSIONAL_NAVIGATOR, 40));
    }

    private static void onBlockBroken(ServerPlayerEntity p, ServerWorld w, BlockPos pos, BlockState state, BlockEntity be) {
        PlayerRpgData d = RpgAttachments.get(p);
        ItemStack tool = p.getMainHandStack();
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        if (state.isIn(ConventionalBlockTags.ORES)) {
            List<ItemStack> drops = Block.getDroppedStacks(state, w, pos, be, p, tool);
            // Minério retirado com Toque Suave não conta (evita colocar/quebrar repetidamente).
            if (drops.stream().anyMatch(s -> s.isOf(state.getBlock().asItem()))) return;
            addXp(p, Profession.ANOMALY_GEOLOGIST, oreXp(path));
            if (w.random.nextDouble() < perkChance(d, Profession.ANOMALY_GEOLOGIST)) Block.dropStacks(state, w, pos, be, p, tool);
        } else if (state.isIn(BlockTags.LOGS)) {
            addXp(p, Profession.BIOME_GARDENER, 2);
            if (w.random.nextDouble() < perkChance(d, Profession.BIOME_GARDENER)) Block.dropStacks(state, w, pos, be, p, tool);
        } else if ((state.getBlock() instanceof CropBlock crop && crop.isMature(state))
                || (state.getBlock() instanceof NetherWartBlock && state.get(NetherWartBlock.AGE) >= 3)) {
            addXp(p, Profession.BIOME_GARDENER, 3);
            if (w.random.nextDouble() < perkChance(d, Profession.BIOME_GARDENER)) Block.dropStacks(state, w, pos, be, p, tool);
        } else if (path.contains("sculk") || path.contains("amethyst_cluster") || path.equals("amethyst_block")) {
            List<ItemStack> drops = Block.getDroppedStacks(state, w, pos, be, p, tool);
            if (drops.stream().anyMatch(s -> s.isOf(state.getBlock().asItem()))) return;
            addXp(p, Profession.ECHO_WEAVER, path.contains("catalyst") || path.contains("shrieker") ? 20 : 3);
            if (w.random.nextDouble() < perkChance(d, Profession.ECHO_WEAVER)) Block.dropStack(w, pos, new ItemStack(Items.ECHO_SHARD));
        }
    }

    private static int oreXp(String path) {
        if (path.contains("ancient_debris")) return 30;
        if (path.contains("diamond") || path.contains("emerald")) return 18;
        if (path.contains("adamantite")) return 16;
        if (path.contains("gold") || path.contains("mithril") || path.contains("soulstone")) return 8;
        if (path.contains("lapis") || path.contains("iron")) return 6;
        return 3;
    }

    /** Coletor de Essências: abates de criaturas hostis. */
    public static void onKill(ServerPlayerEntity p, LivingEntity killed) {
        if (!(killed instanceof Monster)) return;
        addXp(p, Profession.ESSENCE_COLLECTOR, Math.max(2, Math.round(killed.getMaxHealth() / 4f)));
        if (p.getRandom().nextDouble() < perkChance(RpgAttachments.get(p), Profession.ESSENCE_COLLECTOR)) {
            killed.dropStack(new ItemStack(ModItems.ASTER_FRAGMENT, 1 + p.getRandom().nextInt(2)));
        }
    }

    /** Arqueólogo de Ruínas: primeira abertura de baú com loot de estrutura. */
    public static void onLootGenerated(ServerPlayerEntity p, Identifier lootTable) {
        addXp(p, Profession.RUIN_ARCHAEOLOGIST, 20);
        Factions.addRep(p, "wanderers", 15);
        if (p.getRandom().nextDouble() < perkChance(RpgAttachments.get(p), Profession.RUIN_ARCHAEOLOGIST)) {
            ItemStack bonus = new ItemStack(ModItems.ASTER_FRAGMENT, 2 + p.getRandom().nextInt(5));
            if (!p.getInventory().insertStack(bonus)) p.dropItem(bonus, false);
            p.sendMessage(Text.translatable("rpg.europa.profession.hidden_cache").formatted(Formatting.GOLD), true);
        }
    }

    /** A cada segundo: detecta pesca, criação de animais, encantamentos e distância percorrida pelas estatísticas. */
    public static void tick(ServerPlayerEntity p) {
        PlayerRpgData d = RpgAttachments.get(p);
        int fish = delta(p, "fish", Stats.CUSTOM.getOrCreateStat(Stats.FISH_CAUGHT));
        int bred = delta(p, "bred", Stats.CUSTOM.getOrCreateStat(Stats.ANIMALS_BRED));
        if (fish + bred > 0) addXp(p, Profession.PHENOMENON_TAMER, 12 * fish + 10 * bred);
        int enchants = delta(p, "enchant", Stats.CUSTOM.getOrCreateStat(Stats.ENCHANT_ITEM));
        if (enchants > 0) {
            addXp(p, Profession.RELIC_CARTOMANCER, 20 * enchants);
            if (p.getRandom().nextDouble() < perkChance(d, Profession.RELIC_CARTOMANCER)) {
                ItemStack lapis = new ItemStack(Items.LAPIS_LAZULI, 1 + p.getRandom().nextInt(3));
                if (!p.getInventory().insertStack(lapis)) p.dropItem(lapis, false);
                p.sendMessage(Text.translatable("rpg.europa.profession.lapis_refund").formatted(Formatting.BLUE), true);
            }
        }
        int cm = 0;
        for (var stat : new net.minecraft.util.Identifier[]{Stats.WALK_ONE_CM, Stats.SPRINT_ONE_CM, Stats.SWIM_ONE_CM,
                Stats.BOAT_ONE_CM, Stats.HORSE_ONE_CM, Stats.AVIATE_ONE_CM}) {
            cm += p.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(stat));
        }
        Map<String, Integer> base = STAT_BASELINE.computeIfAbsent(p.getUuid(), k -> new HashMap<>());
        Integer lastTravel = base.get("travel");
        if (lastTravel == null) base.put("travel", cm);
        else if (cm - lastTravel >= 50_000) { // a cada 500 blocos
            base.put("travel", cm);
            addXp(p, Profession.DIMENSIONAL_NAVIGATOR, 6);
        }
    }

    private static int delta(ServerPlayerEntity p, String key, Stat<Identifier> stat) {
        int now = p.getStatHandler().getStat(stat);
        Integer before = STAT_BASELINE.computeIfAbsent(p.getUuid(), k -> new HashMap<>()).put(key, now);
        return before == null ? 0 : Math.max(0, now - before);
    }

    public static void forget(ServerPlayerEntity p) {
        STAT_BASELINE.remove(p.getUuid());
    }

    /** Chamado ao fabricar/fundir um item (mixin em ItemStack#onCraftByPlayer). */
    public static void onCraft(ServerPlayerEntity p, ItemStack stack) {
        var item = stack.getItem();
        PlayerRpgData d = RpgAttachments.get(p);
        String path = Registries.ITEM.getId(item).getPath();
        if (item instanceof ArmorItem || item instanceof ToolItem || item instanceof RangedWeaponItem || item instanceof ShieldItem) {
            addXp(p, Profession.ARTIFACT_RESTORER, 12);
            if (!stack.hasEnchantments() && p.getRandom().nextDouble() < perkChance(d, Profession.ARTIFACT_RESTORER)) {
                int lvl = Math.min(3, 1 + level(d, Profession.ARTIFACT_RESTORER) / 20);
                p.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.UNBREAKING).ifPresent(e -> stack.addEnchantment(e, lvl));
                RarityRoller.roll(stack, p.getRandom(), level(d, Profession.ARTIFACT_RESTORER) >= 30 ? 2 : 1);
                p.sendMessage(Text.translatable("rpg.europa.profession.masterwork").formatted(Formatting.GOLD), true);
            }
        } else if (MECHANISM_WORDS.stream().anyMatch(path::contains)) {
            addXp(p, Profession.AUTOMATON_ENGINEER, 6);
            if (p.getRandom().nextDouble() < perkChance(d, Profession.AUTOMATON_ENGINEER)) giveCopy(p, stack, 1);
        } else if (stack.contains(DataComponentTypes.FOOD)) {
            addXp(p, Profession.METAMORPH_COOK, 3);
            if (p.getRandom().nextDouble() < perkChance(d, Profession.METAMORPH_COOK)) giveCopy(p, stack, 1);
        }
    }

    /** Reparo/combinação na bigorna. */
    public static void onAnvil(ServerPlayerEntity p, ItemStack result) {
        addXp(p, Profession.ARTIFACT_RESTORER, 15);
    }

    private static void giveCopy(ServerPlayerEntity p, ItemStack stack, int count) {
        ItemStack copy = stack.copyWithCount(count);
        if (!p.getInventory().insertStack(copy)) p.dropItem(copy, false);
    }

    /** Destilador de Memórias: retirar uma poção recém-preparada. */
    public static void onBrewTaken(ServerPlayerEntity p, ItemStack stack) {
        PlayerRpgData d = RpgAttachments.get(p);
        addXp(p, Profession.MEMORY_DISTILLER, 8);
        if (p.getRandom().nextDouble() < perkChance(d, Profession.MEMORY_DISTILLER)) {
            giveCopy(p, stack, stack.getCount());
            p.sendMessage(Text.translatable("rpg.europa.profession.extra_potion").formatted(Formatting.LIGHT_PURPLE), true);
        }
    }
}
