package com.europa.rpg.client;

import com.europa.rpg.ability.AbilityHandler;
import com.europa.rpg.network.CastAbilityPayload;
import com.europa.rpg.network.RpgSyncPayload;
import com.europa.rpg.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class EuropaRPGClient implements ClientModInitializer {
    private static KeyBinding primaryKey;
    private static KeyBinding ultimateKey;

    @Override
    public void onInitializeClient() {
        primaryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.europa.primary", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.europa"));
        ultimateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.europa.ultimate", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.europa"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (primaryKey.wasPressed()) ClientPlayNetworking.send(new CastAbilityPayload(AbilityHandler.PRIMARY));
            while (ultimateKey.wasPressed()) ClientPlayNetworking.send(new CastAbilityPayload(AbilityHandler.ULTIMATE));
            ClientRpgState.tick();
        });

        ClientPlayNetworking.registerGlobalReceiver(RpgSyncPayload.ID, (payload, ctx) -> ClientRpgState.update(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientRpgState.reset());

        HudRenderCallback.EVENT.register(RpgHud::render);
        EntityRendererRegistry.register(ModEntities.CATACOMB_LORD, CatacombLordRenderer::new);
    }
}
