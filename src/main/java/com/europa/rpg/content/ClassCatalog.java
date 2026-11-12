package com.europa.rpg.content;

import com.europa.rpg.ability.Abilities;
import com.europa.rpg.ability.Spell;
import com.europa.rpg.ability.Summons;
import com.europa.rpg.registry.ModItems;
import com.europa.rpg.rpg.Resource;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.rpg.RpgClass.Category;
import com.europa.rpg.rpg.RpgSubclass;
import com.europa.rpg.rpg.StatBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;

import static net.minecraft.entity.effect.StatusEffects.*;
import static net.minecraft.util.Formatting.*;

/**
 * Catálogo de conteúdo: 32 classes em 5 categorias, 3 subclasses cada (96), e as habilidades novas (DSL).
 * Habilidades artesanais ficam em {@link Abilities}. A validação no fim garante que nenhuma referência ficou solta.
 */
public final class ClassCatalog {
    private static boolean done;

    private ClassCatalog() {}

    // Perfis de atributos base
    private static StatBlock tank() { return StatBlock.builder().health(6, 0.6).armor(3, 0.12).damage(0, 0.04).manaRegen(1f).build(); }
    private static StatBlock melee() { return StatBlock.builder().health(4, 0.5).armor(2, 0.08).damage(0.5, 0.06).manaRegen(1f).build(); }
    private static StatBlock agile() { return StatBlock.builder().health(1, 0.35).damage(0, 0.05).speed(0.01).attackSpeed(0.3).manaRegen(1f).build(); }
    private static StatBlock ranged() { return StatBlock.builder().health(2, 0.35).speed(0.005).manaRegen(1f).build(); }
    private static StatBlock caster() { return StatBlock.builder().health(0, 0.25).mana(50, 4).manaRegen(3f).build(); }
    private static StatBlock support() { return StatBlock.builder().health(2, 0.4).armor(1, 0.05).mana(30, 2).manaRegen(2.5f).build(); }
    private static StatBlock hybrid() { return StatBlock.builder().health(3, 0.45).armor(1, 0.08).damage(0, 0.04).mana(20, 2).manaRegen(2f).build(); }

    private static StatBlock hp(double v) { return StatBlock.builder().health(v, 0).build(); }
    private static StatBlock armor(double v) { return StatBlock.builder().armor(v, 0).build(); }
    private static StatBlock dmg(double v) { return StatBlock.builder().damage(v, 0).build(); }
    private static StatBlock mana(float v) { return StatBlock.builder().mana(v, 0).build(); }
    private static StatBlock spd(double v) { return StatBlock.builder().speed(v).build(); }

    private static Category current = Category.MARTIAL;

    private static RpgClass cls(String id, Formatting color, Resource r, String ability, String icon, StatBlock s) {
        return RpgClass.register(id, current, color, r, ability, icon, s);
    }

    private static void sub(String id, RpgClass parent, Formatting color, String ultimate, StatBlock s) {
        RpgSubclass.register(id, parent, color, ultimate, s);
    }

    public static synchronized void init() {
        if (done) return;
        done = true;
        abilities();
        classes();
        validate();
    }

    // =====================================================================================
    // Habilidades novas (DSL). Custos em escala de mana; recargas em segundos.
    // =====================================================================================
    private static void abilities() {
        // ---- Marciais
        Spell.of("bulwark", 45, 40).buff(RESISTANCE, 8, 2).buff(ABSORPTION, 15, 1).taunt(10).sound(SoundEvents.ITEM_SHIELD_BLOCK, 0.6f).register();
        Spell.of("lightning_riposte", 35, 20).look(10).dashToTarget().physical(11).buff(SPEED, 6, 1).particle(ParticleTypes.SWEEP_ATTACK).sound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1.3f).register();
        Spell.of("arsenal_storm", 50, 30).around(5).physical(6).knockback(0.8).pulses(3, 10).particle(ParticleTypes.SWEEP_ATTACK).sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f).register();
        Spell.of("savage_cleave", 20, 6).around(3.5).physical(7).knockback(1).particle(ParticleTypes.SWEEP_ATTACK).sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f).register();
        Spell.of("earthshatter", 50, 30).around(6).physical(10).status(SLOWNESS, 3, 2).knockup(0.7).particle(ParticleTypes.EXPLOSION).sound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.6f).register();
        Spell.of("hunt", 40, 25).look(16).dashToTarget().physical(8).mark(0.2f, 10).buff(SPEED, 6, 1).sound(SoundEvents.ENTITY_WOLF_GROWL, 1f).register();
        Spell.of("holy_strike", 20, 6).look(5).magic(6).vsUndead().heal(2).particle(ParticleTypes.END_ROD).sound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f).register();
        Spell.of("purification", 50, 40).allies(10, 8).allyBuff(REGENERATION, 5, 1).cleanse().sound(SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.4f).register();
        Spell.of("iaijutsu", 25, 8).dash(1.6).around(3).physical(7).delay(6).particle(ParticleTypes.SWEEP_ATTACK).sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.6f).register();
        Spell.of("wandering_blade", 40, 20).look(6).physical(14).status(WITHER, 4, 0).particle(ParticleTypes.CRIT).sound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 0.8f).register();
        Spell.of("thousand_cuts", 45, 25).look(5).physical(2.5f).rapid().pulses(6, 3).particle(ParticleTypes.CRIT).sound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1.6f).register();
        Spell.of("shadow_palm", 35, 18).look(14).behindTarget().physical(8).status(BLINDNESS, 3, 0).particle(ParticleTypes.SQUID_INK).sound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.3f).register();
        Spell.of("inner_peace", 40, 45).heal(12).restore(30).buff(REGENERATION, 8, 1).buff(RESISTANCE, 10, 0).sound(SoundEvents.BLOCK_BELL_RESONATE, 1.5f).register();
        Spell.of("arena_strike", 20, 6).look(4).physical(8).status(SLOWNESS, 2, 1).knockback(1.5).sound(SoundEvents.ENTITY_IRON_GOLEM_ATTACK, 1f).register();
        Spell.of("champions_roar", 45, 40).allies(10, 0).allyBuff(STRENGTH, 10, 0).buff(RESISTANCE, 10, 1).around(8).status(WEAKNESS, 8, 0).sound(SoundEvents.ENTITY_RAVAGER_ROAR, 1.2f).register();
        Spell.of("bleeding_frenzy", 45, 25).around(4).physical(6).status(WITHER, 6, 1).drain(0.5f).particle(ParticleTypes.DAMAGE_INDICATOR).sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f).register();
        Spell.of("crowd_favor", 45, 50).buff(ABSORPTION, 15, 2).buff(SPEED, 15, 0).buff(STRENGTH, 15, 0).buff(REGENERATION, 6, 0).sound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.8f).register();

        // ---- Destreza e distância
        Spell.of("pilfer", 30, 20).look(6).physical(6).status(WEAKNESS, 6, 1).buff(SPEED, 6, 1)
                .extra("rpg.europa.fx.pilfer", (p, d, targets, pw) -> {
                    if (!targets.isEmpty()) {
                        ItemStack loot = new ItemStack(ModItems.ASTER_FRAGMENT, 1 + p.getRandom().nextInt(3));
                        if (!p.getInventory().insertStack(loot)) p.dropItem(loot, false);
                    }
                }).sound(SoundEvents.ENTITY_ITEM_PICKUP, 0.8f).register();
        Spell.of("hunters_mark", 25, 15).look(40).mark(0.25f, 15).status(WEAKNESS, 8, 0).particle(ParticleTypes.CRIT).sound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.8f).register();
        Spell.of("spirit_hawk", 40, 25).look(30).magic(8).status(BLINDNESS, 3, 0).mark(0.15f, 8).particle(ParticleTypes.CLOUD).sound(SoundEvents.ENTITY_PARROT_FLY, 0.6f).register();
        Spell.of("bolt_barrage", 25, 6).arrows(3, 2, 3).sound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.2f).register();
        Spell.of("deadeye", 50, 25).line(80).physical(18).particle(ParticleTypes.CRIT).sound(SoundEvents.ITEM_CROSSBOW_SHOOT, 0.5f).register();
        Spell.of("clockwork_trap", 35, 18).areaAt(16, 3).physical(8).root(3).delay(30).pulses(6, 20).particle(ParticleTypes.ELECTRIC_SPARK).sound(SoundEvents.BLOCK_PISTON_EXTEND, 1.4f).register();
        Spell.of("armor_breaker", 40, 20).line(40).physical(10).status(WEAKNESS, 8, 1).mark(0.3f, 10).particle(ParticleTypes.CRIT).sound(SoundEvents.ITEM_CROSSBOW_SHOOT, 0.7f).register();
        Spell.of("arcane_shot", 15, 2).look(32).magic(6).particle(ParticleTypes.WITCH).sound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.4f).register();
        Spell.of("volley_fire", 45, 22).line(50).physical(14).knockback(2).particle(ParticleTypes.SMOKE).sound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.6f).register();
        Spell.of("rune_rounds", 40, 18).look(30).magic(3).rapid().pulses(5, 4).particle(ParticleTypes.ENCHANT).sound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.8f).register();
        Spell.of("bombardment", 60, 35).areaAt(40, 5).magic(10).explosion().delay(15).pulses(3, 8).particle(ParticleTypes.FLAME).sound(SoundEvents.ENTITY_TNT_PRIMED, 1f).register();
        Spell.of("lunge", 20, 5).dash(1.4).look(5).physical(7).sound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.4f).register();
        Spell.of("flurry_thrust", 40, 20).look(5).physical(3).rapid().pulses(4, 3).particle(ParticleTypes.CRIT).sound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1.8f).register();
        Spell.of("duel_challenge", 30, 25).look(16).mark(0.4f, 10).buff(RESISTANCE, 10, 0).sound(SoundEvents.EVENT_RAID_HORN.value(), 1f).register();
        Spell.of("summon_companion", 30, 20).summon(Summons.Kind.WOLF, 1, 45).sound(SoundEvents.ENTITY_WOLF_AMBIENT, 1f).register();
        Spell.of("herd_blessing", 50, 45).allies(12, 8).allyBuff(REGENERATION, 15, 1).allyBuff(RESISTANCE, 15, 0).sound(SoundEvents.ENTITY_SHEEP_AMBIENT, 0.8f).register();
        Spell.of("alpha_howl", 60, 60).summon(Summons.Kind.WOLF, 2, 40).allies(16, 0).allyBuff(STRENGTH, 20, 1).allyBuff(SPEED, 20, 0).buff(STRENGTH, 20, 0).sound(SoundEvents.ENTITY_WOLF_HOWL, 0.8f).register();

        // ---- Arcanas
        Spell.of("elemental_burst", 50, 25).areaAt(30, 4).magic(8).ignite(3).status(SLOWNESS, 3, 1).particle(ParticleTypes.FLAME).sound(SoundEvents.ENTITY_BLAZE_SHOOT, 1f).register();
        Spell.of("arcane_barrage", 50, 25).look(32).magic(3.5f).rapid().pulses(5, 4).particle(ParticleTypes.END_ROD).sound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 1.8f).register();
        Spell.of("spellblade_aura", 45, 35).buff(STRENGTH, 15, 0).buff(RESISTANCE, 15, 0).buff(ABSORPTION, 15, 1).around(4).magic(5).particle(ParticleTypes.ENCHANT).register();
        Spell.of("chaos_bolt", 15, 3).look(30).magic(7).particle(ParticleTypes.WITCH)
                .extra("rpg.europa.fx.chaos", (p, d, targets, pw) -> {
                    for (var t : targets) {
                        switch (p.getRandom().nextInt(3)) {
                            case 0 -> t.setOnFireFor(4);
                            case 1 -> t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
                            default -> t.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 0));
                        }
                    }
                }).register();
        Spell.of("dragon_breath", 50, 25).areaAt(8, 3.5).magic(9).ignite(5).particle(ParticleTypes.DRAGON_BREATH).sound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 1.4f).register();
        Spell.of("storm_call", 55, 30).around(10).magic(9).lightning().particle(ParticleTypes.ELECTRIC_SPARK).sound(SoundEvents.ITEM_TRIDENT_THUNDER.value(), 1f).register();
        Spell.of("abyss_gaze", 45, 25).look(24).magic(10).status(WITHER, 5, 1).status(DARKNESS, 5, 0).particle(ParticleTypes.SCULK_SOUL).sound(SoundEvents.ENTITY_WARDEN_ANGRY, 1.5f).register();
        Spell.of("fireball", 15, 2).look(30).magic(6).ignite(4).particle(ParticleTypes.FLAME).sound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.2f).register();
        Spell.of("flame_cloak", 45, 35).buff(FIRE_RESISTANCE, 15, 0).around(3).magic(2).ignite(3).pulses(10, 20).particle(ParticleTypes.FLAME).register();
        Spell.of("ash_storm", 55, 30).around(7).magic(8).ignite(6).status(BLINDNESS, 3, 0).particle(ParticleTypes.ASH).sound(SoundEvents.ENTITY_BLAZE_AMBIENT, 0.6f).register();
        Spell.of("frost_bolt", 15, 2).look(30).magic(5).status(SLOWNESS, 3, 1).freeze(60).particle(ParticleTypes.SNOWFLAKE).sound(SoundEvents.ENTITY_PLAYER_HURT_FREEZE, 1.4f).register();
        Spell.of("ice_prison", 45, 25).look(24).magic(6).root(4).freeze(200).particle(ParticleTypes.SNOWFLAKE).sound(SoundEvents.BLOCK_GLASS_PLACE, 0.6f).register();
        Spell.of("permafrost_field", 50, 30).around(7).magic(2).status(SLOWNESS, 2, 3).freeze(80).pulses(6, 20).particle(ParticleTypes.SNOWFLAKE).register();
        Spell.of("spark", 15, 2).look(24).magic(6).particle(ParticleTypes.ELECTRIC_SPARK).sound(SoundEvents.BLOCK_COPPER_BULB_TURN_ON, 1.6f).register();
        Spell.of("thunderstorm", 60, 35).areaAt(30, 5).magic(5).lightning().pulses(4, 10).particle(ParticleTypes.ELECTRIC_SPARK).sound(SoundEvents.ITEM_TRIDENT_THUNDER.value(), 1.2f).register();
        Spell.of("chain_lightning", 45, 20).chain(30, 4, 6).magic(8).particle(ParticleTypes.ELECTRIC_SPARK).sound(SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 1.6f).register();
        Spell.of("voltaic_dash", 40, 20).blink(10).around(3).magic(8).status(SLOWNESS, 3, 1).particle(ParticleTypes.ELECTRIC_SPARK).sound(SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 2f).register();
        Spell.of("stone_spike", 15, 3).look(24).magic(6).knockup(0.9).particle(ParticleTypes.CRIT).sound(SoundEvents.BLOCK_POINTED_DRIPSTONE_LAND, 0.8f).register();
        Spell.of("quake", 55, 30).around(8).magic(5).status(SLOWNESS, 3, 2).knockup(0.4).pulses(3, 10).particle(ParticleTypes.CAMPFIRE_COSY_SMOKE).sound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.5f).register();
        Spell.of("crystal_armor", 45, 40).buff(ABSORPTION, 20, 3).buff(RESISTANCE, 10, 1).around(3).magic(4).particle(ParticleTypes.END_ROD).sound(SoundEvents.BLOCK_AMETHYST_CLUSTER_PLACE, 1f).register();
        Spell.of("titan_form", 55, 50).buff(HEALTH_BOOST, 20, 2).buff(RESISTANCE, 20, 1).buff(STRENGTH, 20, 0).buff(SLOWNESS, 20, 0).heal(8).sound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 0.6f).register();
        Spell.of("mirror_image", 25, 12).buff(INVISIBILITY, 4, 0).buff(SPEED, 4, 1).vanish(8).sound(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f).register();
        Spell.of("hall_of_mirrors", 50, 35).around(10).status(NAUSEA, 6, 0).status(BLINDNESS, 4, 0).confuse().particle(ParticleTypes.WITCH).sound(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.7f).register();
        Spell.of("false_death", 40, 45).buff(INVISIBILITY, 8, 0).buff(RESISTANCE, 3, 2).heal(6).vanish(16).sound(SoundEvents.ENTITY_PLAYER_DEATH, 1.2f).register();
        Spell.of("nightmare", 50, 30).look(24).magic(4).status(DARKNESS, 8, 0).status(WEAKNESS, 8, 1).status(SLOWNESS, 8, 1).pulses(4, 20).particle(ParticleTypes.SQUID_INK).sound(SoundEvents.ENTITY_PHANTOM_AMBIENT, 0.6f).register();
        Spell.of("orbiting_stones", 45, 35).buff(RESISTANCE, 10, 0).around(3).magic(3).knockback(0.6).pulses(10, 10).particle(ParticleTypes.CRIT).register();

        // ---- Espirituais
        Spell.of("foresight", 45, 35).allies(12, 0).allyBuff(ABSORPTION, 20, 1).around(24).mark(0.15f, 15).particle(ParticleTypes.END_ROD).sound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.2f).register();
        Spell.of("banish", 50, 30).around(8).magic(7).vsUndead().status(WEAKNESS, 6, 1).particle(ParticleTypes.END_ROD).sound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 1.2f).register();
        Spell.of("spirit_stampede", 55, 45).summon(Summons.Kind.WOLF, 3, 30).line(20).magic(8).knockback(2).particle(ParticleTypes.COMPOSTER).sound(SoundEvents.ENTITY_RAVAGER_ROAR, 1.4f).register();
        Spell.of("lich_form", 60, 50).buff(RESISTANCE, 20, 1).buff(REGENERATION, 20, 0).buff(ABSORPTION, 20, 2).around(6).status(WITHER, 6, 1).pulses(4, 20).particle(ParticleTypes.SOUL).register();
        Spell.of("eldritch_blast", 15, 2).look(30).magic(7).knockback(1).particle(ParticleTypes.WITCH).sound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.2f).register();
        Spell.of("hellfire", 50, 25).areaAt(30, 4).magic(10).ignite(6).selfDamage(4).particle(ParticleTypes.SOUL_FIRE_FLAME).sound(SoundEvents.ENTITY_GHAST_SHOOT, 0.8f).register();
        Spell.of("fey_step", 35, 18).around(4).status(SLOWNESS, 4, 1).status(NAUSEA, 4, 0).blink(12).buff(INVISIBILITY, 5, 0).particle(ParticleTypes.CHERRY_LEAVES).sound(SoundEvents.ENTITY_ALLAY_ITEM_THROWN, 1f).register();
        Spell.of("elder_sign", 55, 35).around(10).magic(8).status(DARKNESS, 6, 0).status(WEAKNESS, 6, 1).buff(RESISTANCE, 10, 0).particle(ParticleTypes.SCULK_SOUL).sound(SoundEvents.ENTITY_WARDEN_ROAR, 1.4f).register();
        Spell.of("spirit_bolt", 15, 3).look(24).magic(6).heal(3).particle(ParticleTypes.SOUL).sound(SoundEvents.PARTICLE_SOUL_ESCAPE.value(), 1f).register();
        Spell.of("ancestors_call", 50, 40).allies(12, 6).allyBuff(REGENERATION, 12, 1).allyBuff(STRENGTH, 12, 0).sound(SoundEvents.EVENT_RAID_HORN.value(), 1.4f).register();
        Spell.of("war_totem", 50, 35).allies(6, 0).allyBuff(REGENERATION, 10, 0).around(6).magic(3).status(SLOWNESS, 2, 0).pulses(10, 20).particle(ParticleTypes.TOTEM_OF_UNDYING).register();
        Spell.of("spirit_link", 40, 20).look(24).magic(8).drain(1f).particle(ParticleTypes.SOUL).sound(SoundEvents.ENTITY_ALLAY_HURT, 0.6f).register();
        Spell.of("war_anthem", 55, 45).allies(12, 0).allyBuff(STRENGTH, 15, 1).allyBuff(RESISTANCE, 15, 0).allyBuff(SPEED, 15, 0).sound(SoundEvents.EVENT_RAID_HORN.value(), 1f).register();

        // ---- Híbridas
        Spell.of("dragon_claw", 20, 5).look(5).magic(8).ignite(3).particle(ParticleTypes.FLAME).sound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1.4f).register();
        Spell.of("inferno_dive", 50, 25).dash(2).around(4).magic(10).ignite(5).explosion().delay(8).particle(ParticleTypes.FLAME).register();
        Spell.of("frost_roar", 50, 25).around(8).magic(8).status(SLOWNESS, 4, 2).freeze(160).particle(ParticleTypes.SNOWFLAKE).sound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 1.2f).register();
        Spell.of("dragon_ascendance", 60, 60).buff(STRENGTH, 20, 1).buff(RESISTANCE, 20, 1).buff(FIRE_RESISTANCE, 20, 0).buff(REGENERATION, 20, 0).buff(SLOW_FALLING, 20, 0).sound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 0.8f).register();
        Spell.of("runic_strike", 20, 5).look(5).magic(8).particle(ParticleTypes.ENCHANT).sound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.6f).register();
        Spell.of("arcane_cleave", 45, 22).around(5).magic(12).particle(ParticleTypes.ENCHANTED_HIT).sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f).register();
        Spell.of("conjured_arsenal", 45, 22).arrows(7, 4, 3).sound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 0.8f).register();
        Spell.of("tempered_arms", 40, 50).buff(STRENGTH, 20, 1).buff(HASTE, 20, 1).buff(RESISTANCE, 20, 0)
                .extra("rpg.europa.fx.repair", (p, d, targets, pw) -> {
                    ItemStack held = p.getMainHandStack();
                    if (held.isDamageable()) held.setDamage(Math.max(0, held.getDamage() - held.getMaxDamage() / 4));
                }).sound(SoundEvents.BLOCK_ANVIL_USE, 1.2f).register();
        Spell.of("field_remedy", 40, 30).allies(8, 8).allyBuff(ABSORPTION, 15, 0).cleanse().sound(SoundEvents.ENTITY_GENERIC_DRINK, 1.2f).register();
        Spell.of("transmute_flesh", 45, 45).heal(10).buff(HEALTH_BOOST, 30, 1).buff(RESISTANCE, 15, 0).sound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1f).register();
        Spell.of("enchanted_edge", 20, 6).buff(STRENGTH, 8, 0).around(3).magic(5).particle(ParticleTypes.ENCHANTED_HIT).sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f).register();
        Spell.of("rune_waltz", 45, 22).dash(1.6).around(3).magic(5).pulses(3, 6).particle(ParticleTypes.ENCHANT).register();
        Spell.of("elemental_edge", 45, 20).look(5).magic(10).ignite(4).status(SLOWNESS, 3, 1).freeze(100).particle(ParticleTypes.FLAME).sound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1f).register();
        Spell.of("phantom_blades", 45, 22).line(20).magic(9).status(WITHER, 4, 0).particle(ParticleTypes.SOUL).sound(SoundEvents.ENTITY_VEX_CHARGE, 1f).register();
        Spell.of("summon_familiar", 30, 25).summon(Summons.Kind.WOLF, 1, 40).sound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1f).register();
        Spell.of("elemental_guardian", 60, 60).summon(Summons.Kind.GOLEM, 1, 30).sound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 0.6f).register();
        Spell.of("rift_horde", 60, 50).summon(Summons.Kind.SERVANT, 3, 30).sound(SoundEvents.BLOCK_END_PORTAL_SPAWN, 1.6f).register();
        Spell.of("familiar_pack", 60, 50).summon(Summons.Kind.WOLF, 3, 45).allies(16, 0).allyBuff(STRENGTH, 20, 0).sound(SoundEvents.ENTITY_WOLF_HOWL, 1.2f).register();
    }

    // =====================================================================================
    // Classes e subclasses
    // =====================================================================================
    private static void classes() {
        // ---------------- CATEGORIA 1 — MARCIAIS
        current = Category.MARTIAL;
        RpgClass warrior = cls("warrior", RED, Resource.ENERGY, "charge", "minecraft:iron_sword", melee());
        sub("guardian", warrior, BLUE, "bulwark", armor(4));
        sub("duelist", warrior, WHITE, "lightning_riposte", spd(0.01));
        sub("weapon_master", warrior, GOLD, "arsenal_storm", dmg(1.5));

        RpgClass barbarian = cls("barbarian", DARK_RED, Resource.FURY, "savage_cleave", "minecraft:iron_axe", tank());
        sub("furious", barbarian, RED, "blood_rage", dmg(2));
        sub("colossus", barbarian, GRAY, "earthshatter", hp(6));
        sub("predator", barbarian, DARK_GREEN, "hunt", spd(0.015));

        RpgClass paladin = cls("paladin", YELLOW, Resource.FAITH, "holy_strike", "minecraft:golden_sword", tank());
        sub("templar", paladin, WHITE, "divine_shield", armor(4));
        sub("inquisitor", paladin, GOLD, "judgment", dmg(1.5));
        sub("redeemer", paladin, AQUA, "purification", mana(30));

        RpgClass samurai = cls("samurai", RED, Resource.ENERGY, "iaijutsu", "minecraft:netherite_sword", agile());
        sub("ronin", samurai, GRAY, "wandering_blade", dmg(1.5));
        sub("blade_master", samurai, WHITE, "thousand_cuts", StatBlock.builder().attackSpeed(0.3).build());
        sub("war_dancer", samurai, LIGHT_PURPLE, "blade_whirl", spd(0.01));

        RpgClass monk = cls("monk", GOLD, Resource.ENERGY, "seismic_palm", "minecraft:bamboo", agile());
        sub("celestial_fist", monk, YELLOW, "hundred_strikes", dmg(2));
        sub("shadow_master", monk, DARK_PURPLE, "shadow_palm", spd(0.01));
        sub("spirit_disciple", monk, AQUA, "inner_peace", hp(4));

        RpgClass gladiator = cls("gladiator", GOLD, Resource.FURY, "arena_strike", "minecraft:mace", melee());
        sub("champion", gladiator, YELLOW, "champions_roar", hp(4));
        sub("slasher", gladiator, DARK_RED, "bleeding_frenzy", dmg(1.5));
        sub("arena_idol", gladiator, LIGHT_PURPLE, "crowd_favor", armor(2));

        // ---------------- CATEGORIA 2 — DESTREZA E DISTÂNCIA
        current = Category.DEXTERITY;
        RpgClass rogue = cls("rogue", DARK_GREEN, Resource.OPPORTUNITY, "shadow_step", "minecraft:iron_sword", agile());
        sub("assassin", rogue, GRAY, "death_mark", dmg(1.5));
        sub("trickster", rogue, LIGHT_PURPLE, "smoke_bomb", spd(0.01));
        sub("relic_thief", rogue, GOLD, "pilfer", StatBlock.builder().speed(0.005).build());

        RpgClass ranger = cls("ranger", GREEN, Resource.ENERGY, "volley", "minecraft:bow", ranged());
        sub("marksman", ranger, DARK_AQUA, "piercing_shot", dmg(1));
        sub("hunter", ranger, GOLD, "hunters_mark", spd(0.01));
        sub("ranger_tamer", ranger, DARK_GREEN, "spirit_hawk", hp(4));

        RpgClass crossbowman = cls("crossbowman", GRAY, Resource.CHARGES, "bolt_barrage", "minecraft:crossbow", ranged());
        sub("sharpshooter", crossbowman, DARK_AQUA, "deadeye", dmg(1));
        sub("gadgeteer", crossbowman, GOLD, "clockwork_trap", armor(2));
        sub("piercer", crossbowman, RED, "armor_breaker", dmg(1));

        RpgClass arcaneGunner = cls("arcane_gunner", AQUA, Resource.MANA, "arcane_shot", "minecraft:firework_rocket", ranged());
        sub("musketeer", arcaneGunner, WHITE, "volley_fire", dmg(1));
        sub("rune_pistoleer", arcaneGunner, LIGHT_PURPLE, "rune_rounds", mana(30));
        sub("artillerist", arcaneGunner, GOLD, "bombardment", hp(4));

        RpgClass swordsman = cls("swordsman", WHITE, Resource.ENERGY, "lunge", "minecraft:diamond_sword", agile());
        sub("fencer", swordsman, AQUA, "flurry_thrust", StatBlock.builder().attackSpeed(0.3).build());
        sub("dancing_blade", swordsman, LIGHT_PURPLE, "wind_step", spd(0.015));
        sub("challenger", swordsman, RED, "duel_challenge", armor(2));

        RpgClass tamer = cls("tamer", GREEN, Resource.BOND, "summon_companion", "minecraft:lead", ranged());
        sub("beastmaster", tamer, DARK_GREEN, "call_of_the_pack", hp(4));
        sub("creature_shepherd", tamer, YELLOW, "herd_blessing", mana(30));
        sub("alpha", tamer, RED, "alpha_howl", dmg(1.5));

        // ---------------- CATEGORIA 3 — ARCANAS
        current = Category.ARCANE;
        RpgClass mage = cls("mage", AQUA, Resource.MANA, "arcane_missile", "europa:arcane_staff", caster());
        sub("elementalist", mage, GOLD, "elemental_burst", mana(40));
        sub("arcanist", mage, LIGHT_PURPLE, "arcane_barrage", mana(50));
        sub("battlemage", mage, RED, "spellblade_aura", hp(4));

        RpgClass sorcerer = cls("sorcerer", LIGHT_PURPLE, Resource.MANA, "chaos_bolt", "minecraft:amethyst_shard", caster());
        sub("draconic_heir", sorcerer, GOLD, "dragon_breath", hp(4));
        sub("tempestarian", sorcerer, YELLOW, "storm_call", mana(40));
        sub("abyssal", sorcerer, DARK_PURPLE, "abyss_gaze", mana(40));

        RpgClass pyromancer = cls("pyromancer", GOLD, Resource.MANA, "fireball", "minecraft:blaze_powder", caster());
        sub("living_flame", pyromancer, YELLOW, "flame_cloak", hp(4));
        sub("incendiary", pyromancer, RED, "meteor", mana(40));
        sub("ash_lord", pyromancer, DARK_GRAY, "ash_storm", mana(40));

        RpgClass cryomancer = cls("cryomancer", AQUA, Resource.MANA, "frost_bolt", "minecraft:blue_ice", caster());
        sub("glacial", cryomancer, WHITE, "ice_prison", armor(2));
        sub("permafrost", cryomancer, DARK_AQUA, "permafrost_field", hp(4));
        sub("blizzard_caller", cryomancer, BLUE, "blizzard", mana(40));

        RpgClass electromancer = cls("electromancer", YELLOW, Resource.MANA, "spark", "minecraft:lightning_rod", caster());
        sub("storm", electromancer, BLUE, "thunderstorm", mana(40));
        sub("conductor", electromancer, YELLOW, "chain_lightning", mana(40));
        sub("voltaic_arc", electromancer, AQUA, "voltaic_dash", spd(0.01));

        RpgClass geomancer = cls("geomancer", GRAY, Resource.MANA, "stone_spike", "minecraft:cobblestone", caster());
        sub("earthquake", geomancer, GOLD, "quake", mana(40));
        sub("crystalline", geomancer, LIGHT_PURPLE, "crystal_armor", armor(3));
        sub("stone_titan", geomancer, GRAY, "titan_form", hp(6));

        RpgClass illusionist = cls("illusionist", LIGHT_PURPLE, Resource.MANA, "mirror_image", "minecraft:glass_pane", caster());
        sub("mirror_master", illusionist, AQUA, "hall_of_mirrors", mana(40));
        sub("deceiver", illusionist, GRAY, "false_death", spd(0.01));
        sub("nightmare_weaver", illusionist, DARK_PURPLE, "nightmare", mana(40));

        RpgClass graviturge = cls("graviturge", DARK_PURPLE, Resource.MANA, "void_rift", "minecraft:ender_eye", caster());
        sub("black_well", graviturge, DARK_AQUA, "devouring_rift", hp(4));
        sub("living_orbit", graviturge, AQUA, "orbiting_stones", armor(3));
        sub("collapse", graviturge, LIGHT_PURPLE, "stellar_collapse", mana(40));

        // ---------------- CATEGORIA 4 — ESPIRITUAIS E SOBRENATURAIS
        current = Category.SPIRITUAL;
        RpgClass cleric = cls("cleric", YELLOW, Resource.FAITH, "restoring_light", "minecraft:golden_apple", support());
        sub("priest", cleric, WHITE, "sanctuary", mana(40));
        sub("oracle", cleric, AQUA, "foresight", mana(40));
        sub("exorcist", cleric, GOLD, "banish", dmg(1.5));

        RpgClass druid = cls("druid", DARK_GREEN, Resource.MANA, "thorn_snare", "minecraft:oak_sapling", support());
        sub("shapeshifter", druid, GOLD, "beast_form", hp(4));
        sub("forest_guardian", druid, GREEN, "ancestral_roots", armor(2));
        sub("beast_circle", druid, DARK_GREEN, "spirit_stampede", dmg(1));

        RpgClass necromancer = cls("necromancer", DARK_PURPLE, Resource.SPIRIT, "raise_servant", "minecraft:wither_skeleton_skull", caster());
        sub("necro_summoner", necromancer, GRAY, "unholy_legion", mana(40));
        sub("lich", necromancer, AQUA, "lich_form", hp(4));
        sub("soul_reaper", necromancer, DARK_RED, "blood_pact", hp(4));

        RpgClass warlock = cls("warlock", DARK_PURPLE, Resource.SPIRIT, "eldritch_blast", "minecraft:crying_obsidian", caster());
        sub("demonic_pact", warlock, RED, "hellfire", dmg(1));
        sub("fey_pact", warlock, LIGHT_PURPLE, "fey_step", spd(0.01));
        sub("ancient_pact", warlock, DARK_AQUA, "elder_sign", mana(40));

        RpgClass shaman = cls("shaman", AQUA, Resource.SPIRIT, "spirit_bolt", "minecraft:totem_of_undying", support());
        sub("ancestral_spirit", shaman, YELLOW, "ancestors_call", mana(40));
        sub("totemic", shaman, GOLD, "war_totem", armor(2));
        sub("medium", shaman, DARK_AQUA, "spirit_link", hp(4));

        RpgClass bard = cls("bard", LIGHT_PURPLE, Resource.MANA, "song_of_valor", "minecraft:note_block", support());
        sub("minstrel", bard, YELLOW, "restoring_ballad", mana(40));
        sub("enchanter", bard, DARK_PURPLE, "dissonant_chord", mana(30));
        sub("war_herald", bard, RED, "war_anthem", hp(4));

        // ---------------- CATEGORIA 5 — HÍBRIDAS E ESPECIALIZADAS
        current = Category.HYBRID;
        RpgClass dragonKnight = cls("dragon_knight", DARK_RED, Resource.FURY, "dragon_claw", "minecraft:dragon_head", tank());
        sub("igneous_dragon", dragonKnight, GOLD, "inferno_dive", dmg(1.5));
        sub("glacial_dragon", dragonKnight, AQUA, "frost_roar", armor(2));
        sub("ancestral_dragon", dragonKnight, DARK_PURPLE, "dragon_ascendance", hp(6));

        RpgClass warArcanist = cls("war_arcanist", BLUE, Resource.MANA, "runic_strike", "minecraft:enchanted_book", hybrid());
        sub("war_spellblade", warArcanist, LIGHT_PURPLE, "arcane_cleave", dmg(1.5));
        sub("battle_conjurer", warArcanist, AQUA, "conjured_arsenal", mana(30));
        sub("mystic_guardian", warArcanist, BLUE, "runic_armor", armor(3));

        RpgClass artificer = cls("artificer", GOLD, Resource.CHARGES, "explosive_rune", "minecraft:piston", hybrid());
        sub("arcane_smith", artificer, GOLD, "tempered_arms", armor(2));
        sub("combat_engineer", artificer, GRAY, "rune_turret", hp(4));
        sub("field_alchemist", artificer, GREEN, "field_remedy", mana(20));

        RpgClass alchemist = cls("alchemist", DARK_AQUA, Resource.MANA, "volatile_flask", "minecraft:splash_potion", support());
        sub("transmuter", alchemist, GOLD, "transmute_flesh", hp(4));
        sub("bomber", alchemist, DARK_GREEN, "toxic_cloud", mana(30));
        sub("elixir_master", alchemist, LIGHT_PURPLE, "perfect_elixir", mana(30));

        RpgClass arcaneBlade = cls("arcane_blade", LIGHT_PURPLE, Resource.MANA, "enchanted_edge", "minecraft:golden_sword", hybrid());
        sub("rune_dancer", arcaneBlade, AQUA, "rune_waltz", spd(0.01));
        sub("elemental_sword", arcaneBlade, GOLD, "elemental_edge", dmg(1.5));
        sub("spectral_blade", arcaneBlade, GRAY, "phantom_blades", mana(30));

        RpgClass summoner = cls("summoner", DARK_AQUA, Resource.MANA, "summon_familiar", "minecraft:allay_spawn_egg", caster());
        sub("elemental_summoner", summoner, GOLD, "elemental_guardian", hp(4));
        sub("dimensional_conjurer", summoner, DARK_PURPLE, "rift_horde", mana(40));
        sub("familiar_master", summoner, GREEN, "familiar_pack", mana(30));

        // Dançarino de Guerra (Samurai): redemoinho de lâminas
        Spell.of("blade_whirl", 45, 22).buff(SPEED, 6, 1).around(4).physical(4).pulses(4, 5).particle(ParticleTypes.SWEEP_ATTACK)
                .sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f).register();
    }

    private static void validate() {
        if (RpgClass.all().size() != 32) throw new IllegalStateException("Esperadas 32 classes, há " + RpgClass.all().size());
        for (RpgClass c : RpgClass.all()) {
            if (Abilities.get(c.primaryAbility) == null) throw new IllegalStateException("Habilidade ausente: " + c.primaryAbility + " (" + c.id + ")");
            if (c.subclasses().size() != 3) throw new IllegalStateException("Classe " + c.id + " deveria ter 3 subclasses");
            for (RpgSubclass s : c.subclasses()) {
                if (Abilities.get(s.ultimateAbility) == null) throw new IllegalStateException("Suprema ausente: " + s.ultimateAbility + " (" + s.id + ")");
            }
        }
    }
}
