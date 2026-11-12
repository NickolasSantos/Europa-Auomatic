package com.europa.rpg.race;

import com.europa.rpg.ability.Spell;
import com.europa.rpg.rpg.PlayerRpgData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.europa.rpg.race.Race.Traits.b;
import static net.minecraft.entity.effect.StatusEffects.*;
import static net.minecraft.util.Formatting.*;

/** Catálogo das 18 raças, passivas e habilidades raciais. */
public final class Races {
    public static final int RACIAL_COST = 0;
    /** Posições recentes (1 por segundo) para a habilidade "Retroceder" do Horólogo. */
    private static final Map<UUID, Deque<Vec3d>> HISTORY = new HashMap<>();
    private static boolean done;

    private Races() {}

    public static Race of(PlayerRpgData d) {
        return d.race.isEmpty() ? null : Race.byId(d.race);
    }

    public static synchronized void init() {
        if (done) return;
        done = true;
        // Habilidades raciais (sem custo de recurso; recarga longa)
        Spell.of("determination", 0, 90).heal(6).buff(RESISTANCE, 6, 0).sound(SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), 1.2f).register();
        Spell.of("elven_grace", 0, 45).buff(SPEED, 8, 1).buff(JUMP_BOOST, 8, 1).sound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.4f).register();
        Spell.of("stoneskin", 0, 60).buff(RESISTANCE, 8, 1).sound(SoundEvents.BLOCK_STONE_PLACE, 0.6f).register();
        Spell.of("war_cry", 0, 60).buff(STRENGTH, 8, 1).around(6).status(WEAKNESS, 6, 0).particle(ParticleTypes.ANGRY_VILLAGER).sound(SoundEvents.ENTITY_RAVAGER_ROAR, 1.3f).register();
        Spell.of("infernal_rebuke", 0, 30).around(4).magic(4).ignite(4).particle(ParticleTypes.FLAME).sound(SoundEvents.ENTITY_BLAZE_SHOOT, 0.8f).register();
        Spell.of("breath_of_ancestors", 0, 30).areaAt(6, 3).magic(6).ignite(3).particle(ParticleTypes.FLAME).sound(SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, 1.2f).register();
        Spell.of("lucky_escape", 0, 60).buff(INVISIBILITY, 5, 0).buff(SPEED, 5, 1).vanish(12).sound(SoundEvents.ENTITY_FOX_SNIFF, 1.4f).register();
        Spell.of("fey_blink", 0, 15).blink(8).sound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.6f).register();
        Spell.of("deathless", 0, 90).buff(ABSORPTION, 10, 2).sound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1.2f).register();
        Spell.of("prismatic_flash", 0, 40).around(6).status(BLINDNESS, 4, 0).particle(ParticleTypes.END_ROD).sound(SoundEvents.BLOCK_GLASS_BREAK, 1.4f).register();
        Spell.of("spore_cloud", 0, 40).around(5).status(POISON, 5, 1).status(NAUSEA, 5, 0).particle(ParticleTypes.SPORE_BLOSSOM_AIR).sound(SoundEvents.BLOCK_FUNGUS_BREAK, 0.6f).register();
        Spell.of("shadow_meld", 0, 45).buff(INVISIBILITY, 6, 0).vanish(10).sound(SoundEvents.ENTITY_PHANTOM_FLAP, 0.8f).register();
        Spell.of("glaze", 0, 45).buff(RESISTANCE, 4, 2).sound(SoundEvents.BLOCK_DECORATED_POT_PLACE, 1.2f).register();
        Spell.of("tidal_surge", 0, 25).dash(2).around(3).magic(4).knockback(2).delay(6).particle(ParticleTypes.SPLASH).sound(SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1f).register();
        Spell.of("photosynthesis", 0, 60).heal(8).buff(REGENERATION, 5, 1).sound(SoundEvents.BLOCK_AZALEA_LEAVES_PLACE, 1f).register();
        Spell.of("rewind", 0, 45).heal(4).extra("rpg.europa.fx.rewind", (p, d, t, pw) -> {
            Deque<Vec3d> h = HISTORY.get(p.getUuid());
            if (h == null || h.isEmpty()) return;
            Vec3d back = h.peekFirst();
            p.getServerWorld().spawnParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.1);
            p.requestTeleport(back.x, back.y, back.z);
            p.fallDistance = 0;
        }).sound(SoundEvents.BLOCK_BELL_USE, 1.6f).register();
        Spell.of("ink_splash", 0, 30).around(6).status(BLINDNESS, 4, 0).status(SLOWNESS, 4, 1).particle(ParticleTypes.SQUID_INK).sound(SoundEvents.ENTITY_SQUID_SQUIRT, 1f).register();
        Spell.of("starfall", 0, 30).areaAt(20, 3).magic(7).status(GLOWING, 10, 0).particle(ParticleTypes.END_ROD).sound(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f).register();

        // Raças: escala, traços (positivos e limitações), habilidade
        Race.register("human", WHITE, 1.0, b().xp(0.10).build(), "determination");
        Race.register("elf", GREEN, 1.0, b().speed(0.01).crit(0.03).health(-2).build(), "elven_grace");
        Race.register("dwarf", GOLD, 0.85, b().health(4).armor(2).speed(-0.005).build(), "stoneskin");
        Race.register("orc", DARK_GREEN, 1.08, b().health(2).damage(1.5).mana(-20).build(), "war_cry");
        Race.register("tiefling", RED, 1.0, b().mana(15).health(-2).build(), "infernal_rebuke");
        Race.register("dragonborn", DARK_RED, 1.1, b().health(2).armor(2).speed(-0.005).build(), "breath_of_ancestors");
        Race.register("halfling", YELLOW, 0.7, b().luck(2).speed(0.01).health(-4).build(), "lucky_escape");
        Race.register("feyborn", LIGHT_PURPLE, 0.9, b().mana(30).manaRegen(0.5f).armor(-2).build(), "fey_blink");
        Race.register("reborn", GRAY, 1.0, b().health(-2).build(), "deathless");
        Race.register("vitrean", AQUA, 1.0, b().crit(0.05).manaRegen(0.5f).armor(-4).build(), "prismatic_flash");
        Race.register("mycelian", DARK_PURPLE, 0.95, b().health(2).build(), "spore_cloud");
        Race.register("umbral", DARK_GRAY, 1.0, b().build(), "shadow_meld");
        Race.register("porcelain", WHITE, 1.0, b().toughness(3).mana(20).health(-4).build(), "glaze");
        Race.register("abyssal", DARK_AQUA, 1.0, b().health(-2).build(), "tidal_surge");
        Race.register("verdan", GREEN, 1.0, b().luck(1).build(), "photosynthesis");
        Race.register("horologist", GOLD, 1.0, b().cooldown(0.08).speed(-0.005).build(), "rewind");
        Race.register("inkling", BLUE, 0.9, b().mana(15).armor(-2).build(), "ink_splash");
        Race.register("astrarian", LIGHT_PURPLE, 1.0, b().power(0.05).manaRegen(0.5f).health(-2).build(), "starfall");
    }

    /** Passivas raciais — a cada segundo. */
    public static void tick(ServerPlayerEntity p, PlayerRpgData d) {
        Race race = of(d);
        if (race == null) return;
        Deque<Vec3d> h = HISTORY.computeIfAbsent(p.getUuid(), k -> new ArrayDeque<>());
        h.addLast(p.getPos());
        while (h.size() > 5) h.removeFirst();

        ServerWorld w = p.getServerWorld();
        BlockPos pos = p.getBlockPos();
        boolean sunlit = w.isDay() && w.isSkyVisible(pos) && !w.isRaining();
        int light = w.getLightLevel(pos);
        switch (race.id()) {
            case "dwarf" -> { if (p.getY() < 40) effect(p, HASTE, 60, 0); }
            case "tiefling" -> effect(p, FIRE_RESISTANCE, 60, 0);
            case "reborn" -> { p.removeStatusEffect(POISON); p.removeStatusEffect(StatusEffects.HUNGER); }
            case "mycelian" -> {
                if (light <= 7) effect(p, REGENERATION, 60, 0);
                if (sunlit) effect(p, WEAKNESS, 60, 0);
            }
            case "umbral" -> {
                effect(p, NIGHT_VISION, 260, 0);
                if (sunlit) effect(p, WEAKNESS, 60, 0);
            }
            case "abyssal" -> {
                effect(p, WATER_BREATHING, 60, 0);
                if (p.isTouchingWater()) effect(p, DOLPHINS_GRACE, 60, 0);
            }
            case "verdan" -> { if (sunlit) effect(p, REGENERATION, 60, 0); }
            default -> {}
        }
    }

    /** Multiplicador de dano causado (Umbral no escuro). */
    public static float damageMultiplier(ServerPlayerEntity p, PlayerRpgData d) {
        Race r = of(d);
        if (r != null && r.id().equals("umbral") && (p.getWorld().isNight() || p.getWorld().getLightLevel(p.getBlockPos()) <= 7)) return 1.15f;
        return 1f;
    }

    /** Multiplicador de dano recebido (Verdano é vulnerável a fogo). */
    public static float incomingMultiplier(PlayerRpgData d, net.minecraft.entity.damage.DamageSource source) {
        Race r = of(d);
        if (r != null && r.id().equals("verdan") && source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE)) return 1.25f;
        return 1f;
    }

    public static void forget(ServerPlayerEntity p) {
        HISTORY.remove(p.getUuid());
    }

    private static void effect(ServerPlayerEntity p, RegistryEntry<StatusEffect> e, int ticks, int amp) {
        StatusEffectInstance cur = p.getStatusEffect(e);
        if (cur == null || cur.getDuration() < ticks - 20) p.addStatusEffect(new StatusEffectInstance(e, ticks, amp, true, false, true));
    }
}
