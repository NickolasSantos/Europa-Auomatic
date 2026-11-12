package com.europa.rpg.quest;

import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Definição de missão.
 * KILL: matar {@code count} entidades {@code target} (progresso automático, conclui sozinha).
 * COLLECT: entregar {@code count} itens {@code target} (no Quadro de Missões ou com /europa quest turnin).
 */
public record Quest(String id, Type type, Identifier target, int count, int minLevel, String prerequisite,
                    int xpReward, List<Reward> items, String faction, int repReward, int fragments) {
    public enum Type { KILL, COLLECT }

    public record Reward(Identifier item, int count) {}

    public String titleKey() {
        return "quest.europa." + id;
    }

    public String descKey() {
        return "quest.europa." + id + ".desc";
    }
}
