package com.europa.rpg.profession;

import net.minecraft.util.Identifier;

import java.util.Locale;

/** Profissões: evoluem independentemente da classe, pelo próprio uso. Nível 1..50. */
public enum Profession {
    MINING("iron_pickaxe"),
    WOODCUTTING("iron_axe"),
    FARMING("wheat"),
    FISHING("fishing_rod"),
    SMITHING("anvil"),
    ALCHEMY("brewing_stand");

    public static final int MAX_LEVEL = 50;
    public final Identifier icon;

    Profession(String icon) {
        this.icon = Identifier.ofVanilla(icon);
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "rpg.europa.profession." + id();
    }

    public static int xpForNext(int level) {
        return 40 + 12 * level * level;
    }

    public static Profession byId(String id) {
        for (Profession p : values()) if (p.id().equals(id)) return p;
        return null;
    }
}
