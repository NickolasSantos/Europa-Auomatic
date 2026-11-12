package com.europa.rpg.rpg;

import com.europa.rpg.blessing.Blessings;
import com.europa.rpg.blessing.Patron;
import com.europa.rpg.entity.ServantEntity;
import com.europa.rpg.talent.TalentEffect;
import com.europa.rpg.talent.Talents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Regras de combate dos talentos e bênçãos. */
public final class CombatHooks {
    private static final int LAST_STAND_COOLDOWN = 120 * 20;
    /** Alvos marcados: uuid -> (tick final, bônus de dano recebido). */
    private record Mark(long until, float bonus) {}
    private static final java.util.Map<java.util.UUID, Mark> MARKS = new java.util.HashMap<>();

    public static void mark(LivingEntity target, float bonus, int ticks) {
        if (target.getServer() == null) return;
        long now = target.getServer().getOverworld().getTime();
        MARKS.put(target.getUuid(), new Mark(now + ticks, bonus));
    }

    public static void cleanupMarks(long now) {
        MARKS.values().removeIf(m -> m.until < now);
    }

    private CombatHooks() {}

    /** Ajusta o dano antes de ser aplicado (mixin em LivingEntity#damage). */
    public static float modifyIncoming(LivingEntity target, DamageSource source, float amount) {
        if (target.getWorld().isClient() || amount <= 0) return amount;
        if (target instanceof ServerPlayerEntity victim) amount *= com.europa.rpg.race.Races.incomingMultiplier(RpgAttachments.get(victim), source);
        if (!(source.getAttacker() instanceof ServerPlayerEntity p) || p == target) return amount;
        PlayerRpgData d = RpgAttachments.get(p);
        float result = amount;
        com.europa.rpg.race.Race race = com.europa.rpg.race.Races.of(d);
        double crit = Talents.total(d, TalentEffect.CRIT_CHANCE) + Blessings.critBonus(d)
                + com.europa.rpg.item.RarityRoller.equipped(p, "crit") + (race == null ? 0 : race.traits().crit());
        if (crit > 0 && p.getRandom().nextDouble() < crit) {
            result *= 1.5f;
            if (target.getWorld() instanceof ServerWorld w) {
                w.spawnParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getBodyY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
            }
        }
        double execute = Talents.total(d, TalentEffect.EXECUTE);
        if (execute > 0 && target.getHealth() < target.getMaxHealth() * 0.3f) result *= (float) (1 + execute);
        result *= Blessings.damageMultiplier(p, d, target);
        result *= com.europa.rpg.race.Races.damageMultiplier(p, d);
        Mark m = MARKS.get(target.getUuid());
        if (m != null && m.until >= p.getServer().getOverworld().getTime()) result *= 1 + m.bonus;
        return result;
    }

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // Kaelith tier 2: imunidade a dano de queda.
            if (entity instanceof ServerPlayerEntity p && source.isOf(DamageTypes.FALL)
                    && Blessings.has(RpgAttachments.get(p), Patron.THARION, 2)) return false;
            return true;
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseTaken, taken, blocked) -> {
            if (entity instanceof ServerPlayerEntity victim && taken > 0) {
                Progression.markCombat(victim);
                Progression.gain(victim, com.europa.rpg.rpg.Resource.FURY, 4f);
                com.europa.rpg.item.GearProgress.onHurt(victim, taken);
            }
            if (taken <= 0 || !(source.getAttacker() instanceof ServerPlayerEntity p) || p == entity) return;
            PlayerRpgData d = RpgAttachments.get(p);
            Progression.markCombat(p);
            Progression.gain(p, com.europa.rpg.rpg.Resource.FURY, 6f);
            Progression.gain(p, com.europa.rpg.rpg.Resource.OPPORTUNITY, 1f);
            double lifesteal = Talents.total(d, TalentEffect.LIFESTEAL) + com.europa.rpg.item.RarityRoller.equipped(p, "lifesteal");
            if (lifesteal > 0) p.heal((float) (taken * lifesteal));
            // Kaelith tier 3: raio em golpes corpo a corpo.
            if (source.getSource() == p && Blessings.has(d, Patron.THARION, 3) && p.getRandom().nextFloat() < 0.15f
                    && entity.getWorld() instanceof ServerWorld w) {
                LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
                if (bolt != null) {
                    bolt.refreshPositionAfterTeleport(entity.getPos());
                    bolt.setCosmetic(true);
                    w.spawnEntity(bolt);
                }
                entity.timeUntilRegen = 0;
                entity.damage(p.getDamageSources().lightningBolt(), 5f);
            }
            // Invocações atacam o alvo do dono.
            if (entity.isAlive() && !com.europa.rpg.ability.Summons.isSummonOf(entity, p)) com.europa.rpg.ability.Summons.focus(p, entity);
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity p)) return true;
            PlayerRpgData d = RpgAttachments.get(p);
            if (Talents.total(d, TalentEffect.LAST_STAND) <= 0) return true;
            long now = p.getServer().getOverworld().getTime();
            if (d.cooldownRemaining("last_stand", now) > 0) return true;
            d.cooldowns.put("last_stand", now + LAST_STAND_COOLDOWN);
            p.setHealth(4f);
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 3));
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
            p.sendMessage(Text.translatable("rpg.europa.last_stand").formatted(Formatting.GOLD, Formatting.BOLD), true);
            p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1f, 0.8f);
            return false;
        });
    }
}
