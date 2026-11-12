package com.europa.rpg.network;

import com.europa.rpg.ability.AbilityHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    private ModNetworking() {}

    public static void init() {
        PayloadTypeRegistry.playS2C().register(RpgSyncPayload.ID, RpgSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CastAbilityPayload.ID, CastAbilityPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CastAbilityPayload.ID, (payload, ctx) -> {
            int slot = payload.slot();
            if (slot == AbilityHandler.PRIMARY || slot == AbilityHandler.ULTIMATE) {
                AbilityHandler.tryCast(ctx.player(), slot);
            }
        });
    }
}
