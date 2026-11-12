package com.europa.rpg.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Targeting {
    private Targeting() {}

    /** Distância até o primeiro bloco sólido na linha de visão. */
    public static double blockDistance(ServerPlayerEntity p, double range) {
        Vec3d start = p.getEyePos();
        Vec3d end = start.add(p.getRotationVec(1f).multiply(range));
        BlockHitResult hit = p.getWorld().raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
        return hit.getType() == HitResult.Type.MISS ? range : hit.getPos().distanceTo(start);
    }

    public static Vec3d blockHitPos(ServerPlayerEntity p, double range) {
        Vec3d start = p.getEyePos();
        return start.add(p.getRotationVec(1f).multiply(blockDistance(p, range)));
    }

    /** Primeira entidade viva na mira (respeitando paredes). */
    public static LivingEntity lookTarget(ServerPlayerEntity p, double range) {
        double dist = blockDistance(p, range);
        Vec3d start = p.getEyePos();
        Vec3d dir = p.getRotationVec(1f);
        Vec3d end = start.add(dir.multiply(dist));
        Box box = p.getBoundingBox().stretch(dir.multiply(dist)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(p, start, end, box,
                e -> e instanceof LivingEntity le && le.isAlive() && !e.isSpectator() && e != p, dist * dist);
        return hit != null && hit.getEntity() instanceof LivingEntity le ? le : null;
    }

    /** Todas as entidades vivas ao longo da linha de visão (perfuração). */
    public static List<LivingEntity> lineTargets(ServerPlayerEntity p, double range) {
        double dist = blockDistance(p, range);
        Vec3d start = p.getEyePos();
        Vec3d dir = p.getRotationVec(1f);
        Vec3d end = start.add(dir.multiply(dist));
        Box box = p.getBoundingBox().stretch(dir.multiply(dist)).expand(1.0);
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : p.getWorld().getOtherEntities(p, box, e -> e instanceof LivingEntity && e.isAlive())) {
            if (e.getBoundingBox().expand(0.3).raycast(start, end).isPresent()) out.add((LivingEntity) e);
        }
        out.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(p)));
        return out;
    }

    public static void particleLine(ServerWorld world, ParticleEffect particle, Vec3d from, Vec3d to, double step) {
        Vec3d delta = to.subtract(from);
        double len = delta.length();
        if (len < 1e-3) return;
        Vec3d dir = delta.normalize();
        for (double d = 0; d <= len; d += step) {
            Vec3d pos = from.add(dir.multiply(d));
            world.spawnParticles(particle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    /** Hostis/vivos próximos, excluindo o jogador, outros jogadores e criaturas domesticadas dele. */
    public static List<LivingEntity> enemiesAround(ServerPlayerEntity p, Vec3d center, double radius) {
        Box box = new Box(center, center).expand(radius);
        return p.getWorld().getEntitiesByClass(LivingEntity.class, box, e -> e != p && e.isAlive()
                && !(e instanceof net.minecraft.entity.player.PlayerEntity)
                && !(e instanceof net.minecraft.entity.passive.TameableEntity t && t.getOwner() == p)
                && e.squaredDistanceTo(center) <= radius * radius);
    }
}
