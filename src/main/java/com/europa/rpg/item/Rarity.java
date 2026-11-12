package com.europa.rpg.item;

import net.minecraft.util.Formatting;

import java.util.Locale;

/** Raridades do documento mestre. {@code affixes} = número de afixos; {@code scale} multiplica seus valores. */
public enum Rarity {
    COMMON(Formatting.WHITE, 500, 0, 1.0),
    UNCOMMON(Formatting.GREEN, 250, 1, 1.0),
    RARE(Formatting.BLUE, 130, 2, 1.2),
    EPIC(Formatting.DARK_PURPLE, 70, 3, 1.4),
    LEGENDARY(Formatting.GOLD, 35, 4, 1.6),
    MYTHIC(Formatting.RED, 12, 5, 1.9),
    DIVINE(Formatting.YELLOW, 3, 6, 2.3);

    public final Formatting color;
    public final int weight;
    public final int affixes;
    public final double scale;

    Rarity(Formatting color, int weight, int affixes, double scale) {
        this.color = color;
        this.weight = weight;
        this.affixes = affixes;
        this.scale = scale;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "rpg.europa.rarity." + id();
    }

    public static Rarity byId(String id) {
        for (Rarity r : values()) if (r.id().equals(id)) return r;
        return null;
    }
}
