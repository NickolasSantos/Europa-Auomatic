package com.europa.rpg.network;

import com.europa.rpg.EuropaRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> servidor: pedido para usar a habilidade do slot (0 primária, 1 suprema). */
public record CastAbilityPayload(int slot) implements CustomPayload {
    public static final Id<CastAbilityPayload> ID = new Id<>(EuropaRPG.id("cast"));
    public static final PacketCodec<PacketByteBuf, CastAbilityPayload> CODEC =
            PacketCodecs.VAR_INT.<PacketByteBuf>cast().xmap(CastAbilityPayload::new, CastAbilityPayload::slot);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
