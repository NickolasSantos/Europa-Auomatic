package com.europa.rpg.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

/**
 * Senhor da Catacumba — chefe da dungeon.
 * Fase 1: combate corpo a corpo + Onda de Almas periódica.
 * Fase 2 (&lt;50% de vida): fica mais rápido e invoca esqueletos.
 */
public class CatacombLordEntity extends WitherSkeletonEntity {
    private final ServerBossBar bossBar = new ServerBossBar(getDisplayName(), BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10);
    private int novaTimer = 120;
    private int summonTimer = 60;
    private boolean enraged;

    public CatacombLordEntity(EntityType<? extends WitherSkeletonEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 200;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 350.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.GENERIC_ARMOR, 12.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.85)
                .add(EntityAttributes.GENERIC_SCALE, 1.6);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        equipStack(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        for (EquipmentSlot slot : EquipmentSlot.values()) setEquipmentDropChance(slot, 0f);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        bossBar.setPercent(getHealth() / getMaxHealth());
        if (!(getWorld() instanceof ServerWorld world) || getTarget() == null) return;

        if (!enraged && getHealth() < getMaxHealth() / 2) {
            enraged = true;
            addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 0));
            addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 0));
            bossBar.setColor(BossBar.Color.RED);
            world.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2f, 0.6f);
            for (ServerPlayerEntity p : bossBar.getPlayers()) {
                p.sendMessage(Text.translatable("entity.europa.catacomb_lord.enraged"), true);
            }
        }

        if (--novaTimer <= 0) {
            novaTimer = enraged ? 100 : 160;
            soulNova(world);
        }
        if (enraged && --summonTimer <= 0) {
            summonTimer = 300;
            summonMinions(world);
        }
    }

    private void soulNova(ServerWorld world) {
        for (PlayerEntity p : world.getEntitiesByClass(PlayerEntity.class, getBoundingBox().expand(7), pl -> !pl.isCreative() && !pl.isSpectator())) {
            p.damage(getDamageSources().indirectMagic(this, this), enraged ? 9f : 6f);
            Vec3d push = p.getPos().subtract(getPos()).normalize().multiply(1.4);
            p.addVelocity(push.x, 0.5, push.z);
            p.velocityModified = true;
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
        }
        for (int i = 0; i < 64; i++) {
            double a = Math.PI * 2 * i / 64;
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, getX() + Math.cos(a) * 6, getY() + 0.5, getZ() + Math.sin(a) * 6, 2, 0, 0.2, 0, 0.01);
        }
        world.playSound(null, getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 1.2f, 0.7f);
    }

    private void summonMinions(ServerWorld world) {
        for (int i = 0; i < 3; i++) {
            var minion = i == 0 ? EntityType.WITHER_SKELETON.create(world) : EntityType.SKELETON.create(world);
            if (minion == null) continue;
            double ox = (random.nextDouble() - 0.5) * 6, oz = (random.nextDouble() - 0.5) * 6;
            minion.refreshPositionAndAngles(getX() + ox, getY(), getZ() + oz, random.nextFloat() * 360, 0);
            minion.initialize(world, world.getLocalDifficulty(minion.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
            if (minion instanceof SkeletonEntity || minion instanceof WitherSkeletonEntity) minion.setTarget(getTarget());
            world.spawnEntity(minion);
            world.spawnParticles(ParticleTypes.SOUL, minion.getX(), minion.getY() + 1, minion.getZ(), 15, 0.3, 0.6, 0.3, 0.02);
        }
        world.playSound(null, getBlockPos(), SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.HOSTILE, 1.5f, 0.8f);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        // Imune a dano de queda e sufocamento: evita mortes "baratas" na arena.
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.FALL) || source.isOf(net.minecraft.entity.damage.DamageTypes.IN_WALL)) return false;
        return super.damage(source, amount);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void setCustomName(Text name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        enraged = nbt.getBoolean("Enraged");
        if (hasCustomName()) bossBar.setName(getDisplayName());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Enraged", enraged);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean isAffectedBySplashPotions() {
        return false;
    }
}
