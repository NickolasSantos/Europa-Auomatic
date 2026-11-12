package com.europa.rpg.network;

import com.europa.rpg.ability.AbilityHandler;
import com.europa.rpg.blessing.Blessings;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgActions;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    private ModNetworking() {}

    public static void init() {
        PayloadTypeRegistry.playS2C().register(RpgSyncPayload.ID, RpgSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RpgFullSyncPayload.ID, RpgFullSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenScreenPayload.ID, OpenScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CastAbilityPayload.ID, CastAbilityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RpgActionPayload.ID, RpgActionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CastAbilityPayload.ID, (payload, ctx) -> {
            int slot = payload.slot();
            AbilityHandler.tryCast(ctx.player(), slot);
        });
        ServerPlayNetworking.registerGlobalReceiver(RpgActionPayload.ID, (payload, ctx) -> {
            var p = ctx.player();
            switch (payload.action()) {
                case RpgActionPayload.CHOOSE_CLASS -> RpgActions.chooseClass(p, payload.arg());
                case RpgActionPayload.CHOOSE_SUBCLASS -> RpgActions.chooseSubclass(p, payload.arg());
                case RpgActionPayload.UNLOCK_TALENT -> RpgActions.unlockTalent(p, payload.arg());
                case RpgActionPayload.PLEDGE_PATRON -> Blessings.pledge(p, payload.arg());
                case RpgActionPayload.CHOOSE_RACE -> RpgActions.chooseRace(p, payload.arg());
                default -> {}
            }
            Progression.fullSync(p, true);
        });
    }
}
