package com.europa.rpg.mixin;

import com.europa.rpg.rpg.CombatHooks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float europa$modifyDamage(float amount, DamageSource source) {
        return CombatHooks.modifyIncoming((LivingEntity) (Object) this, source, amount);
    }
}
