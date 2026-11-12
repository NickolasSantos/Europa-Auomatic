package com.europa.rpg.economy;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

/**
 * Pressão de oferta por item (em fragmentos vendidos recentemente), persistida no mundo.
 * Decai com o tempo; quanto maior, menor o preço de venda — evita inflação por farm de um único recurso.
 */
public class MarketState extends PersistentState {
    private static final Type<MarketState> TYPE = new Type<>(MarketState::new, MarketState::fromNbt, null);
    public final Map<String, Double> pressure = new HashMap<>();

    public static MarketState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "europa_market");
    }

    private static MarketState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        MarketState s = new MarketState();
        NbtCompound p = nbt.getCompound("pressure");
        for (String k : p.getKeys()) s.pressure.put(k, p.getDouble(k));
        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound p = new NbtCompound();
        pressure.forEach(p::putDouble);
        nbt.put("pressure", p);
        return nbt;
    }

    /** Decaimento de 5% por minuto (meia-vida ≈ 13,5 min). */
    public void decay() {
        if (pressure.isEmpty()) return;
        pressure.replaceAll((k, v) -> v * 0.95);
        pressure.values().removeIf(v -> v < 0.5);
        markDirty();
    }
}
