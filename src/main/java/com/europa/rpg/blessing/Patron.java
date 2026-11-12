package com.europa.rpg.blessing;

import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Patrono divino. O jogador jura lealdade a um patrono num Santuário e ganha favor com oferendas.
 * Tier 1 ao jurar; tier 2 com {@link #TIER2_FAVOR}; tier 3 com {@link #TIER3_FAVOR}.
 */
public record Patron(String id, Formatting color, Identifier icon, Map<Identifier, Integer> offerings) {
    public static final int TIER2_FAVOR = 150;
    public static final int TIER3_FAVOR = 500;
    public static final int MAX_FAVOR = 1000;

    private static final Map<String, Patron> ALL = new LinkedHashMap<>();

    public static final Patron SOLENNE = reg("solenne", Formatting.GOLD, "sunflower",
            "gold_ingot", 5, "sunflower", 3, "glowstone_dust", 2, "golden_apple", 25, "gold_block", 45);
    public static final Patron MORVATH = reg("morvath", Formatting.DARK_PURPLE, "crying_obsidian",
            "ender_pearl", 6, "obsidian", 3, "crying_obsidian", 5, "wither_rose", 20, "echo_shard", 30);
    public static final Patron THALOR = reg("thalor", Formatting.GRAY, "anvil",
            "raw_iron", 2, "iron_ingot", 3, "diamond", 15, "europa:adamantite_ingot", 12, "netherite_scrap", 40);
    public static final Patron YSMERA = reg("ysmera", Formatting.AQUA, "heart_of_the_sea",
            "cod", 1, "salmon", 1, "prismarine_shard", 3, "prismarine_crystals", 4, "nautilus_shell", 20, "heart_of_the_sea", 80);
    public static final Patron KAELITH = reg("kaelith", Formatting.YELLOW, "lightning_rod",
            "feather", 1, "copper_ingot", 1, "lightning_rod", 6, "phantom_membrane", 8, "breeze_rod", 12);
    public static final Patron VERDANYA = reg("verdanya", Formatting.GREEN, "oak_sapling",
            "wheat", 1, "oak_sapling", 2, "glow_berries", 2, "golden_carrot", 6, "emerald", 10);

    private static Patron reg(String id, Formatting color, String icon, Object... offerings) {
        Map<Identifier, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < offerings.length; i += 2) map.put(Identifier.of((String) offerings[i]), (Integer) offerings[i + 1]);
        Patron p = new Patron(id, color, Identifier.of(icon), map);
        ALL.put(id, p);
        return p;
    }

    public static Patron byId(String id) {
        return ALL.get(id);
    }

    public static List<Patron> all() {
        return List.copyOf(ALL.values());
    }

    public static int tier(int favor) {
        return favor >= TIER3_FAVOR ? 3 : favor >= TIER2_FAVOR ? 2 : 1;
    }

    public String translationKey() {
        return "rpg.europa.patron." + id;
    }
}
