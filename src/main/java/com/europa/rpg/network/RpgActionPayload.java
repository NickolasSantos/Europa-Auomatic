package com.europa.rpg.network;

import com.europa.rpg.EuropaRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> servidor: ação da tela de personagem (escolher classe/subclasse, talento, patrono). */
public record RpgActionPayload(String action, String arg) implements CustomPayload {
    public static final String CHOOSE_CLASS = "class";
    public static final String CHOOSE_SUBCLASS = "subclass";
    public static final String UNLOCK_TALENT = "talent";
    public static final String PLEDGE_PATRON = "patron";
    public static final String CHOOSE_RACE = "race";

    public static final Id<RpgActionPayload> ID = new Id<>(EuropaRPG.id("action"));
    public static final PacketCodec<PacketByteBuf, RpgActionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.string(32), RpgActionPayload::action,
            PacketCodecs.string(64), RpgActionPayload::arg,
            RpgActionPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
