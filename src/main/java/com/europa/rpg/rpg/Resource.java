package com.europa.rpg.rpg;

import java.util.Locale;

/**
 * Recurso de combate da classe. Mana e afins regeneram com o tempo; Fúria e Oportunidade são
 * construídas em combate e decaem fora dele; Cargas recarregam devagar e têm poucas unidades.
 * {@code costScale} converte o custo nominal da habilidade (escala de mana) para a escala do recurso.
 */
public enum Resource {
    MANA(0xFF3B82F6, 100, 0f, false, 1f),
    FURY(0xFFDC2626, 100, 0f, true, 1f),
    FAITH(0xFFF5C542, 100, 1.5f, false, 1f),
    ENERGY(0xFFFACC15, 100, 5f, false, 1f),
    SPIRIT(0xFF2DD4BF, 100, 1f, false, 1f),
    OPPORTUNITY(0xFF22C55E, 5, 0f, true, 0.06f),
    BOND(0xFF84CC16, 100, 2f, false, 1f),
    CHARGES(0xFFA78BFA, 5, 1f / 6f, false, 0.06f);

    public final int color;
    public final int baseMax;
    /** Regeneração por segundo (MANA usa a regeneração da classe). */
    public final float regen;
    /** Construído em combate; decai após alguns segundos sem lutar. */
    public final boolean combatBuilt;
    public final float costScale;

    Resource(int color, int baseMax, float regen, boolean combatBuilt, float costScale) {
        this.color = color;
        this.baseMax = baseMax;
        this.regen = regen;
        this.combatBuilt = combatBuilt;
        this.costScale = costScale;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "rpg.europa.resource." + id();
    }

    public boolean smallPool() {
        return baseMax <= 10;
    }

    public static Resource byId(String id) {
        for (Resource r : values()) if (r.id().equals(id)) return r;
        return MANA;
    }
}
