package com.europa.rpg.talent;

import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.talent.TalentNode.Branch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.europa.rpg.talent.TalentEffect.*;

/** Árvore de talentos compartilhada por todas as classes: 3 ramos × 5 tiers, 55 ranks no total (49 pontos no nível 50). */
public final class Talents {
    public static final int CAPSTONE_REQUIREMENT = 10;
    private static final Map<String, TalentNode> ALL = new LinkedHashMap<>();

    static {
        // Combate
        add("brute_force", Branch.COMBAT, 1, 5, ATTACK_DAMAGE, 0.5);
        add("keen_edge", Branch.COMBAT, 2, 5, CRIT_CHANCE, 0.03);
        add("focused_power", Branch.COMBAT, 3, 5, ABILITY_POWER, 0.05);
        add("bloodthirst", Branch.COMBAT, 4, 3, LIFESTEAL, 0.02);
        add("executioner", Branch.COMBAT, 5, 1, EXECUTE, 0.30);
        // Defesa
        add("vigor", Branch.DEFENSE, 1, 5, MAX_HEALTH, 2);
        add("iron_skin", Branch.DEFENSE, 2, 5, ARMOR, 1);
        add("steadfast", Branch.DEFENSE, 3, 3, TOUGHNESS, 1);
        add("second_wind", Branch.DEFENSE, 4, 3, REGEN, 0.5);
        add("last_stand", Branch.DEFENSE, 5, 1, LAST_STAND, 1);
        // Arcano
        add("deep_mind", Branch.ARCANE, 1, 5, MAX_MANA, 10);
        add("flow", Branch.ARCANE, 2, 5, MANA_REGEN, 0.4);
        add("celerity", Branch.ARCANE, 3, 5, COOLDOWN, 0.04);
        add("efficiency", Branch.ARCANE, 4, 3, MANA_COST, 0.05);
        add("overload", Branch.ARCANE, 5, 1, OVERLOAD, 0.15);
    }

    private Talents() {}

    private static void add(String id, Branch b, int tier, int maxRank, TalentEffect e, double v) {
        ALL.put(id, new TalentNode(id, b, tier, maxRank, e, v));
    }

    public static TalentNode get(String id) {
        return ALL.get(id);
    }

    public static List<TalentNode> all() {
        return List.copyOf(ALL.values());
    }

    public static TalentNode at(Branch b, int tier) {
        for (TalentNode n : ALL.values()) if (n.branch() == b && n.tier() == tier) return n;
        return null;
    }

    /** Pontos ganhos: 1 por nível após o primeiro. */
    public static int earnedPoints(PlayerRpgData d) {
        return Math.max(0, d.level - 1);
    }

    public static int spentPoints(PlayerRpgData d) {
        return d.talents.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static int availablePoints(PlayerRpgData d) {
        return earnedPoints(d) - spentPoints(d);
    }

    public static int pointsInBranch(PlayerRpgData d, Branch b) {
        int sum = 0;
        for (TalentNode n : ALL.values()) if (n.branch() == b) sum += d.talents.getOrDefault(n.id(), 0);
        return sum;
    }

    public static int rank(PlayerRpgData d, TalentNode n) {
        return d.talents.getOrDefault(n.id(), 0);
    }

    public enum Status { OK, MAXED, NO_POINTS, NEEDS_PREVIOUS, NEEDS_BRANCH_POINTS, NO_CLASS }

    public static Status canUnlock(PlayerRpgData d, TalentNode n) {
        if (!d.hasClass()) return Status.NO_CLASS;
        if (rank(d, n) >= n.maxRank()) return Status.MAXED;
        if (availablePoints(d) <= 0) return Status.NO_POINTS;
        if (n.tier() > 1) {
            TalentNode prev = at(n.branch(), n.tier() - 1);
            if (prev != null && rank(d, prev) < 1) return Status.NEEDS_PREVIOUS;
        }
        if (n.tier() == 5 && pointsInBranch(d, n.branch()) < CAPSTONE_REQUIREMENT) return Status.NEEDS_BRANCH_POINTS;
        return Status.OK;
    }

    public static boolean unlock(PlayerRpgData d, TalentNode n) {
        if (canUnlock(d, n) != Status.OK) return false;
        d.talents.merge(n.id(), 1, Integer::sum);
        return true;
    }

    /** Soma de todos os nós com o efeito dado (valor × rank). */
    public static double total(PlayerRpgData d, TalentEffect effect) {
        double sum = 0;
        for (TalentNode n : ALL.values()) {
            if (n.effect() == effect) sum += n.valuePerRank() * d.talents.getOrDefault(n.id(), 0);
        }
        return sum;
    }
}
