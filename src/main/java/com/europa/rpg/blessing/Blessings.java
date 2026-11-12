package com.europa.rpg.blessing;

import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.registry.ModBlocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/** Efeitos das bênçãos e lógica de juramento/oferendas. */
public final class Blessings {
    private Blessings() {}

    public static Patron patronOf(PlayerRpgData d) {
        return d.patron.isEmpty() ? null : Patron.byId(d.patron);
    }

    public static int tierOf(PlayerRpgData d) {
        return patronOf(d) == null ? 0 : Patron.tier(d.favor);
    }

    public static boolean has(PlayerRpgData d, Patron p, int tier) {
        return p.id().equals(d.patron) && Patron.tier(d.favor) >= tier;
    }

    /** Chamado a cada segundo. */
    public static void tick(ServerPlayerEntity p, PlayerRpgData d, long now) {
        Patron patron = patronOf(d);
        if (patron == null) return;
        int tier = Patron.tier(d.favor);
        ServerWorld w = p.getServerWorld();
        BlockPos pos = p.getBlockPos();
        switch (patron.id()) {
            case "solenne" -> {
                if (w.isDay() && w.isSkyVisible(pos)) effect(p, StatusEffects.REGENERATION, 60, 0);
            }
            case "morvath" -> {
                effect(p, StatusEffects.NIGHT_VISION, 260, 0);
                if (tier >= 2 && w.isNight()) effect(p, StatusEffects.STRENGTH, 60, 0);
            }
            case "thalor" -> {
                effect(p, StatusEffects.HASTE, 60, 0);
                if (tier >= 3) effect(p, StatusEffects.FIRE_RESISTANCE, 60, 0);
            }
            case "ysmera" -> {
                effect(p, StatusEffects.WATER_BREATHING, 60, 0);
                if (tier >= 2 && p.isTouchingWater()) {
                    effect(p, StatusEffects.DOLPHINS_GRACE, 60, 0);
                    effect(p, StatusEffects.CONDUIT_POWER, 60, 0);
                }
            }
            case "kaelith" -> effect(p, StatusEffects.SPEED, 60, 0);
            case "verdanya" -> {
                if (tier >= 2 && now % 600 == 0) p.getHungerManager().add(1, 0.5f);
                if (tier >= 3) {
                    p.removeStatusEffect(StatusEffects.POISON);
                    p.removeStatusEffect(StatusEffects.WITHER);
                }
            }
            default -> {}
        }
    }

    private static void effect(ServerPlayerEntity p, RegistryEntry<StatusEffect> e, int ticks, int amp) {
        StatusEffectInstance cur = p.getStatusEffect(e);
        if (cur == null || cur.getDuration() < ticks - 20 || cur.getAmplifier() < amp) {
            p.addStatusEffect(new StatusEffectInstance(e, ticks, amp, true, false, true));
        }
    }

    /** Multiplicador de dano causado pelo jogador (tier 3 de alguns patronos). */
    public static float damageMultiplier(ServerPlayerEntity p, PlayerRpgData d, net.minecraft.entity.LivingEntity target) {
        if (has(d, Patron.SOLENNE, 3) && target.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) return 1.3f;
        if (has(d, Patron.MORVATH, 3) && p.getWorld().isNight()) return 1.2f;
        if (has(d, Patron.YSMERA, 3) && p.isTouchingWaterOrRain()) return 1.25f;
        return 1f;
    }

    public static boolean nearShrine(ServerPlayerEntity p) {
        for (BlockPos bp : BlockPos.iterate(p.getBlockPos().add(-5, -3, -5), p.getBlockPos().add(5, 3, 5))) {
            if (p.getWorld().getBlockState(bp).isOf(ModBlocks.SHRINE)) return true;
        }
        return false;
    }

    /** Jura lealdade a um patrono. Trocar de patrono zera o favor. */
    public static boolean pledge(ServerPlayerEntity p, String id) {
        Patron patron = Patron.byId(id);
        if (patron == null) return false;
        if (!nearShrine(p)) {
            p.sendMessage(Text.translatable("rpg.europa.patron.need_shrine").formatted(Formatting.RED), true);
            return false;
        }
        PlayerRpgData d = RpgAttachments.get(p);
        if (id.equals(d.patron)) return false;
        d.patron = id;
        d.favor = 0;
        Progression.applyAttributes(p);
        p.sendMessage(Text.translatable("rpg.europa.patron.pledged", Text.translatable(patron.translationKey()).formatted(patron.color())).formatted(Formatting.GOLD));
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1f, 1f);
        return true;
    }

    /** Oferenda no santuário. Retorna true se o item foi aceito. */
    public static boolean offer(ServerPlayerEntity p, ItemStack stack, boolean wholeStack) {
        PlayerRpgData d = RpgAttachments.get(p);
        Patron patron = patronOf(d);
        if (patron == null) return false;
        Integer value = patron.offerings().get(Registries.ITEM.getId(stack.getItem()));
        if (value == null) {
            p.sendMessage(Text.translatable("rpg.europa.patron.rejected").formatted(Formatting.GRAY), true);
            return false;
        }
        if (d.favor >= Patron.MAX_FAVOR) {
            p.sendMessage(Text.translatable("rpg.europa.patron.max_favor").formatted(Formatting.GRAY), true);
            return false;
        }
        int count = wholeStack ? stack.getCount() : 1;
        int before = Patron.tier(d.favor);
        d.favor = Math.min(Patron.MAX_FAVOR, d.favor + value * count);
        if (!p.isCreative()) stack.decrement(count);
        ServerWorld w = p.getServerWorld();
        w.spawnParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.5, p.getZ(), 20, 0.5, 0.5, 0.5, 0.5);
        w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 1f, 1.2f);
        p.sendMessage(Text.translatable("rpg.europa.patron.favor", d.favor, Patron.MAX_FAVOR).formatted(patron.color()), true);
        int after = Patron.tier(d.favor);
        if (after > before) {
            p.sendMessage(Text.translatable("rpg.europa.patron.tier_up", Text.translatable(patron.translationKey()), after).formatted(Formatting.GOLD, Formatting.BOLD));
            w.playSound(null, p.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1f, 1f);
            Progression.applyAttributes(p);
        }
        return true;
    }
}
