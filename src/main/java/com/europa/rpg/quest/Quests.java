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
        add(new Quest("undead_patrol", KILL, mc("zombie"), 10, 1, null, 150,
                List.of(new Reward(mc("bread"), 8), new Reward(mc("iron_ingot"), 4))));
        add(new Quest("bone_collector", KILL, mc("skeleton"), 8, 2, null, 180,
                List.of(new Reward(mc("arrow"), 32), new Reward(mc("golden_apple"), 1))));
        add(new Quest("spider_nest", KILL, mc("spider"), 6, 3, null, 200,
                List.of(new Reward(mc("cobweb"), 4), new Reward(EuropaRPG.id("raw_mithril"), 3))));
        add(new Quest("mithril_prospector", COLLECT, EuropaRPG.id("raw_mithril"), 8, 4, null, 350,
                List.of(new Reward(EuropaRPG.id("mithril_pickaxe"), 1))));
        add(new Quest("creeper_hunt", KILL, mc("creeper"), 5, 5, "undead_patrol", 300,
                List.of(new Reward(mc("tnt"), 4), new Reward(mc("experience_bottle"), 8))));
        add(new Quest("deep_forge", COLLECT, EuropaRPG.id("adamantite_ingot"), 4, 8, "mithril_prospector", 600,
                List.of(new Reward(EuropaRPG.id("adamantite_chestplate"), 1))));
        add(new Quest("ender_threat", KILL, mc("enderman"), 5, 10, "creeper_hunt", 700,
                List.of(new Reward(mc("ender_pearl"), 8), new Reward(mc("diamond"), 3))));
        add(new Quest("soul_harvest", COLLECT, EuropaRPG.id("soul_shard"), 6, 12, "deep_forge", 900,
                List.of(new Reward(EuropaRPG.id("catacomb_key"), 1))));
        add(new Quest("blaze_trial", KILL, mc("blaze"), 8, 14, "ender_threat", 1000,
                List.of(new Reward(EuropaRPG.id("arcane_staff"), 1))));
        add(new Quest("catacomb_lord", KILL, EuropaRPG.id("catacomb_lord"), 1, 15, "soul_harvest", 2500,
                List.of(new Reward(EuropaRPG.id("soulreaper"), 1), new Reward(mc("nether_star"), 1))));
    }

    private Quests() {}

    private static void add(Quest q) {
        ALL.put(q.id(), q);
    }

    private static Identifier mc(String path) {
        return Identifier.ofVanilla(path);
    }

    public static Quest get(String id) {
        return ALL.get(id);
    }
}
