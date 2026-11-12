package com.europa.rpg.mixin;

import com.europa.rpg.profession.BrewMarker;
import com.europa.rpg.profession.Professions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.screen.BrewingStandScreenHandler$PotionSlot")
public abstract class PotionSlotMixin {
    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void europa$onTake(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity sp && BrewMarker.consume(stack)) {
            Professions.onBrewTaken(sp, stack);
        }
    }
}
