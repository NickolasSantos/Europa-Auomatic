package com.europa.rpg.mixin;

import com.europa.rpg.profession.BrewMarker;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Marca as poções recém-preparadas para que só elas rendam XP de Alquimia ao serem retiradas. */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
    @Inject(method = "craft", at = @At("TAIL"))
    private static void europa$markBrewed(World world, BlockPos pos, DefaultedList<ItemStack> slots, CallbackInfo ci) {
        for (int i = 0; i < 3; i++) BrewMarker.mark(slots.get(i));
    }
}
