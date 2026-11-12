package com.europa.rpg.ability;

import com.europa.rpg.rpg.PlayerRpgData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Supplier;

/**
 * Definição de habilidade: custo nominal (escala de mana), recarga (ticks), ação e descrição.
 * A descrição é gerada a partir dos números (DSL) ou vem do arquivo de idioma (habilidades artesanais).
 */
public record AbilityDef(String id, int manaCost, int cooldown, Action action, Supplier<Text> description) {
    @FunctionalInterface
    public interface Action {
        boolean cast(ServerPlayerEntity player, PlayerRpgData data, float power);
    }

    public String translationKey() {
        return "rpg.europa.ability." + id;
    }
}
