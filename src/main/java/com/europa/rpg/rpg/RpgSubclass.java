package com.europa.rpg.rpg;

import net.minecraft.util.Formatting;

import java.util.Locale;

/** Subclasses: liberadas no nível {@link Progression#SUBCLASS_LEVEL}; cada uma concede um poder supremo (tecla G). */
public enum RpgSubclass {
    NONE(RpgClass.NONE, Formatting.GRAY, 0, 0),
    BERSERKER(RpgClass.WARRIOR, Formatting.DARK_RED, 40, 45 * 20),
    PALADIN(RpgClass.WARRIOR, Formatting.YELLOW, 50, 60 * 20),
    PYROMANCER(RpgClass.MAGE, Formatting.GOLD, 60, 30 * 20),
    CRYOMANCER(RpgClass.MAGE, Formatting.BLUE, 50, 30 * 20),
    ASSASSIN(RpgClass.ROGUE, Formatting.DARK_GRAY, 40, 25 * 20),
    SHADOWDANCER(RpgClass.ROGUE, Formatting.DARK_PURPLE, 40, 40 * 20),
    SNIPER(RpgClass.RANGER, Formatting.DARK_AQUA, 45, 25 * 20),
    BEASTMASTER(RpgClass.RANGER, Formatting.DARK_GREEN, 60, 90 * 20);

    public final RpgClass parent;
    public final Formatting color;
    public final int ultimateCost;
    public final int ultimateCooldown;

    RpgSubclass(RpgClass parent, Formatting color, int ultimateCost, int ultimateCooldown) {
        this.parent = parent;
        this.color = color;
        this.ultimateCost = ultimateCost;
        this.ultimateCooldown = ultimateCooldown;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "rpg.europa.subclass." + id();
    }

    public String abilityKey() {
        return "rpg.europa.ultimate." + id();
    }

    public static RpgSubclass byId(String id) {
        for (RpgSubclass s : values()) {
            if (s.id().equals(id)) return s;
        }
        return NONE;
    }
}
