package com.europa.rpg.ability;

import com.europa.rpg.util.Targeting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Utilitários de efeitos usados pelas habilidades. */
final class Fx {
    private Fx() {}

    static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1f, pitch);
    }

    static void ring(ServerWorld world, Vec3d c, double r, ParticleEffect particle) {
        int n = (int) Math.max(16, r * 8);
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            world.spawnParticles(particle, c.x + Math.cos(a) * r, c.y + 0.3, c.z + Math.sin(a) * r, 1, 0, 0.1, 0, 0);
        }
    }

    static boolean noTarget(ServerPlayerEntity p) {
        p.sendMessage(Text.translatable("rpg.europa.no_target").formatted(Formatting.GRAY), true);
        return false;
    }

    static boolean isUndead(LivingEntity e) {
        return e.getType().isIn(EntityTypeTags.UNDEAD);
    }

    static List<ServerPlayerEntity> allies(ServerPlayerEntity p, double radius) {
        return p.getServerWorld().getPlayers(o -> o.isAlive() && !o.isSpectator() && o.squaredDistanceTo(p) <= radius * radius);
    }

    static void buff(PlayerEntity p, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect, int ticks, int amp) {
        p.addStatusEffect(new StatusEffectInstance(effect, ticks, amp));
    }

    static void root(LivingEntity e, int ticks) {
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, ticks, 6));
    }

    static List<LivingEntity> enemies(ServerPlayerEntity p, Vec3d c, double r) {
        return Targeting.enemiesAround(p, c, r);
    }
}
