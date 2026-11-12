package com.europa.rpg.quest;

import com.europa.rpg.EuropaRPG;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.europa.rpg.quest.Quest.Reward;
import static com.europa.rpg.quest.Quest.Type.COLLECT;
import static com.europa.rpg.quest.Quest.Type.KILL;

/** Linha de missões do mod, em ordem de progressão. */
public final class Quests {
    public static final Map<String, Quest> ALL = new LinkedHashMap<>();
    public static final int MAX_ACTIVE = 3;

    static {
        add(q("undead_patrol", KILL, mc("zombie"), 10, 1, null, 150,
                List.of(new Reward(mc("bread"), 8), new Reward(mc("iron_ingot"), 4))));
        add(q("bone_collector", KILL, mc("skeleton"), 8, 2, null, 180,
                List.of(new Reward(mc("arrow"), 32), new Reward(mc("golden_apple"), 1))));
        add(q("spider_nest", KILL, mc("spider"), 6, 3, null, 200,
                List.of(new Reward(mc("cobweb"), 4), new Reward(EuropaRPG.id("raw_mithril"), 3))));
        add(q("mithril_prospector", COLLECT, EuropaRPG.id("raw_mithril"), 8, 4, null, 350,
                List.of(new Reward(EuropaRPG.id("mithril_pickaxe"), 1))));
        add(q("creeper_hunt", KILL, mc("creeper"), 5, 5, "undead_patrol", 300,
                List.of(new Reward(mc("tnt"), 4), new Reward(mc("experience_bottle"), 8))));
        add(q("deep_forge", COLLECT, EuropaRPG.id("adamantite_ingot"), 4, 8, "mithril_prospector", 600,
                List.of(new Reward(EuropaRPG.id("adamantite_chestplate"), 1))));
        add(q("ender_threat", KILL, mc("enderman"), 5, 10, "creeper_hunt", 700,
                List.of(new Reward(mc("ender_pearl"), 8), new Reward(mc("diamond"), 3))));
        add(q("soul_harvest", COLLECT, EuropaRPG.id("soul_shard"), 6, 12, "deep_forge", 900,
                List.of(new Reward(EuropaRPG.id("catacomb_key"), 1))));
        add(q("blaze_trial", KILL, mc("blaze"), 8, 14, "ender_threat", 1000,
                List.of(new Reward(EuropaRPG.id("arcane_staff"), 1))));
        add(q("catacomb_lord", KILL, EuropaRPG.id("catacomb_lord"), 1, 15, "soul_harvest", 2500,
                List.of(new Reward(EuropaRPG.id("soulreaper"), 1), new Reward(mc("nether_star"), 1))));

        // Contratos de facção
        add(contract("observatory", "star_charts", COLLECT, mc("amethyst_shard"), 16, 3, 250, 150, 20));
        add(contract("observatory", "far_horizons", COLLECT, mc("ender_pearl"), 6, 8, 500, 300, 45));
        add(contract("wanderers", "road_wardens", KILL, mc("pillager"), 8, 5, 400, 200, 30));
        add(contract("wanderers", "cave_sweep", KILL, mc("cave_spider"), 12, 4, 300, 150, 20));
        add(contract("gears", "spare_parts", COLLECT, mc("redstone"), 48, 2, 200, 150, 15));
        add(contract("gears", "copper_contract", COLLECT, mc("copper_ingot"), 32, 4, 300, 200, 25));
        add(contract("nameless", "soul_tithe", COLLECT, EuropaRPG.id("soul_shard"), 8, 10, 600, 300, 50));
        add(contract("nameless", "night_rites", KILL, mc("witch"), 5, 8, 500, 250, 40));
    }

    private Quests() {}

    private static void add(Quest q) {
        ALL.put(q.id(), q);
    }

    /** Missão sem facção (compatível com a lista original). */
    private static Quest q(String id, Quest.Type t, Identifier target, int count, int minLevel, String prereq, int xp, List<Reward> items) {
        return new Quest(id, t, target, count, minLevel, prereq, xp, items, null, 0, xp / 20);
    }

    /** Contrato de facção: rende reputação e Fragmentos de Aster. */
    private static Quest contract(String faction, String id, Quest.Type t, Identifier target, int count, int minLevel, int xp, int rep, int fragments) {
        return new Quest(id, t, target, count, minLevel, null, xp, List.of(), faction, rep, fragments);
    }

    private static Identifier mc(String path) {
        return Identifier.ofVanilla(path);
    }

    public static Quest get(String id) {
        return ALL.get(id);
    }
}
