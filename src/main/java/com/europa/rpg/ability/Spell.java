package com.europa.rpg.ability;

import com.europa.rpg.rpg.CombatHooks;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.util.TaskScheduler;
import com.europa.rpg.util.Targeting;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * DSL de habilidades. Uma habilidade = modo de alvo + carga útil (dano/efeitos nos alvos) +
 * efeitos imediatos (no lançador, aliados, movimento, invocações). A descrição exibida ao jogador
 * é gerada a partir destes mesmos números, então nunca fica desatualizada.
 *
 * <pre>
 * Spell.of("frost_bolt", 15, 2).look(30).magic(5).status(SLOWNESS, 3, 1).freeze(60).particle(SNOWFLAKE).register();
 * </pre>
 */
public final class Spell {
    public enum Mode { SELF, LOOK, AREA_SELF, AREA_LOOK, LINE, CHAIN }

    @FunctionalInterface
    public interface Extra {
        void apply(ServerPlayerEntity p, PlayerRpgData d, List<LivingEntity> targets, float power);
    }

    private record Fx(RegistryEntry<StatusEffect> effect, int seconds, int amp) {}

    private final String id;
    private final int cost, cooldown;
    private Mode mode = Mode.SELF;
    private double range, radius;
    private int chain;
    private float damage;
    private boolean physical, undeadBonus, ignoreIframes, lightning, explosionFx, taunt, aggroDrop, confuse, cleanse, behind, dashToTarget;
    private final List<Fx> targetFx = new ArrayList<>(), selfFx = new ArrayList<>(), allyFx = new ArrayList<>();
    private int ignite, freeze, delay, pulses = 1, interval = 20, markSeconds, summonCount, summonSeconds;
    private double knockback, knockup, pull, allyRadius, dash, blink, aggroRadius;
    private float drain, markBonus, selfHeal, selfDamage, restore, allyHeal, arrowDamage, arrowSpread;
    private int arrows;
    private Summons.Kind summonKind;
    private ParticleEffect particle = ParticleTypes.CRIT;
    private SoundEvent sound = SoundEvents.ENTITY_EVOKER_CAST_SPELL;
    private float pitch = 1f;
    private Extra extra;
    private String extraKey;

    private Spell(String id, int cost, int cooldownSeconds) {
        this.id = id;
        this.cost = cost;
        this.cooldown = cooldownSeconds * 20;
    }

    public static Spell of(String id, int cost, int cooldownSeconds) {
        return new Spell(id, cost, cooldownSeconds);
    }

    // ------------------------------------------------------------- alvo
    public Spell self() { mode = Mode.SELF; return this; }
    public Spell look(double range) { mode = Mode.LOOK; this.range = range; return this; }
    public Spell around(double radius) { mode = Mode.AREA_SELF; this.radius = radius; return this; }
    public Spell areaAt(double range, double radius) { mode = Mode.AREA_LOOK; this.range = range; this.radius = radius; return this; }
    public Spell line(double range) { mode = Mode.LINE; this.range = range; return this; }
    public Spell chain(double range, int jumps, double jumpRadius) { mode = Mode.CHAIN; this.range = range; chain = jumps; radius = jumpRadius; return this; }

    // ------------------------------------------------------------- carga útil nos alvos
    public Spell magic(float dmg) { damage = dmg; physical = false; return this; }
    public Spell physical(float dmg) { damage = dmg; physical = true; return this; }
    public Spell vsUndead() { undeadBonus = true; return this; }
    public Spell rapid() { ignoreIframes = true; return this; }
    public Spell status(RegistryEntry<StatusEffect> e, int seconds, int amp) { targetFx.add(new Fx(e, seconds, amp)); return this; }
    public Spell ignite(int seconds) { ignite = seconds; return this; }
    public Spell freeze(int ticks) { freeze = ticks; return this; }
    public Spell root(int seconds) { return status(StatusEffects.SLOWNESS, seconds, 6); }
    public Spell knockback(double s) { knockback = s; return this; }
    public Spell knockup(double s) { knockup = s; return this; }
    public Spell pull(double s) { pull = s; return this; }
    public Spell drain(float fraction) { drain = fraction; return this; }
    public Spell mark(float bonus, int seconds) { markBonus = bonus; markSeconds = seconds; return this; }
    public Spell lightning() { lightning = true; return this; }
    public Spell explosion() { explosionFx = true; return this; }
    public Spell confuse() { confuse = true; return this; }
    public Spell delay(int ticks) { delay = ticks; return this; }
    public Spell pulses(int count, int intervalTicks) { pulses = count; interval = intervalTicks; return this; }

    // ------------------------------------------------------------- imediatos
    public Spell buff(RegistryEntry<StatusEffect> e, int seconds, int amp) { selfFx.add(new Fx(e, seconds, amp)); return this; }
    public Spell heal(float amount) { selfHeal = amount; return this; }
    public Spell selfDamage(float amount) { selfDamage = amount; return this; }
    public Spell restore(float amount) { restore = amount; return this; }
    public Spell allies(double r, float heal) { allyRadius = r; allyHeal = heal; return this; }
    public Spell allyBuff(RegistryEntry<StatusEffect> e, int seconds, int amp) { allyFx.add(new Fx(e, seconds, amp)); return this; }
    public Spell cleanse() { cleanse = true; return this; }
    public Spell dash(double s) { dash = s; return this; }
    public Spell blink(double distance) { blink = distance; return this; }
    public Spell behindTarget() { behind = true; return this; }
    public Spell dashToTarget() { dashToTarget = true; return this; }
    public Spell summon(Summons.Kind kind, int count, int seconds) { summonKind = kind; summonCount = count; summonSeconds = seconds; return this; }
    public Spell arrows(int count, float spread, float dmg) { arrows = count; arrowSpread = spread; arrowDamage = dmg; return this; }
    public Spell taunt(double r) { taunt = true; aggroRadius = r; return this; }
    public Spell vanish(double r) { aggroDrop = true; aggroRadius = r; return this; }
    public Spell particle(ParticleEffect p) { particle = p; return this; }
    public Spell sound(SoundEvent s, float pitch) { sound = s; this.pitch = pitch; return this; }
    /** Efeito especial em código, com chave de idioma própria para a descrição. */
    public Spell extra(String descKey, Extra e) { extraKey = descKey; extra = e; return this; }

    public AbilityDef register() {
        AbilityDef def = new AbilityDef(id, cost, cooldown, this::cast, this::describe);
        Abilities.add(def);
        return def;
    }

    // ============================================================= execução

    private boolean cast(ServerPlayerEntity p, PlayerRpgData d, float power) {
        ServerWorld w = p.getServerWorld();
        LivingEntity primary = null;
        if (mode == Mode.LOOK || mode == Mode.CHAIN || behind || dashToTarget) {
            primary = Targeting.lookTarget(p, range > 0 ? range : 16);
            if (primary == null) {
                p.sendMessage(Text.translatable("rpg.europa.no_target").formatted(Formatting.GRAY), true);
                return false;
            }
        }
        final Vec3d fixedAnchor = mode == Mode.AREA_LOOK ? Targeting.blockHitPos(p, range) : null;

        // --- movimento
        if (blink > 0) {
            double dist = Math.max(0, Targeting.blockDistance(p, blink) - 1);
            if (dist >= 1) {
                Vec3d to = p.getPos().add(p.getRotationVector().multiply(dist));
                w.spawnParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.4);
                p.requestTeleport(to.x, to.y, to.z);
                p.fallDistance = 0;
            }
        }
        if (dash > 0) {
            Vec3d look = p.getRotationVector().multiply(1, 0, 1).normalize();
            p.addVelocity(look.x * dash, 0.3, look.z * dash);
            p.velocityModified = true;
        }
        if (primary != null && (behind || dashToTarget)) {
            Vec3d to = behind
                    ? primary.getPos().subtract(primary.getRotationVector().multiply(1, 0, 1).normalize().multiply(1.5))
                    : primary.getPos().add(p.getPos().subtract(primary.getPos()).normalize().multiply(1.2));
            if (!w.isSpaceEmpty(p, p.getBoundingBox().offset(to.subtract(p.getPos())))) to = primary.getPos();
            w.spawnParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 1, p.getZ(), 15, 0.3, 0.6, 0.3, 0.02);
            p.requestTeleport(to.x, primary.getY(), to.z);
            p.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, primary.getEyePos());
        }

        // --- lançador e aliados
        for (Fx f : selfFx) p.addStatusEffect(new StatusEffectInstance(f.effect, scaledTicks(f.seconds, power), f.amp));
        if (selfHeal > 0) p.heal(selfHeal * power);
        if (selfDamage > 0) p.damage(p.getDamageSources().magic(), selfDamage);
        if (restore > 0) d.mana = Math.min(Progression.maxMana(d), d.mana + restore);
        if (allyRadius > 0) {
            for (ServerPlayerEntity a : w.getPlayers(o -> o.isAlive() && o.squaredDistanceTo(p) <= allyRadius * allyRadius)) {
                if (allyHeal > 0) a.heal(allyHeal * power);
                for (Fx f : allyFx) a.addStatusEffect(new StatusEffectInstance(f.effect, scaledTicks(f.seconds, power), f.amp));
                if (cleanse) {
                    a.removeStatusEffect(StatusEffects.POISON);
                    a.removeStatusEffect(StatusEffects.WITHER);
                    a.removeStatusEffect(StatusEffects.WEAKNESS);
                    a.removeStatusEffect(StatusEffects.SLOWNESS);
                }
                w.spawnParticles(ParticleTypes.HAPPY_VILLAGER, a.getX(), a.getY() + 1, a.getZ(), 6, 0.4, 0.6, 0.4, 0);
            }
            for (MobEntity pet : Summons.owned(p)) {
                if (allyHeal > 0) pet.heal(allyHeal * power);
                for (Fx f : allyFx) pet.addStatusEffect(new StatusEffectInstance(f.effect, scaledTicks(f.seconds, power), f.amp));
            }
        }
        if (summonKind != null) Summons.summon(p, summonKind, summonCount, summonSeconds * 20, power, power > 1.5f);
        if (arrows > 0) {
            for (int i = 0; i < arrows; i++) {
                float yawOff = (i - (arrows - 1) / 2f) * arrowSpread;
                ArrowEntity a = new ArrowEntity(w, p, new ItemStack(Items.ARROW), null);
                a.setVelocity(p, p.getPitch(), p.getYaw() + yawOff, 0f, 3.2f, 1.0f);
                a.setDamage(arrowDamage * power);
                a.setCritical(true);
                a.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                w.spawnEntity(a);
            }
        }
        if (taunt || aggroDrop) {
            for (LivingEntity e : Targeting.enemiesAround(p, p.getPos(), aggroRadius)) {
                if (e instanceof MobEntity mob) {
                    if (taunt) mob.setTarget(p);
                    else if (mob.getTarget() == p) mob.setTarget(null);
                }
            }
        }
        w.playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1f, pitch);

        // --- carga útil (com atraso e pulsos)
        final LivingEntity first = primary;
        boolean hasPayload = damage > 0 || !targetFx.isEmpty() || ignite > 0 || freeze > 0 || knockback > 0 || knockup > 0
                || pull > 0 || markBonus > 0 || lightning || confuse || extra != null || explosionFx;
        if (hasPayload && mode != Mode.SELF) {
            for (int i = 0; i < pulses; i++) {
                int at = delay + i * interval;
                Runnable pulse = () -> {
                    if (!p.isAlive()) return;
                    Vec3d anchor = fixedAnchor != null ? fixedAnchor : (first != null && first.isAlive() ? first.getPos() : p.getPos());
                    List<LivingEntity> targets = resolveTargets(p, first, anchor);
                    applyPayload(p, d, power, anchor, targets);
                };
                if (at <= 0) pulse.run();
                else TaskScheduler.schedule(at, pulse);
            }
        } else if (extra != null) {
            extra.apply(p, d, List.of(), power);
        }
        return true;
    }

    private static int scaledTicks(int seconds, float power) {
        return seconds * 20;
    }

    private List<LivingEntity> resolveTargets(ServerPlayerEntity p, LivingEntity first, Vec3d anchor) {
        return switch (mode) {
            case LOOK -> first != null && first.isAlive() ? List.of(first) : List.of();
            case AREA_SELF -> Targeting.enemiesAround(p, p.getPos(), radius);
            case AREA_LOOK -> Targeting.enemiesAround(p, anchor, radius);
            case LINE -> Targeting.lineTargets(p, range);
            case CHAIN -> {
                List<LivingEntity> out = new ArrayList<>();
                if (first == null || !first.isAlive()) yield out;
                out.add(first);
                Set<LivingEntity> seen = new HashSet<>(out);
                LivingEntity cur = first;
                for (int i = 0; i < chain; i++) {
                    final LivingEntity from = cur;
                    LivingEntity next = Targeting.enemiesAround(p, from.getPos(), radius).stream()
                            .filter(e -> !seen.contains(e))
                            .min((a, b) -> Double.compare(a.squaredDistanceTo(from), b.squaredDistanceTo(from))).orElse(null);
                    if (next == null) break;
                    out.add(next);
                    seen.add(next);
                    cur = next;
                }
                yield out;
            }
            case SELF -> List.of();
        };
    }

    private void applyPayload(ServerPlayerEntity p, PlayerRpgData d, float power, Vec3d anchor, List<LivingEntity> targets) {
        ServerWorld w = p.getServerWorld();
        float dealt = 0;
        float chainFactor = 1f;
        LivingEntity prev = null;
        for (LivingEntity t : targets) {
            if (damage > 0) {
                float dmg = damage * power * chainFactor;
                if (undeadBonus && t.getType().isIn(EntityTypeTags.UNDEAD)) dmg *= 2f;
                if (ignoreIframes) t.timeUntilRegen = 0;
                boolean hit = t.damage(physical ? p.getDamageSources().playerAttack(p) : p.getDamageSources().indirectMagic(p, p), dmg);
                if (hit) dealt += dmg;
            }
            for (Fx f : targetFx) t.addStatusEffect(new StatusEffectInstance(f.effect, f.seconds * 20, f.amp));
            if (ignite > 0) t.setOnFireFor(ignite);
            if (freeze > 0) t.setFrozenTicks(Math.max(t.getFrozenTicks(), freeze));
            if (knockback > 0) {
                Vec3d dir = t.getPos().subtract(mode == Mode.AREA_LOOK ? anchor : p.getPos()).normalize();
                t.takeKnockback(knockback, -dir.x, -dir.z);
            }
            if (knockup > 0) { t.addVelocity(0, knockup, 0); t.velocityModified = true; }
            if (pull > 0) {
                Vec3d dir = anchor.subtract(t.getPos()).normalize().multiply(pull);
                t.addVelocity(dir.x, dir.y * 0.4 + 0.1, dir.z);
                t.velocityModified = true;
            }
            if (markBonus > 0) {
                CombatHooks.mark(t, markBonus, markSeconds * 20);
                t.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, markSeconds * 20, 0));
            }
            if (lightning) {
                LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
                if (bolt != null) {
                    bolt.refreshPositionAfterTeleport(t.getPos());
                    bolt.setCosmetic(true);
                    w.spawnEntity(bolt);
                }
            }
            if (confuse && t instanceof MobEntity mob && targets.size() > 1) {
                mob.setTarget(targets.get((targets.indexOf(t) + 1) % targets.size()));
            }
            if (mode == Mode.LOOK || mode == Mode.LINE || mode == Mode.CHAIN) {
                Vec3d from = prev != null && mode == Mode.CHAIN ? prev.getPos().add(0, prev.getHeight() / 2, 0) : p.getEyePos().subtract(0, 0.2, 0);
                Targeting.particleLine(w, particle, from, t.getPos().add(0, t.getHeight() / 2, 0), 0.4);
            } else {
                w.spawnParticles(particle, t.getX(), t.getBodyY(0.5), t.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
            }
            prev = t;
            if (mode == Mode.CHAIN) chainFactor *= 0.8f;
        }
        if (mode == Mode.AREA_SELF || mode == Mode.AREA_LOOK) {
            Vec3d c = mode == Mode.AREA_SELF ? p.getPos() : anchor;
            int n = (int) Math.max(16, radius * 8);
            for (int i = 0; i < n; i++) {
                double a = Math.PI * 2 * i / n;
                w.spawnParticles(particle, c.x + Math.cos(a) * radius, c.y + 0.3, c.z + Math.sin(a) * radius, 1, 0, 0.1, 0, 0);
            }
            if (explosionFx) {
                w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 0.5, c.z, 1, 0, 0, 0, 0);
                w.playSound(null, c.x, c.y, c.z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 1f, 1f);
            }
        }
        if (drain > 0 && dealt > 0) p.heal(dealt * drain);
        if (!targets.isEmpty()) Summons.focus(p, targets.getFirst());
        if (extra != null) extra.apply(p, d, targets, power);
    }

    // ============================================================= descrição

    private Text describe() {
        MutableText t = Text.empty();
        List<Text> parts = new ArrayList<>();
        if (blink > 0) parts.add(Text.translatable("rpg.europa.fx.blink", fmt(blink)));
        if (dash > 0) parts.add(Text.translatable("rpg.europa.fx.dash"));
        if (behind) parts.add(Text.translatable("rpg.europa.fx.behind"));
        if (dashToTarget) parts.add(Text.translatable("rpg.europa.fx.dash_to"));
        for (Fx f : selfFx) parts.add(Text.translatable("rpg.europa.fx.self", effectName(f)));
        if (selfHeal > 0) parts.add(Text.translatable("rpg.europa.fx.heal_self", fmt(selfHeal)));
        if (restore > 0) parts.add(Text.translatable("rpg.europa.fx.restore", fmt(restore)));
        if (selfDamage > 0) parts.add(Text.translatable("rpg.europa.fx.self_damage", fmt(selfDamage)));
        if (allyRadius > 0) {
            MutableText a = Text.translatable("rpg.europa.fx.allies", fmt(allyRadius));
            List<Text> ap = new ArrayList<>();
            if (allyHeal > 0) ap.add(Text.translatable("rpg.europa.fx.heal", fmt(allyHeal)));
            for (Fx f : allyFx) ap.add(effectName(f));
            if (cleanse) ap.add(Text.translatable("rpg.europa.fx.cleanse"));
            parts.add(a.append(join(ap)));
        }
        if (summonKind != null) parts.add(Text.translatable("rpg.europa.fx.summon." + summonKind.name().toLowerCase(Locale.ROOT), summonCount, summonSeconds));
        if (arrows > 0) parts.add(Text.translatable("rpg.europa.fx.arrows", arrows, fmt(arrowDamage)));
        if (taunt) parts.add(Text.translatable("rpg.europa.fx.taunt", fmt(aggroRadius)));
        if (aggroDrop) parts.add(Text.translatable("rpg.europa.fx.vanish", fmt(aggroRadius)));

        List<Text> payload = new ArrayList<>();
        if (damage > 0) payload.add(Text.translatable(physical ? "rpg.europa.fx.damage_physical" : "rpg.europa.fx.damage_magic", fmt(damage)));
        if (undeadBonus) payload.add(Text.translatable("rpg.europa.fx.vs_undead"));
        for (Fx f : targetFx) payload.add(isRoot(f) ? Text.translatable("rpg.europa.fx.root", f.seconds) : effectName(f));
        if (ignite > 0) payload.add(Text.translatable("rpg.europa.fx.ignite", ignite));
        if (freeze > 0) payload.add(Text.translatable("rpg.europa.fx.freeze"));
        if (knockback > 0) payload.add(Text.translatable("rpg.europa.fx.knockback"));
        if (knockup > 0) payload.add(Text.translatable("rpg.europa.fx.knockup"));
        if (pull > 0) payload.add(Text.translatable("rpg.europa.fx.pull"));
        if (markBonus > 0) payload.add(Text.translatable("rpg.europa.fx.mark", fmt(markBonus * 100), markSeconds));
        if (drain > 0) payload.add(Text.translatable("rpg.europa.fx.drain", fmt(drain * 100)));
        if (lightning) payload.add(Text.translatable("rpg.europa.fx.lightning"));
        if (confuse) payload.add(Text.translatable("rpg.europa.fx.confuse"));
        if (extraKey != null) payload.add(Text.translatable(extraKey));
        if (!payload.isEmpty() && mode != Mode.SELF) {
            MutableText head = switch (mode) {
                case LOOK -> Text.translatable("rpg.europa.fx.target", fmt(range));
                case AREA_SELF -> Text.translatable("rpg.europa.fx.around", fmt(radius));
                case AREA_LOOK -> Text.translatable("rpg.europa.fx.area_at", fmt(radius), fmt(range));
                case LINE -> Text.translatable("rpg.europa.fx.line", fmt(range));
                case CHAIN -> Text.translatable("rpg.europa.fx.chain", chain + 1);
                default -> Text.empty();
            };
            head.append(join(payload));
            if (pulses > 1) head.append(Text.translatable("rpg.europa.fx.pulses", pulses, fmt(interval / 20.0)));
            if (delay > 0) head.append(Text.translatable("rpg.europa.fx.delay", fmt(delay / 20.0)));
            parts.add(head);
        } else if (extraKey != null && mode == Mode.SELF) {
            parts.add(Text.translatable(extraKey));
        }
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) t.append(Text.literal(". "));
            t.append(parts.get(i));
        }
        return t.append(".");
    }

    private static boolean isRoot(Fx f) {
        return f.effect == StatusEffects.SLOWNESS && f.amp >= 6;
    }

    private static Text effectName(Fx f) {
        MutableText name = Text.translatable(f.effect.value().getTranslationKey());
        if (f.amp > 0) name.append(" " + roman(f.amp + 1));
        return name.append(" (" + f.seconds + "s)");
    }

    private static MutableText join(List<Text> parts) {
        MutableText t = Text.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) t.append(", ");
            t.append(parts.get(i));
        }
        return t;
    }

    private static String roman(int n) {
        return switch (n) { case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(n); };
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v);
    }
}
