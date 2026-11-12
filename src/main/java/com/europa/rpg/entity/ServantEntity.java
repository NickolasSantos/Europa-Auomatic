package com.europa.rpg.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.UUID;

/** Servo morto-vivo do Necromante: luta contra monstros, segue o dono e se desfaz após um tempo. */
public class ServantEntity extends ZombieEntity {
    private UUID ownerUuid;
    private int lifetime = 900;

    public ServantEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 0;
        setCanPickUpLoot(false);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0);
    }

    public void bind(ServerPlayerEntity owner, int lifetimeTicks, float power) {
        this.ownerUuid = owner.getUuid();
        this.lifetime = lifetimeTicks;
        var dmg = getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(4.0 * power);
        setPersistent();
    }

    public static int countOwned(ServerPlayerEntity p) {
        return p.getServerWorld().getEntitiesByClass(ServantEntity.class, p.getBoundingBox().expand(48),
                s -> p.getUuid().equals(s.ownerUuid)).size();
    }

    public PlayerEntity getOwner() {
        return ownerUuid == null ? null : getWorld().getPlayerByUuid(ownerUuid);
    }

    public boolean isOwnedBy(PlayerEntity p) {
        return p != null && p.getUuid().equals(ownerUuid);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        goalSelector.add(3, new FollowOwnerGoal());
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, MobEntity.class, 5, true, false,
                e -> e instanceof Monster && !(e instanceof ServantEntity)));
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (target instanceof PlayerEntity || target instanceof ServantEntity) return false;
        return super.canTarget(target);
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld() instanceof ServerWorld w && --lifetime <= 0) {
            w.spawnParticles(ParticleTypes.SOUL, getX(), getY() + 1, getZ(), 15, 0.3, 0.6, 0.3, 0.02);
            discard();
        }
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (ownerUuid != null) nbt.putUuid("Owner", ownerUuid);
        nbt.putInt("Lifetime", lifetime);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("Owner")) ownerUuid = nbt.getUuid("Owner");
        if (nbt.contains("Lifetime")) lifetime = nbt.getInt("Lifetime"); // invocado por comando/ovo: mantém o padrão
    }

    private class FollowOwnerGoal extends Goal {
        FollowOwnerGoal() {
            setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            PlayerEntity o = getOwner();
            return o != null && getTarget() == null && squaredDistanceTo(o) > 36;
        }

        @Override
        public void tick() {
            PlayerEntity o = getOwner();
            if (o == null) return;
            if (squaredDistanceTo(o) > 400) refreshPositionAndAngles(o.getX(), o.getY(), o.getZ(), getYaw(), getPitch());
            else getNavigation().startMovingTo(o, 1.2);
        }
    }
}
