package com.europa.rpg.client;

import com.europa.rpg.item.GearProgress;
import com.europa.rpg.item.Rarity;
import com.europa.rpg.item.RarityRoller;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Locale;

/** Nome colorido pela raridade, linha de raridade/nível com barra de XP e afixos especiais. */
public final class RarityTooltip {
    private RarityTooltip() {}

    public static void append(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) {
        Rarity r = RarityRoller.rarityOf(stack);
        if (r == null || lines.isEmpty()) return;
        lines.set(0, lines.get(0).copy().formatted(r.color));
        int at = 1;
        int lvl = GearProgress.level(stack);
        lines.add(at, Text.literal("✦ ").append(Text.translatable(r.translationKey())).formatted(r.color, Formatting.BOLD)
                .append(Text.literal("  ").append(Text.translatable("rpg.europa.gear.level", lvl, GearProgress.MAX_LEVEL)).formatted(Formatting.GRAY)));
        if (lvl < GearProgress.MAX_LEVEL) {
            int xp = GearProgress.xp(stack), need = GearProgress.THRESHOLDS[lvl];
            int filled = Math.round(10f * Math.min(1f, xp / (float) need));
            lines.add(++at, Text.literal("▮".repeat(filled)).formatted(Formatting.AQUA)
                    .append(Text.literal("▮".repeat(10 - filled)).formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(" " + xp + "/" + need + " XP").formatted(Formatting.GRAY)));
        }
        int stage = GearProgress.visualStage(stack);
        if (stage > 0) lines.add(++at, Text.translatable("rpg.europa.gear.stage." + stage).formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
        for (String key : new String[]{"crit", "lifesteal", "power"}) {
            double v = RarityRoller.special(stack, key);
            if (v > 0) lines.add(++at, Text.translatable("rpg.europa.affix." + key, String.format(Locale.ROOT, "%.1f", v * 100)).formatted(Formatting.BLUE));
        }
    }
}
