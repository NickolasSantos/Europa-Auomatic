package com.europa.rpg.mixin;

import com.europa.rpg.profession.Professions;
import net.minecraft.advancement.criterion.PlayerGeneratesContainerLootCriterion;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Disparado quando um jogador gera o loot de um baú de estrutura pela primeira vez (Arqueólogo de Ruínas). */
@Mixin(PlayerGeneratesContainerLootCriterion.class)
public abstract class ContainerLootCriterionMixin {
    @Inject(method = "trigger", at = @At("HEAD"))
    private void europa$onLoot(ServerPlayerEntity player, RegistryKey<LootTable> lootTable, CallbackInfo ci) {
        Professions.onLootGenerated(player, lootTable.getValue());
    }
}
