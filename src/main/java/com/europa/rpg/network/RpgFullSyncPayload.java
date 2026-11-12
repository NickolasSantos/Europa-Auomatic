package com.europa.rpg.network;

import com.europa.rpg.EuropaRPG;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: estado RPG completo, codificado com {@code PlayerRpgData.CODEC}. */
public record RpgFullSyncPayload(NbtCompound data) implements CustomPayload {
    public static final Id<RpgFullSyncPayload> ID = new Id<>(EuropaRPG.id("full_sync"));
    public static final PacketCodec<PacketByteBuf, RpgFullSyncPayload> CODEC =
            PacketCodecs.NBT_COMPOUND.<PacketByteBuf>cast().xmap(RpgFullSyncPayload::new, RpgFullSyncPayload::data);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
