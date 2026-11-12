package com.europa.rpg.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Evolução de equipamentos pelo uso. Armas ganham XP ao matar; armaduras ao receber dano.
 * Nível 1..5: cada nível adiciona um afixo; os níveis 3 e 5 sobem um tier de raridade (até Mítico).
 * A fase visual (base, Desperto, Ascendido) depende do maior entre nível e raridade.
 */
public final class GearProgress {
    public static final int MAX_LEVEL = 5;
    /** XP total necessário para alcançar cada nível (índice = nível - 1). */
    public static final int[] THRESHOLDS = {0, 40, 150, 400, 1000};

    private GearProgress() {}

    public static int level(ItemStack s) {
        return Math.max(1, RarityRoller.gear(s).getInt("level"));
    }

    public static int xp(ItemStack s) {
        return RarityRoller.gear(s).getInt("xp");
    }

    /** 0 = base, 1 = Desperto, 2 = Ascendido. */
    public static int visualStage(ItemStack s) {
        Rarity r = RarityRoller.rarityOf(s);
        int byRarity = r == null ? 0 : r.ordinal() >= Rarity.LEGENDARY.ordinal() ? 2 : r.ordinal() >= Rarity.RARE.ordinal() ? 1 : 0;
        int lvl = level(s);
        int byLevel = lvl >= 5 ? 2 : lvl >= 3 ? 1 : 0;
        return Math.max(byRarity, byLevel);
    }

    public static void addXp(ServerPlayerEntity p, ItemStack stack, int amount) {
        if (amount <= 0 || stack.isEmpty() || !RarityRoller.isGear(stack)) return;
        if (RarityRoller.rarityOf(stack) == null) RarityRoller.setRarity(stack, Rarity.COMMON);
        NbtCompound g = RarityRoller.gear(stack);
        int lvl = Math.max(1, g.getInt("level"));
        if (lvl >= MAX_LEVEL) return;
        int xp = g.getInt("xp") + amount;
        g.putInt("xp", xp);
        g.putInt("level", lvl);
        RarityRoller.writeGear(stack, g);
        while (lvl < MAX_LEVEL && xp >= THRESHOLDS[lvl]) {
            lvl++;
            NbtCompound up = RarityRoller.gear(stack);
            up.putInt("level", lvl);
            RarityRoller.writeGear(stack, up);
            onLevelUp(p, stack, lvl);
        }
    }

    private static void onLevelUp(ServerPlayerEntity p, ItemStack stack, int lvl) {
        int stageBefore = visualStage(stack);
        if (lvl == 3 || lvl == 5) {
            Rarity r = RarityRoller.rarityOf(stack);
            if (r != null && r.ordinal() < Rarity.MYTHIC.ordinal()) RarityRoller.setRarity(stack, Rarity.values()[r.ordinal() + 1]);
        }
        RarityRoller.addAffix(stack, p.getRandom());
        Rarity now = RarityRoller.rarityOf(stack);
        p.sendMessage(Text.translatable("rpg.europa.gear.level_up", stack.getName(), lvl,
                Text.translatable(now.translationKey()).formatted(now.color)).formatted(Formatting.GOLD));
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.PLAYERS, 0.8f, 1.4f);
        p.getServerWorld().spawnParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.2, p.getZ(), 40, 0.5, 0.6, 0.5, 0.8);
        if (visualStage(stack) > stageBefore || lvl == 3 || lvl == 5) {
            p.sendMessage(Text.translatable("rpg.europa.gear.stage." + visualStage(stack)).formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC), true);
        }
    }

    /** Arma na mão principal ganha XP ao matar. */
    public static void onKill(ServerPlayerEntity p, float victimMaxHealth) {
        addXp(p, p.getMainHandStack(), Math.max(1, Math.round(victimMaxHealth / 4f)));
    }

    /** Uma peça de armadura aleatória ganha XP ao receber dano. */
    public static void onHurt(ServerPlayerEntity p, float damage) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        ItemStack piece = p.getEquippedStack(slots[p.getRandom().nextInt(slots.length)]);
        addXp(p, piece, Math.max(1, Math.round(damage / 2f)));
    }
}
