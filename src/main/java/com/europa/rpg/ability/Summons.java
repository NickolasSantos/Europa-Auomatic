package com.europa.rpg.ability;

import com.europa.rpg.entity.ServantEntity;
import com.europa.rpg.registry.ModEntities;
import com.europa.rpg.util.TaskScheduler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/** Invocações temporárias: servos, lobos espirituais e golens. Limite total por dono. */
public final class Summons {
    public static final int MAX_PER_OWNER = 6;
    public static final String TAG = "europa_summon";
    private static final String OWNER_PREFIX = "europa_owner:";

    public enum Kind { SERVANT, WOLF, GOLEM }

    private Summons() {}

    public static void init() {
        // Invocações não sobrevivem a reinícios do servidor (o temporizador vive em memória).
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.getCommandTags().contains(TAG) && !TaskScheduler.isTracked(entity.getUuid())) entity.discard();
        });
    }

    public static boolean isSummonOf(Entity e, ServerPlayerEntity p) {
        return e.getCommandTags().contains(OWNER_PREFIX + p.getUuidAsString());
    }

    public static List<MobEntity> owned(ServerPlayerEntity p) {
        return p.getServerWorld().getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(64), e -> e.isAlive() && isSummonOf(e, p));
    }

    /** Invoca até {@code count} criaturas (respeitando o limite). Retorna quantas foram criadas. */
    public static int summon(ServerPlayerEntity p, Kind kind, int count, int lifetimeTicks, float power, boolean empowered) {
        ServerWorld w = p.getServerWorld();
        int room = MAX_PER_OWNER - owned(p).size();
        int made = 0;
        for (int i = 0; i < Math.min(count, room); i++) {
            MobEntity mob = switch (kind) {
                case SERVANT -> ModEntities.SERVANT.create(w);
                case WOLF -> EntityType.WOLF.create(w);
                case GOLEM -> EntityType.IRON_GOLEM.create(w);
            };
            if (mob == null) continue;
            BlockPos pos = p.getBlockPos().add(w.random.nextInt(3) - 1, 0, w.random.nextInt(3) - 1);
            mob.refreshPositionAndAngles(pos, p.getYaw(), 0);
            switch (mob) {
                case ServantEntity s -> s.bind(p, lifetimeTicks, power);
                case WolfEntity wolf -> {
                    wolf.setOwner(p);
                    wolf.setCustomName(Text.translatable("rpg.europa.spirit_wolf").formatted(Formatting.DARK_GREEN));
                }
                case IronGolemEntity golem -> {
                    golem.setPlayerCreated(true);
                    golem.setCustomName(Text.translatable("rpg.europa.stone_guardian").formatted(Formatting.GRAY));
                }
                default -> {}
            }
            mob.addCommandTag(TAG);
            mob.addCommandTag(OWNER_PREFIX + p.getUuidAsString());
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, lifetimeTicks, empowered ? 1 : 0));
            if (empowered) mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, lifetimeTicks, 0));
            TaskScheduler.track(mob.getUuid()); // antes do spawn: ENTITY_LOAD dispara durante spawnEntity
            w.spawnEntity(mob);
            w.spawnParticles(kind == Kind.SERVANT ? ParticleTypes.SOUL : ParticleTypes.POOF, mob.getX(), mob.getY() + 0.5, mob.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
            TaskScheduler.schedule(lifetimeTicks, () -> {
                TaskScheduler.untrack(mob.getUuid());
                if (mob.isAlive()) {
                    w.spawnParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + 0.5, mob.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
                    mob.discard();
                }
            });
            made++;
        }
        if (made == 0) p.sendMessage(Text.translatable("rpg.europa.servant_limit").formatted(Formatting.GRAY), true);
        return made;
    }

    /** Ordena às invocações do jogador que ataquem o alvo. */
    public static void focus(ServerPlayerEntity p, LivingEntity target) {
        for (MobEntity m : owned(p)) {
            if (m != target && m.getTarget() == null && !(target instanceof ServerPlayerEntity)) m.setTarget(target);
        }
    }
}
