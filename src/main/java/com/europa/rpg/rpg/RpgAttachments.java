package com.europa.rpg.rpg;

import com.europa.rpg.EuropaRPG;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;

public final class RpgAttachments {
    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<PlayerRpgData> PLAYER_DATA = AttachmentRegistry.<PlayerRpgData>builder()
            .persistent(PlayerRpgData.CODEC)
            .copyOnDeath()
            .initializer(PlayerRpgData::new)
            .buildAndRegister(EuropaRPG.id("player_data"));

    private RpgAttachments() {}

    @SuppressWarnings("UnstableApiUsage")
    public static PlayerRpgData get(PlayerEntity player) {
        return player.getAttachedOrCreate(PLAYER_DATA);
    }

    public static void init() {}
}
