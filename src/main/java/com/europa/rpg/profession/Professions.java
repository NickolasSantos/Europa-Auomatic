package com.europa.rpg.profession;

import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
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
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Ganho de XP e bônus das profissões. */
public final class Professions {
    /** Último valor da estatística de peixes pescados por jogador (não persistido). */
    private static final Map<UUID, Integer> FISH_BASELINE = new HashMap<>();

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
            case SMITHING -> lvl * 0.015;
            default -> lvl * 0.01;
        };
    }

    public static void addXp(ServerPlayerEntity player, Profession p, int amount) {
        if (amount <= 0) return;
        PlayerRpgData d = RpgAttachments.get(player);
        int lvl = level(d, p);
        if (lvl >= Profession.MAX_LEVEL) return;
        int xp = xp(d, p) + amount;
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
            player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.6f);
            if (p == Profession.MINING || p == Profession.FISHING) Progression.applyAttributes(player);
        }
    }

    public static void init() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayerEntity sp && world instanceof ServerWorld sw && !sp.isCreative()) {
                onBlockBroken(sp, sw, pos, state, blockEntity);
            }
        });
    }

    private static void onBlockBroken(ServerPlayerEntity p, ServerWorld w, BlockPos pos, BlockState state, BlockEntity be) {
        PlayerRpgData d = RpgAttachments.get(p);
        ItemStack tool = p.getMainHandStack();
        if (state.isIn(ConventionalBlockTags.ORES)) {
            List<ItemStack> drops = Block.getDroppedStacks(state, w, pos, be, p, tool);
            // Minério retirado com Toque Suave não conta (evita colocar/quebrar repetidamente).
            if (drops.stream().anyMatch(s -> s.isOf(state.getBlock().asItem()))) return;
            addXp(p, Profession.MINING, oreXp(state));
            if (w.random.nextDouble() < perkChance(d, Profession.MINING)) Block.dropStacks(state, w, pos, be, p, tool);
        } else if (state.isIn(BlockTags.LOGS)) {
            addXp(p, Profession.WOODCUTTING, 2);
            if (w.random.nextDouble() < perkChance(d, Profession.WOODCUTTING)) Block.dropStacks(state, w, pos, be, p, tool);
        } else if ((state.getBlock() instanceof CropBlock crop && crop.isMature(state))
                || (state.getBlock() instanceof NetherWartBlock && state.get(NetherWartBlock.AGE) >= 3)) {
            addXp(p, Profession.FARMING, 3);
            if (w.random.nextDouble() < perkChance(d, Profession.FARMING)) Block.dropStacks(state, w, pos, be, p, tool);
        }
    }

    private static int oreXp(BlockState state) {
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        if (path.contains("ancient_debris")) return 30;
        if (path.contains("diamond") || path.contains("emerald")) return 18;
        if (path.contains("adamantite")) return 16;
        if (path.contains("gold") || path.contains("mithril") || path.contains("soulstone")) return 8;
        if (path.contains("lapis") || path.contains("iron")) return 6;
        return 3;
    }

    /** A cada segundo: detecta peixes pescados pela estatística do jogo. */
    public static void tick(ServerPlayerEntity p) {
        int caught = p.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.FISH_CAUGHT));
        Integer before = FISH_BASELINE.put(p.getUuid(), caught);
        if (before != null && caught > before) addXp(p, Profession.FISHING, 12 * (caught - before));
    }

    public static void forget(ServerPlayerEntity p) {
        FISH_BASELINE.remove(p.getUuid());
    }

    /** Chamado ao fabricar um item (mixin em ItemStack#onCraftByPlayer). */
    public static void onCraft(ServerPlayerEntity p, ItemStack stack) {
        var item = stack.getItem();
        if (!(item instanceof ArmorItem || item instanceof ToolItem || item instanceof RangedWeaponItem || item instanceof ShieldItem)) return;
        PlayerRpgData d = RpgAttachments.get(p);
        addXp(p, Profession.SMITHING, 12);
        if (!stack.hasEnchantments() && p.getRandom().nextDouble() < perkChance(d, Profession.SMITHING)) {
            int lvl = Math.min(3, 1 + level(d, Profession.SMITHING) / 20);
            var unbreaking = p.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.UNBREAKING);
            unbreaking.ifPresent(e -> stack.addEnchantment(e, lvl));
            p.sendMessage(Text.translatable("rpg.europa.profession.masterwork").formatted(Formatting.GOLD), true);
        }
    }

    /** Chamado ao retirar uma poção recém-preparada do suporte de poções. */
    public static void onBrewTaken(ServerPlayerEntity p, ItemStack stack) {
        PlayerRpgData d = RpgAttachments.get(p);
        addXp(p, Profession.ALCHEMY, 8);
        if (p.getRandom().nextDouble() < perkChance(d, Profession.ALCHEMY)) {
            ItemStack copy = stack.copy();
            if (!p.getInventory().insertStack(copy)) p.dropItem(copy, false);
            p.sendMessage(Text.translatable("rpg.europa.profession.extra_potion").formatted(Formatting.LIGHT_PURPLE), true);
        }
    }
}
