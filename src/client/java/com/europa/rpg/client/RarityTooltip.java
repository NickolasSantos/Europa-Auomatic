package com.europa.rpg.client;

import com.europa.rpg.item.Rarity;
import com.europa.rpg.item.RarityRoller;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Locale;

/** Linha de raridade e afixos especiais no tooltip dos equipamentos. */
public final class RarityTooltip {
    private RarityTooltip() {}

    public static void append(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) {
        Rarity r = RarityRoller.rarityOf(stack);
        if (r == null) return;
        int at = Math.min(1, lines.size());
        lines.add(at, Text.literal("✦ ").append(Text.translatable(r.translationKey())).formatted(r.color, Formatting.BOLD));
        for (String key : new String[]{"crit", "lifesteal", "power"}) {
            double v = RarityRoller.special(stack, key);
            if (v > 0) {
                lines.add(++at, Text.translatable("rpg.europa.affix." + key, String.format(Locale.ROOT, "%.1f", v * 100)).formatted(Formatting.BLUE));
            }
        }
    }
}
