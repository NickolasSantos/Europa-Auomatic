package com.europa.rpg.ability;

import com.europa.rpg.rpg.PlayerRpgData;
import net.minecraft.server.network.ServerPlayerEntity;

/** Definição de habilidade: custo de mana, recarga (ticks) e ação. A ação retorna false se não pôde ser usada. */
public record AbilityDef(String id, int manaCost, int cooldown, Action action) {
    @FunctionalInterface
    public interface Action {
        boolean cast(ServerPlayerEntity player, PlayerRpgData data, float power);
    }

    public String translationKey() {
        return "rpg.europa.ability." + id;
    }
}
