package com.europa.rpg.network;

import com.europa.rpg.EuropaRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: estado do HUD. */
public record RpgSyncPayload(String rpgClass, String subclass, int level, int xp, int xpNeeded,
                             float mana, float maxMana,
                             int primaryCd, int primaryCdMax, int ultimateCd, int ultimateCdMax) implements CustomPayload {
    public static final Id<RpgSyncPayload> ID = new Id<>(EuropaRPG.id("sync"));
    public static final PacketCodec<PacketByteBuf, RpgSyncPayload> CODEC = PacketCodec.of(RpgSyncPayload::write, RpgSyncPayload::read);

    private void write(PacketByteBuf buf) {
        buf.writeString(rpgClass);
        buf.writeString(subclass);
        buf.writeVarInt(level);
        buf.writeVarInt(xp);
        buf.writeVarInt(xpNeeded);
        buf.writeFloat(mana);
        buf.writeFloat(maxMana);
        buf.writeVarInt(primaryCd);
        buf.writeVarInt(primaryCdMax);
        buf.writeVarInt(ultimateCd);
        buf.writeVarInt(ultimateCdMax);
    }

    private static RpgSyncPayload read(PacketByteBuf buf) {
        return new RpgSyncPayload(buf.readString(), buf.readString(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
