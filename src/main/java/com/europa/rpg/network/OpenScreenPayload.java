package com.europa.rpg.network;

import com.europa.rpg.EuropaRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: abre a tela de personagem numa aba (0 ficha, 1 talentos, 2 bênção, 3 profissões). */
public record OpenScreenPayload(int tab) implements CustomPayload {
    public static final Id<OpenScreenPayload> ID = new Id<>(EuropaRPG.id("open_screen"));
    public static final PacketCodec<PacketByteBuf, OpenScreenPayload> CODEC =
            PacketCodecs.VAR_INT.<PacketByteBuf>cast().xmap(OpenScreenPayload::new, OpenScreenPayload::tab);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
