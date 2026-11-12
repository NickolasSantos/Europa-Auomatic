package com.europa.rpg.ability;

import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.util.TaskScheduler;
import com.europa.rpg.util.Targeting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.europa.rpg.ability.Fx.*;

/** Registro de todas as habilidades. Ids referenciados por {@code RpgClass.primaryAbility} e {@code RpgSubclass.ultimateAbility}. */
public final class Abilities {
    private static final Map<String, AbilityDef> ALL = new LinkedHashMap<>();
    private static final int S = 20; // ticks por segundo

    private Abilities() {}

    public static AbilityDef get(String id) {
        return ALL.get(id);
    }

    public static Map<String, AbilityDef> all() {
        return ALL;
    }

    private static void reg(String id, int cost, int cooldown, AbilityDef.Action action) {
        add(new AbilityDef(id, cost, cooldown, action, () -> Text.translatable("rpg.europa.ability." + id + ".desc")));
    }

    static void add(AbilityDef def) {
        if (ALL.containsKey(def.id())) throw new IllegalStateException("Habilidade duplicada: " + def.id());
        ALL.put(def.id(), def);
    }

    static {
        // ============================================================ GUERREIRO
        reg("charge", 20, 8 * S, (p, d, pw) -> {
            ServerWorld w = p.getServerWorld();
            Vec3d look = p.getRotationVector().multiply(1, 0, 1).normalize();
            p.addVelocity(look.x * 1.8, 0.25, look.z * 1.8);
            p.velocityModified = true;
            sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f);
            TaskScheduler.schedule(6, () -> {
                if (!p.isAlive()) return;
                Vec3d c = p.getPos().add(look.multiply(1.5));
                for (LivingEntity e : enemies(p, c, 3)) {
                    e.damage(p.getDamageSources().playerAttack(p), 6f * pw);
                    e.takeKnockback(1.2, -look.x, -look.z);
                }
                w.spawnParticles(ParticleTypes.SWEEP_ATTACK, c.x, p.getY() + 1, c.z, 6, 1, 0.3, 1, 0);
            });
            return true;
        });
        reg("blood_rage", 40, 45 * S, (p, d, pw) -> {
            buff(p, StatusEffects.STRENGTH, 10 * S, 1);
            buff(p, StatusEffects.SPEED, 10 * S, 1);
            buff(p, StatusEffects.HASTE, 10 * S, 1);
            buff(p, StatusEffects.REGENERATION, 10 * S, 0);
            p.getServerWorld().spawnParticles(ParticleTypes.ANGRY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 15, 0.6, 0.8, 0.6, 0);
            sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 1.0f);
            return true;
        });
        reg("divine_shield", 50, 60 * S, (p, d, pw) -> {
            for (ServerPlayerEntity a : allies(p, 8)) {
                a.heal(8f * pw);
                buff(a, StatusEffects.RESISTANCE, 6 * S, 2);
                buff(a, StatusEffects.REGENERATION, 6 * S, 1);
            }
            for (LivingEntity e : enemies(p, p.getPos(), 8)) {
                if (isUndead(e)) {
                    e.damage(p.getDamageSources().indirectMagic(p, p), 8f * pw);
                    e.setOnFireFor(3);
                }
            }
            ring(p.getServerWorld(), p.getPos(), 8, ParticleTypes.END_ROD);
            sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.5f);
            return true;
        });

        // ============================================================ MAGO
        reg("arcane_missile", 15, 30, (p, d, pw) -> {
            LivingEntity t = Targeting.lookTarget(p, 32);
            Vec3d end = t != null ? t.getPos().add(0, t.getHeight() / 2, 0) : Targeting.blockHitPos(p, 32);
            Targeting.particleLine(p.getServerWorld(), ParticleTypes.END_ROD, p.getEyePos().subtract(0, 0.2, 0), end, 0.4);
            if (t != null) {
                if (d.subclass.id.equals("pyromancer")) t.setOnFireFor(4);
                if (d.subclass.id.equals("cryomancer")) t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
                t.damage(p.getDamageSources().indirectMagic(p, p), 5f * pw);
            }
            sound(p, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 1.6f);
            return true;
        });
        reg("meteor", 60, 30 * S, (p, d, pw) -> {
            ServerWorld w = p.getServerWorld();
            Vec3d t = Targeting.blockHitPos(p, 40);
            w.spawnParticles(ParticleTypes.FLAME, t.x, t.y + 0.2, t.z, 60, 2, 0.1, 2, 0.01);
            sound(p, SoundEvents.ENTITY_BLAZE_SHOOT, 0.6f);
            for (int i = 0; i < 20; i += 2) {
                final double h = 20 - i;
                TaskScheduler.schedule(i, () -> w.spawnParticles(ParticleTypes.LAVA, t.x, t.y + h, t.z, 6, 0.4, 0.4, 0.4, 0));
            }
            TaskScheduler.schedule(20, () -> {
                w.createExplosion(p, t.x, t.y, t.z, 2.5f, true, World.ExplosionSourceType.NONE);
                for (LivingEntity e : enemies(p, t, 5)) {
                    e.damage(p.getDamageSources().indirectMagic(p, p), 14f * pw);
                    e.setOnFireFor(6);
                }
            });
            return true;
        });
        reg("blizzard", 50, 30 * S, (p, d, pw) -> {
            for (LivingEntity e : enemies(p, p.getPos(), 8)) {
                e.damage(p.getDamageSources().indirectMagic(p, p), 6f * pw);
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 6 * S, 3));
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 6 * S, 1));
                e.setFrozenTicks(240);
            }
            p.getServerWorld().spawnParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1.5, p.getZ(), 300, 6, 1.5, 6, 0.05);
            sound(p, SoundEvents.ENTITY_PLAYER_HURT_FREEZE, 0.6f);
            return true;
        });

        // ============================================================ LADINO
        reg("shadow_step", 25, 10 * S, (p, d, pw) -> {
            LivingEntity t = Targeting.lookTarget(p, 16);
            if (t == null) return noTarget(p);
            ServerWorld w = p.getServerWorld();
            Vec3d behind = t.getPos().subtract(t.getRotationVector().multiply(1, 0, 1).normalize().multiply(1.5));
            w.spawnParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 1, p.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
            if (!w.isSpaceEmpty(p, p.getBoundingBox().offset(behind.subtract(p.getPos())))) behind = t.getPos();
            p.requestTeleport(behind.x, t.getY(), behind.z);
            p.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, t.getEyePos());
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, false, false));
            buff(p, StatusEffects.STRENGTH, 60, 1);
            sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.4f);
            return true;
        });
        reg("death_mark", 40, 25 * S, (p, d, pw) -> {
            LivingEntity t = Targeting.lookTarget(p, 20);
            if (t == null) return noTarget(p);
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
            t.damage(p.getDamageSources().indirectMagic(p, p), 12f * pw);
            p.getServerWorld().spawnParticles(ParticleTypes.DAMAGE_INDICATOR, t.getX(), t.getBodyY(0.5), t.getZ(), 15, 0.3, 0.5, 0.3, 0.2);
            sound(p, SoundEvents.ENTITY_WITHER_SHOOT, 1.6f);
            return true;
        });
        reg("smoke_bomb", 40, 40 * S, (p, d, pw) -> {
            for (LivingEntity e : enemies(p, p.getPos(), 7)) {
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 6 * S, 0));
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 6 * S, 2));
                if (e instanceof MobEntity mob) mob.setTarget(null);
            }
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 8 * S, 0, false, false));
            buff(p, StatusEffects.SPEED, 8 * S, 2);
            p.getServerWorld().spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX(), p.getY() + 0.5, p.getZ(), 120, 3, 1, 3, 0.02);
            sound(p, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7f);
            return true;
        });

        // ============================================================ PATRULHEIRO
        reg("volley", 25, 8 * S, (p, d, pw) -> {
            ServerWorld w = p.getServerWorld();
            for (int i = -2; i <= 2; i++) {
                ArrowEntity a = new ArrowEntity(w, p, new ItemStack(Items.ARROW), null);
                a.setVelocity(p, p.getPitch(), p.getYaw() + i * 6f, 0f, 3.0f, 1.0f);
                a.setDamage(2.0 * pw);
                a.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                if (d.subclass.id.equals("sniper")) a.setCritical(true);
                w.spawnEntity(a);
            }
            sound(p, SoundEvents.ENTITY_ARROW_SHOOT, 0.8f);
            return true;
        });
        reg("piercing_shot", 45, 25 * S, (p, d, pw) -> {
            List<LivingEntity> hits = Targeting.lineTargets(p, 64);
            Targeting.particleLine(p.getServerWorld(), ParticleTypes.CRIT, p.getEyePos().subtract(0, 0.2, 0), Targeting.blockHitPos(p, 64), 0.5);
            for (LivingEntity e : hits) e.damage(p.getDamageSources().playerAttack(p), 14f * pw);
            sound(p, SoundEvents.ITEM_CROSSBOW_SHOOT, 0.6f);
            return true;
        });
        reg("call_of_the_pack", 60, 90 * S, (p, d, pw) -> {
            if (Summons.summon(p, Summons.Kind.WOLF, 3, 60 * S, pw, true) == 0) return false;
            sound(p, SoundEvents.ENTITY_WOLF_HOWL, 1.0f);
            return true;
        });

        // ============================================================ CLÉRIGO
        reg("restoring_light", 25, 8 * S, (p, d, pw) -> {
            for (ServerPlayerEntity a : allies(p, 8)) {
                a.heal(4f * pw);
                a.removeStatusEffect(StatusEffects.POISON);
                a.removeStatusEffect(StatusEffects.WITHER);
                a.removeStatusEffect(StatusEffects.WEAKNESS);
                p.getServerWorld().spawnParticles(ParticleTypes.HAPPY_VILLAGER, a.getX(), a.getY() + 1, a.getZ(), 10, 0.4, 0.6, 0.4, 0);
            }
            sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.4f);
            return true;
        });
        reg("sanctuary", 60, 45 * S, (p, d, pw) -> {
            ServerWorld w = p.getServerWorld();
            Vec3d c = p.getPos();
            for (int i = 0; i < 10; i++) {
                TaskScheduler.schedule(i * S, () -> {
                    ring(w, c, 6, ParticleTypes.END_ROD);
                    for (ServerPlayerEntity a : w.getPlayers(o -> o.squaredDistanceTo(c) <= 36)) a.heal(2f * pw);
                    if (p.isAlive()) {
                        for (LivingEntity e : enemies(p, c, 6)) {
                            if (isUndead(e)) e.damage(p.getDamageSources().indirectMagic(p, p), 3f * pw);
                        }
                    }
                });
            }
            sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.2f);
            return true;
        });
        reg("judgment", 40, 20 * S, (p, d, pw) -> {
            LivingEntity t = Targeting.lookTarget(p, 24);
            if (t == null) return noTarget(p);
            LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(p.getServerWorld());
            if (bolt != null) {
                bolt.refreshPositionAfterTeleport(t.getPos());
                bolt.setCosmetic(true);
                p.getServerWorld().spawnEntity(bolt);
            }
            t.damage(p.getDamageSources().indirectMagic(p, p), (isUndead(t) ? 16f : 8f) * pw);
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 10 * S, 0));
            return true;
        });

        // ============================================================ NECROMANTE
        reg("raise_servant", 30, 15 * S, (p, d, pw) -> Summons.summon(p, Summons.Kind.SERVANT, 1, 45 * S, pw, false) > 0);
        reg("unholy_legion", 70, 60 * S, (p, d, pw) -> {
            if (Summons.summon(p, Summons.Kind.SERVANT, 4, 30 * S, pw, true) == 0) return false;
            sound(p, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 0.7f);
            return true;
        });
        reg("blood_pact", 40, 25 * S, (p, d, pw) -> {
            float total = 0;
            for (LivingEntity e : enemies(p, p.getPos(), 6)) {
                float dmg = 4f * pw;
                if (e.damage(p.getDamageSources().indirectMagic(p, p), dmg)) total += dmg;
                Targeting.particleLine(p.getServerWorld(), net.minecraft.particle.DustParticleEffect.DEFAULT, e.getPos().add(0, 1, 0), p.getPos().add(0, 1, 0), 0.5);
            }
            if (total <= 0) return noTarget(p);
            p.heal(total * 0.5f);
            sound(p, SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.2f);
            return true;
        });

        // ============================================================ DRUIDA
        reg("thorn_snare", 25, 10 * S, (p, d, pw) -> {
            LivingEntity t = Targeting.lookTarget(p, 24);
            if (t == null) return noTarget(p);
            root(t, 3 * S);
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 4 * S, 1));
            t.damage(p.getDamageSources().indirectMagic(p, p), 3f * pw);
            p.getServerWorld().spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, t.getX(), t.getY() + 0.5, t.getZ(), 30, 0.5, 0.5, 0.5, 0);
            sound(p, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PLACE, 0.8f);
            return true;
        });
        reg("beast_form", 50, 50 * S, (p, d, pw) -> {
            int t = 20 * S;
            buff(p, StatusEffects.HEALTH_BOOST, t, 1);
            p.heal(8f);
            buff(p, StatusEffects.STRENGTH, t, 1);
            buff(p, StatusEffects.RESISTANCE, t, 0);
            buff(p, StatusEffects.SPEED, t, 0);
            p.getServerWorld().spawnParticles(ParticleTypes.COMPOSTER, p.getX(), p.getY() + 1, p.getZ(), 40, 0.6, 1, 0.6, 0);
            sound(p, SoundEvents.ENTITY_POLAR_BEAR_WARNING, 0.8f);
            return true;
        });
        reg("ancestral_roots", 55, 40 * S, (p, d, pw) -> {
            for (LivingEntity e : enemies(p, p.getPos(), 8)) {
                root(e, 4 * S);
                e.damage(p.getDamageSources().indirectMagic(p, p), 4f * pw);
            }
            for (ServerPlayerEntity a : allies(p, 8)) buff(a, StatusEffects.REGENERATION, 8 * S, 1);
            ring(p.getServerWorld(), p.getPos(), 8, ParticleTypes.COMPOSTER);
            sound(p, SoundEvents.BLOCK_ROOTS_PLACE, 0.6f);
            return true;
        });

        // ============================================================ MONGE
        reg("seismic_palm", 20, 6 * S, (p, d, pw) -> {
            LivingEntity t = Targeting.lookTarget(p, 4.5);
            if (t == null) return noTarget(p);
            float dmg = 6f * pw * (p.getMainHandStack().isEmpty() ? 1.5f : 1f);
            t.damage(p.getDamageSources().playerAttack(p), dmg);
            Vec3d dir = t.getPos().subtract(p.getPos()).normalize();
            t.takeKnockback(2.5, -dir.x, -dir.z);
            p.getServerWorld().spawnParticles(ParticleTypes.EXPLOSION, t.getX(), t.getBodyY(0.5), t.getZ(), 1, 0, 0, 0, 0);
            sound(p, SoundEvents.ENTITY_IRON_GOLEM_ATTACK, 1.2f);
            return true;
        });
        reg("hundred_strikes", 45, 20 * S, (p, d, pw) -> {
            LivingEntity t = Targeting.lookTarget(p, 5);
            if (t == null) return noTarget(p);
            for (int i = 0; i < 6; i++) {
                TaskScheduler.schedule(i * 3 + 1, () -> {
                    if (!t.isAlive() || !p.isAlive() || t.squaredDistanceTo(p) > 49) return;
                    t.timeUntilRegen = 0;
                    t.damage(p.getDamageSources().playerAttack(p), 2.5f * pw);
                    p.getServerWorld().spawnParticles(ParticleTypes.CRIT, t.getX(), t.getBodyY(0.6), t.getZ(), 8, 0.3, 0.3, 0.3, 0.2);
                    sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1.5f);
                });
            }
            return true;
        });
        reg("wind_step", 35, 25 * S, (p, d, pw) -> {
            Vec3d look = p.getRotationVector().multiply(1, 0, 1).normalize();
            p.addVelocity(look.x * 2.5, 0.6, look.z * 2.5);
            p.velocityModified = true;
            buff(p, StatusEffects.SPEED, 8 * S, 2);
            buff(p, StatusEffects.SLOW_FALLING, 8 * S, 0);
            buff(p, StatusEffects.JUMP_BOOST, 8 * S, 1);
            p.getServerWorld().spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.5, p.getZ(), 30, 0.5, 0.3, 0.5, 0.05);
            sound(p, SoundEvents.ENTITY_BREEZE_JUMP, 1.0f);
            return true;
        });

        // ============================================================ BARDO
        reg("song_of_valor", 30, 20 * S, (p, d, pw) -> {
            int t = (int) (10 * S * pw);
            for (ServerPlayerEntity a : allies(p, 10)) {
                buff(a, StatusEffects.STRENGTH, t, 0);
                buff(a, StatusEffects.SPEED, t, 0);
            }
            p.getServerWorld().spawnParticles(ParticleTypes.NOTE, p.getX(), p.getY() + 2, p.getZ(), 20, 2, 0.5, 2, 1);
            sound(p, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.2f);
            return true;
        });
        reg("restoring_ballad", 50, 40 * S, (p, d, pw) -> {
            for (ServerPlayerEntity a : allies(p, 12)) {
                buff(a, StatusEffects.REGENERATION, 10 * S, 1);
                buff(a, StatusEffects.ABSORPTION, 30 * S, 1);
            }
            p.getServerWorld().spawnParticles(ParticleTypes.NOTE, p.getX(), p.getY() + 2, p.getZ(), 40, 3, 1, 3, 1);
            sound(p, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1.0f);
            return true;
        });
        reg("dissonant_chord", 45, 30 * S, (p, d, pw) -> {
            List<LivingEntity> foes = enemies(p, p.getPos(), 10);
            if (foes.isEmpty()) return noTarget(p);
            for (LivingEntity e : foes) {
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 8 * S, 1));
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 8 * S, 1));
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 8 * S, 0));
                e.damage(p.getDamageSources().indirectMagic(p, p), 3f * pw);
                if (e instanceof MobEntity mob && foes.size() > 1) {
                    LivingEntity other = foes.get((foes.indexOf(e) + 1) % foes.size());
                    mob.setTarget(other); // inimigos se voltam uns contra os outros
                }
            }
            sound(p, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.5f);
            return true;
        });

        // ============================================================ ALQUIMISTA
        reg("volatile_flask", 20, 5 * S, (p, d, pw) -> {
            ItemStack flask = new ItemStack(Items.SPLASH_POTION);
            flask.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0x7FBF3F), List.of(
                    new StatusEffectInstance(StatusEffects.POISON, (int) (6 * S * pw), 1),
                    new StatusEffectInstance(StatusEffects.SLOWNESS, 4 * S, 1),
                    new StatusEffectInstance(StatusEffects.WEAKNESS, 6 * S, 0))));
            PotionEntity potion = new PotionEntity(p.getServerWorld(), p);
            potion.setItem(flask);
            potion.setVelocity(p, p.getPitch(), p.getYaw(), -15f, 0.9f, 0.5f);
            p.getServerWorld().spawnEntity(potion);
            sound(p, SoundEvents.ENTITY_SPLASH_POTION_THROW, 0.8f);
            return true;
        });
        reg("toxic_cloud", 50, 30 * S, (p, d, pw) -> {
            Vec3d t = Targeting.blockHitPos(p, 24);
            AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(p.getServerWorld(), t.x, t.y, t.z);
            cloud.setOwner(p);
            cloud.setRadius(4.5f);
            cloud.setDuration(10 * S);
            cloud.setWaitTime(5);
            cloud.setRadiusGrowth(0f);
            cloud.setPotionContents(new PotionContentsComponent(Optional.empty(), Optional.of(0x3F7F1F), List.of(
                    new StatusEffectInstance(StatusEffects.POISON, 5 * S, 2),
                    new StatusEffectInstance(StatusEffects.WITHER, 4 * S, 0))));
            p.getServerWorld().spawnEntity(cloud);
            sound(p, SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP, 0.6f);
            return true;
        });
        reg("perfect_elixir", 50, 50 * S, (p, d, pw) -> {
            int t = 20 * S;
            buff(p, StatusEffects.ABSORPTION, t, 2);
            buff(p, StatusEffects.REGENERATION, 10 * S, 1);
            buff(p, StatusEffects.FIRE_RESISTANCE, t, 0);
            buff(p, StatusEffects.STRENGTH, t, 0);
            buff(p, StatusEffects.SPEED, t, 0);
            sound(p, SoundEvents.ENTITY_GENERIC_DRINK, 1.0f);
            return true;
        });

        // ============================================================ RUNEIRO
        reg("explosive_rune", 25, 8 * S, (p, d, pw) -> {
            ServerWorld w = p.getServerWorld();
            Vec3d t = Targeting.blockHitPos(p, 16);
            final boolean[] fired = {false};
            for (int i = 0; i < 20 * S; i += 5) {
                TaskScheduler.schedule(i + 1, () -> {
                    if (fired[0]) return;
                    w.spawnParticles(ParticleTypes.ENCHANT, t.x, t.y + 0.2, t.z, 6, 0.4, 0.05, 0.4, 0);
                    if (!enemies(p, t, 1.8).isEmpty()) {
                        fired[0] = true;
                        w.createExplosion(p, t.x, t.y, t.z, 1.5f, false, World.ExplosionSourceType.NONE);
                        for (LivingEntity e : enemies(p, t, 3.5)) e.damage(p.getDamageSources().indirectMagic(p, p), 8f * pw);
                    }
                });
            }
            sound(p, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f);
            return true;
        });
        reg("runic_armor", 45, 40 * S, (p, d, pw) -> {
            buff(p, StatusEffects.RESISTANCE, 15 * S, 1);
            buff(p, StatusEffects.ABSORPTION, 15 * S, 2);
            p.getServerWorld().spawnParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1, p.getZ(), 60, 0.6, 1, 0.6, 0.5);
            sound(p, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(), 0.8f);
            return true;
        });
        reg("rune_turret", 60, 40 * S, (p, d, pw) -> {
            ServerWorld w = p.getServerWorld();
            Vec3d base = p.getPos().add(0, 2.2, 0);
            for (int i = 0; i < 15 * S; i += 10) {
                TaskScheduler.schedule(i + 1, () -> {
                    w.spawnParticles(ParticleTypes.END_ROD, base.x, base.y, base.z, 4, 0.1, 0.3, 0.1, 0);
                    if (!p.isAlive()) return;
                    List<LivingEntity> foes = enemies(p, base, 12);
                    if (foes.isEmpty()) return;
                    LivingEntity t = foes.stream().min(java.util.Comparator.comparingDouble(e -> e.squaredDistanceTo(base))).get();
                    Targeting.particleLine(w, ParticleTypes.ELECTRIC_SPARK, base, t.getPos().add(0, t.getHeight() / 2, 0), 0.4);
                    t.timeUntilRegen = 0;
                    t.damage(p.getDamageSources().indirectMagic(p, p), 3f * pw);
                });
            }
            sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.8f);
            return true;
        });

        // ============================================================ TECELÃO DO VAZIO
        reg("void_rift", 20, 6 * S, (p, d, pw) -> {
            double dist = Math.max(0, Targeting.blockDistance(p, 10) - 1);
            if (dist < 1) return false;
            Vec3d to = p.getPos().add(p.getRotationVector().multiply(dist));
            ServerWorld w = p.getServerWorld();
            w.spawnParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 40, 0.4, 0.8, 0.4, 0.5);
            p.requestTeleport(to.x, to.y, to.z);
            p.fallDistance = 0;
            w.spawnParticles(ParticleTypes.REVERSE_PORTAL, to.x, to.y + 1, to.z, 40, 0.4, 0.8, 0.4, 0.1);
            sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.7f);
            return true;
        });
        reg("stellar_collapse", 60, 35 * S, (p, d, pw) -> {
            ServerWorld w = p.getServerWorld();
            Vec3d c = Targeting.blockHitPos(p, 30).add(0, 1, 0);
            for (int i = 0; i < 20; i += 2) {
                TaskScheduler.schedule(i + 1, () -> {
                    w.spawnParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, 30, 2, 1, 2, 0.2);
                    for (LivingEntity e : enemies(p, c, 8)) {
                        Vec3d pull = c.subtract(e.getPos()).normalize().multiply(0.35);
                        e.addVelocity(pull.x, pull.y * 0.5 + 0.05, pull.z);
                        e.velocityModified = true;
                    }
                });
            }
            TaskScheduler.schedule(22, () -> {
                for (LivingEntity e : enemies(p, c, 4)) e.damage(p.getDamageSources().indirectMagic(p, p), 12f * pw);
                w.spawnParticles(ParticleTypes.FLASH, c.x, c.y, c.z, 1, 0, 0, 0, 0);
                w.playSound(null, BlockPos.ofFloored(c), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, net.minecraft.sound.SoundCategory.PLAYERS, 1f, 1.3f);
            });
            return true;
        });
        reg("devouring_rift", 45, 30 * S, (p, d, pw) -> {
            ServerWorld w = p.getServerWorld();
            for (int i = 0; i < 6; i++) {
                TaskScheduler.schedule(i * 10 + 1, () -> {
                    if (!p.isAlive()) return;
                    ring(w, p.getPos(), 6, ParticleTypes.SQUID_INK);
                    for (LivingEntity e : enemies(p, p.getPos(), 6)) {
                        e.timeUntilRegen = 0;
                        if (e.damage(p.getDamageSources().indirectMagic(p, p), 3f * pw)) d.mana += 3;
                    }
                });
            }
            sound(p, SoundEvents.ENTITY_WARDEN_AMBIENT, 1.4f);
            return true;
        });
    }

}
