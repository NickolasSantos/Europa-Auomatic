package com.europa.rpg.client;

import com.europa.rpg.network.RpgSyncPayload;

/** Cópia no cliente do estado RPG, alimentada por {@link RpgSyncPayload}. */
public final class ClientRpgState {
    public static RpgSyncPayload data;
    /** Cooldowns interpolados localmente entre sincronizações. */
    public static int primaryCd, ultimateCd;

    private ClientRpgState() {}

    public static void update(RpgSyncPayload p) {
        data = p;
        primaryCd = p.primaryCd();
        ultimateCd = p.ultimateCd();
    }

    public static void tick() {
        if (primaryCd > 0) primaryCd--;
        if (ultimateCd > 0) ultimateCd--;
    }

    public static void reset() {
        data = null;
    }
}
