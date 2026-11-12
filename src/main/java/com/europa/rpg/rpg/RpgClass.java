package com.europa.rpg.rpg;

import net.minecraft.util.Formatting;

import java.util.Locale;

/** Classes base. Cada classe tem uma habilidade primária (tecla R) e duas subclasses. */
public enum RpgClass {
    NONE(Formatting.GRAY, 0, 0, 1.0f),
    WARRIOR(Formatting.RED, 20, 8 * 20, 1.0f),
    MAGE(Formatting.AQUA, 15, 30, 3.0f),
    ROGUE(Formatting.DARK_GREEN, 25, 10 * 20, 1.5f),
    RANGER(Formatting.GREEN, 25, 8 * 20, 1.5f);

    public final Formatting color;
    public final int primaryCost;
    public final int primaryCooldown;
    /** Mana regenerada por segundo. */
    public final float manaRegen;

    RpgClass(Formatting color, int primaryCost, int primaryCooldown, float manaRegen) {
        this.color = color;
        this.primaryCost = primaryCost;
        this.primaryCooldown = primaryCooldown;
        this.manaRegen = manaRegen;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "rpg.europa.class." + id();
    }

    public String abilityKey() {
        return "rpg.europa.ability." + id();
    }

    public static RpgClass byId(String id) {
        for (RpgClass c : values()) {
            if (c.id().equals(id)) return c;
        }
        return NONE;
    }

    public RpgSubclass[] subclasses() {
        return switch (this) {
            case WARRIOR -> new RpgSubclass[]{RpgSubclass.BERSERKER, RpgSubclass.PALADIN};
            case MAGE -> new RpgSubclass[]{RpgSubclass.PYROMANCER, RpgSubclass.CRYOMANCER};
            case ROGUE -> new RpgSubclass[]{RpgSubclass.ASSASSIN, RpgSubclass.SHADOWDANCER};
            case RANGER -> new RpgSubclass[]{RpgSubclass.SNIPER, RpgSubclass.BEASTMASTER};
            case NONE -> new RpgSubclass[0];
        };
    }
}
