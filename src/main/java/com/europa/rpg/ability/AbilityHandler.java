package com.europa.rpg.ability;

import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.rpg.RpgSubclass;
import com.europa.rpg.util.TaskScheduler;
import com.europa.rpg.util.Targeting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Execução server-side das habilidades. Slot 0 = primária (classe), slot 1 = suprema (subclasse). */
public final class AbilityHandler {
    public static final int PRIMARY = 0;
    public static final int ULTIMATE = 1;

    private AbilityHandler() {}

    public static void tryCast(ServerPlayerEntity player, int slot) {
        if (player.isSpectator() || !player.isAlive()) return;
        PlayerRpgData d = RpgAttachments.get(player);
        if (!d.hasClass()) {
            player.sendMessage(Text.translatable("rpg.europa.no_class").formatted(Formatting.RED), true);
            return;
        }
        String key;
        int cost, cooldown;
        if (slot == PRIMARY) {
            key = "primary";
            cost = d.rpgClass.primaryCost;
            cooldown = d.rpgClass.primaryCooldown;
        } else {
            if (d.subclass == RpgSubclass.NONE) {
                player.sendMessage(Text.translatable("rpg.europa.no_subclass", Progression.SUBCLASS_LEVEL).formatted(Formatting.RED), true);
                return;
            }
            key = "ultimate";
            cost = d.subclass.ultimateCost;
            cooldown = d.subclass.ultimateCooldown;
        }
        long now = player.getServer().getOverworld().getTime();
        long remaining = d.cooldownRemaining(key, now);
        if (remaining > 0) {
            player.sendMessage(Text.translatable("rpg.europa.on_cooldown", String.format("%.1f", remaining / 20f)).formatted(Formatting.RED), true);
            return;
        }
        if (!player.isCreative() && d.mana < cost) {
            player.sendMessage(Text.translatable("rpg.europa.no_mana").formatted(Formatting.BLUE), true);
            return;
        }
        boolean success = slot == PRIMARY ? castPrimary(player, d) : castUltimate(player, d);
        if (success) {
            if (!player.isCreative()) d.mana -= cost;
            d.cooldowns.put(key, now + cooldown);
            Progression.sync(player);
        }
    }

    // ---------------------------------------------------------------- primárias

    private static boolean castPrimary(ServerPlayerEntity p, PlayerRpgData d) {
        float scale = Progression.powerScale(d);
        ServerWorld world = p.getServerWorld();
        return switch (d.rpgClass) {
            case WARRIOR -> {
                // Investida: avanço + golpe em área à frente.
                Vec3d look = p.getRotationVector().multiply(1, 0, 1).normalize();
                p.addVelocity(look.x * 1.8, 0.25, look.z * 1.8);
                p.velocityModified = true;
                sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f);
                TaskScheduler.schedule(6, () -> {
                    if (!p.isAlive()) return;
                    Vec3d center = p.getPos().add(look.multiply(1.5));
                    for (LivingEntity e : Targeting.enemiesAround(p, center, 3.0)) {
                        e.damage(p.getDamageSources().playerAttack(p), 6f * scale);
                        e.takeKnockback(1.2, -look.x, -look.z);
                    }
                    world.spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, p.getY() + 1, center.z, 6, 1, 0.3, 1, 0);
                });
                yield true;
            }
            case MAGE -> {
                // Míssil Arcano: raio mágico instantâneo.
                LivingEntity target = Targeting.lookTarget(p, 32);
                Vec3d end = target != null ? target.getPos().add(0, target.getHeight() / 2, 0) : Targeting.blockHitPos(p, 32);
                Targeting.particleLine(world, ParticleTypes.END_ROD, p.getEyePos().subtract(0, 0.2, 0), end, 0.4);
                if (target != null) {
                    float dmg = 5f * scale;
                    if (d.subclass == RpgSubclass.PYROMANCER) target.setOnFireFor(4);
                    if (d.subclass == RpgSubclass.CRYOMANCER) target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
                    target.damage(p.getDamageSources().indirectMagic(p, p), dmg);
                }
                sound(p, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 1.6f);
                yield true;
            }
            case ROGUE -> {
                // Passo Sombrio: teleporta para trás do alvo.
                LivingEntity target = Targeting.lookTarget(p, 16);
                if (target == null) {
                    p.sendMessage(Text.translatable("rpg.europa.no_target").formatted(Formatting.GRAY), true);
                    yield false;
                }
                Vec3d behind = target.getPos().subtract(target.getRotationVector().multiply(1, 0, 1).normalize().multiply(1.5));
                world.spawnParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 1, p.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
                if (!world.isSpaceEmpty(p, p.getBoundingBox().offset(behind.subtract(p.getPos())))) {
                    behind = target.getPos();
                }
                p.requestTeleport(behind.x, target.getY(), behind.z);
                p.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, target.getEyePos());
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, false, false));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 1, false, true));
                sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.4f);
                yield true;
            }
            case RANGER -> {
                // Saraivada: leque de 5 flechas.
                for (int i = -2; i <= 2; i++) {
                    ArrowEntity arrow = new ArrowEntity(world, p, new ItemStack(Items.ARROW), null);
                    arrow.setVelocity(p, p.getPitch(), p.getYaw() + i * 6f, 0f, 3.0f, 1.0f);
                    arrow.setDamage(2.0 * scale);
                    arrow.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    if (d.subclass == RpgSubclass.SNIPER) arrow.setCritical(true);
                    world.spawnEntity(arrow);
                }
                sound(p, SoundEvents.ENTITY_ARROW_SHOOT, 0.8f);
                yield true;
            }
            case NONE -> false;
        };
    }

    // ---------------------------------------------------------------- supremas

    private static boolean castUltimate(ServerPlayerEntity p, PlayerRpgData d) {
        float scale = Progression.powerScale(d);
        ServerWorld world = p.getServerWorld();
        switch (d.subclass) {
            case BERSERKER -> {
                // Fúria Sangrenta
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 1));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 200, 1));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
                world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 15, 0.6, 0.8, 0.6, 0);
                sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 1.0f);
            }
            case PALADIN -> {
                // Escudo Divino: cura aliados e fere mortos-vivos.
                for (ServerPlayerEntity ally : world.getPlayers(o -> o.squaredDistanceTo(p) < 64)) {
                    ally.heal(8f * scale);
                    ally.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 120, 2));
                    ally.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 120, 1));
                }
                for (LivingEntity e : Targeting.enemiesAround(p, p.getPos(), 8)) {
                    if (e.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) {
                        e.damage(p.getDamageSources().indirectMagic(p, p), 8f * scale);
                        e.setOnFireFor(3);
                    }
                }
                ring(world, p.getPos(), 8, ParticleTypes.END_ROD);
                sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.5f);
            }
            case PYROMANCER -> {
                // Meteoro: impacto atrasado no ponto mirado.
                Vec3d target = Targeting.blockHitPos(p, 40);
                world.spawnParticles(ParticleTypes.FLAME, target.x, target.y + 0.2, target.z, 60, 2, 0.1, 2, 0.01);
                sound(p, SoundEvents.ENTITY_BLAZE_SHOOT, 0.6f);
                for (int i = 0; i < 20; i += 2) {
                    final double h = 20 - i;
                    TaskScheduler.schedule(i, () -> world.spawnParticles(ParticleTypes.LAVA, target.x, target.y + h, target.z, 6, 0.4, 0.4, 0.4, 0));
                }
                TaskScheduler.schedule(20, () -> {
                    world.createExplosion(p, target.x, target.y, target.z, 2.5f, true, World.ExplosionSourceType.NONE);
                    for (LivingEntity e : Targeting.enemiesAround(p, target, 5)) {
                        e.damage(p.getDamageSources().indirectMagic(p, p), 14f * scale);
                        e.setOnFireFor(6);
                    }
                });
            }
            case CRYOMANCER -> {
                // Nevasca
                for (LivingEntity e : Targeting.enemiesAround(p, p.getPos(), 8)) {
                    e.damage(p.getDamageSources().freeze(), 6f * scale);
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 3));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 1));
                    e.setFrozenTicks(240);
                }
                world.spawnParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1.5, p.getZ(), 300, 6, 1.5, 6, 0.05);
                sound(p, SoundEvents.ENTITY_PLAYER_HURT_FREEZE, 0.6f);
            }
            case ASSASSIN -> {
                // Marca da Morte
                LivingEntity target = Targeting.lookTarget(p, 20);
                if (target == null) {
                    p.sendMessage(Text.translatable("rpg.europa.no_target").formatted(Formatting.GRAY), true);
                    return false;
                }
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
                target.damage(p.getDamageSources().indirectMagic(p, p), 12f * scale);
                world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), 15, 0.3, 0.5, 0.3, 0.2);
                sound(p, SoundEvents.ENTITY_WITHER_SHOOT, 1.6f);
            }
            case SHADOWDANCER -> {
                // Bomba de Fumaça
                for (LivingEntity e : Targeting.enemiesAround(p, p.getPos(), 7)) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 120, 0));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2));
                    if (e instanceof net.minecraft.entity.mob.MobEntity mob) mob.setTarget(null);
                }
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 160, 0, false, false));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 160, 2));
                world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX(), p.getY() + 0.5, p.getZ(), 120, 3, 1, 3, 0.02);
                sound(p, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7f);
            }
            case SNIPER -> {
                // Tiro Perfurante
                List<LivingEntity> hits = Targeting.lineTargets(p, 64);
                Vec3d end = Targeting.blockHitPos(p, 64);
                Targeting.particleLine(world, ParticleTypes.CRIT, p.getEyePos().subtract(0, 0.2, 0), end, 0.5);
                for (LivingEntity e : hits) {
                    e.damage(p.getDamageSources().playerAttack(p), 14f * scale);
                }
                sound(p, SoundEvents.ITEM_CROSSBOW_SHOOT, 0.6f);
            }
            case BEASTMASTER -> {
                // Chamado da Matilha: 3 lobos por 60 s.
                for (int i = 0; i < 3; i++) {
                    WolfEntity wolf = EntityType.WOLF.create(world);
                    if (wolf == null) continue;
                    BlockPos pos = p.getBlockPos().add(world.random.nextInt(3) - 1, 0, world.random.nextInt(3) - 1);
                    wolf.refreshPositionAndAngles(pos, p.getYaw(), 0);
                    wolf.setOwner(p);
                    wolf.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 1));
                    wolf.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1200, 0));
                    wolf.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0));
                    wolf.setCustomName(Text.translatable("rpg.europa.spirit_wolf").formatted(Formatting.DARK_GREEN));
                    world.spawnEntity(wolf);
                    world.spawnParticles(ParticleTypes.POOF, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
                    TaskScheduler.schedule(1200, () -> {
                        if (wolf.isAlive()) {
                            world.spawnParticles(ParticleTypes.POOF, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
                            wolf.discard();
                        }
                    });
                }
                sound(p, SoundEvents.ENTITY_WOLF_HOWL, 1.0f);
            }
            case NONE -> {
                return false;
            }
        }
        return true;
    }

    private static void ring(ServerWorld world, Vec3d c, double r, net.minecraft.particle.ParticleEffect particle) {
        for (int i = 0; i < 48; i++) {
            double a = Math.PI * 2 * i / 48;
            world.spawnParticles(particle, c.x + Math.cos(a) * r, c.y + 0.3, c.z + Math.sin(a) * r, 1, 0, 0.1, 0, 0);
        }
    }

    private static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1f, pitch);
    }
}
