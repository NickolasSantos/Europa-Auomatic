package com.europa.rpg.client;

import com.europa.rpg.network.RpgSyncPayload;
import com.europa.rpg.rpg.PlayerRpgData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

/** Cópia no cliente do estado RPG: leve (HUD, frequente) e completa (tela de personagem, quando muda). */
public final class ClientRpgState {
    public static RpgSyncPayload data;
    public static PlayerRpgData full = new PlayerRpgData();
    /** Incrementado a cada sincronização completa; a tela usa para se reconstruir. */
    public static int version;
    public static int primaryCd, ultimateCd, racialCd;

    private ClientRpgState() {}

    public static void update(RpgSyncPayload p) {
        data = p;
        primaryCd = p.primaryCd();
        ultimateCd = p.ultimateCd();
        racialCd = p.racialCd();
        full.mana = p.mana();
    }

    public static void updateFull(NbtCompound nbt) {
        PlayerRpgData.CODEC.parse(NbtOps.INSTANCE, nbt).result().ifPresent(d -> {
            full = d;
            version++;
        });
    }

    public static void tick() {
        if (primaryCd > 0) primaryCd--;
        if (ultimateCd > 0) ultimateCd--;
        if (racialCd > 0) racialCd--;
    }

    public static void reset() {
        data = null;
        full = new PlayerRpgData();
        version++;
    }
}
