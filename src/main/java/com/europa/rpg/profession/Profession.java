package com.europa.rpg.profession;

import net.minecraft.util.Identifier;

import java.util.Locale;

/**
 * Profissões do documento mestre, independentes da classe e evoluídas pelo uso (nível 1..50).
 * Cada uma está ligada a uma atividade concreta do jogo (ver {@link Professions}).
 */
public enum Profession {
    AUTOMATON_ENGINEER("piston"),      // fabricar mecanismos de redstone
    ECHO_WEAVER("echo_shard"),         // extrair sculk e ametista
    METAMORPH_COOK("cooked_beef"),     // cozinhar e preparar alimentos
    ARTIFACT_RESTORER("anvil"),        // forjar e reparar equipamentos
    ANOMALY_GEOLOGIST("iron_pickaxe"), // minerar minérios
    ESSENCE_COLLECTOR("experience_bottle"), // abater criaturas hostis
    BIOME_GARDENER("oak_sapling"),     // colheitas e madeira
    PHENOMENON_TAMER("lead"),          // pescar e criar animais
    RUIN_ARCHAEOLOGIST("brush"),       // saquear baús de ruínas e estruturas
    RELIC_CARTOMANCER("enchanted_book"), // encantar
    MEMORY_DISTILLER("brewing_stand"), // preparar poções
    DIMENSIONAL_NAVIGATOR("compass");  // viajar e cruzar dimensões

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

    /** Tier: 0 Aprendiz, 1 Praticante, 2 Especialista, 3 Mestre, 4 Lendário. */
    public static int tier(int level) {
        return Math.min(4, level / 10);
    }

    public static String tierKey(int level) {
        return "rpg.europa.profession.tier." + tier(level);
    }

    public static Profession byId(String id) {
        for (Profession p : values()) if (p.id().equals(id)) return p;
        return null;
    }
}
