package com.europa.rpg.rpg;

import com.europa.rpg.EuropaRPG;
import com.europa.rpg.ability.AbilityDef;
import com.europa.rpg.ability.AbilityHandler;
import com.europa.rpg.ability.Summons;
import com.europa.rpg.blessing.Blessings;
import com.europa.rpg.blessing.Patron;
import com.europa.rpg.item.RarityRoller;
import com.europa.rpg.network.RpgFullSyncPayload;
import com.europa.rpg.network.RpgSyncPayload;
import com.europa.rpg.profession.Professions;
import com.europa.rpg.race.Race;
import com.europa.rpg.race.Races;
import com.europa.rpg.registry.ModArmorMaterials;
import com.europa.rpg.talent.TalentEffect;
import com.europa.rpg.talent.Talents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** XP, níveis, recursos e composição de atributos de todas as camadas do personagem. */
public final class Progression {
    public static final int MAX_LEVEL = 50;
    public static final int SUBCLASS_LEVEL = 10;
    public static final float BASE_MANA = 100f;
    /** Ticks sem combate até Fúria/Oportunidade começarem a decair. */
    public static final int COMBAT_GRACE = 100;

    private static final Identifier HEALTH_ID = EuropaRPG.id("rpg_health");
    private static final Identifier ARMOR_ID = EuropaRPG.id("rpg_armor");
    private static final Identifier TOUGHNESS_ID = EuropaRPG.id("rpg_toughness");
    private static final Identifier KNOCKBACK_ID = EuropaRPG.id("rpg_knockback");
    private static final Identifier DAMAGE_ID = EuropaRPG.id("rpg_damage");
    private static final Identifier SPEED_ID = EuropaRPG.id("rpg_speed");
    private static final Identifier ATTACK_SPEED_ID = EuropaRPG.id("rpg_attack_speed");
    private static final Identifier LUCK_ID = EuropaRPG.id("rpg_luck");
    private static final Identifier BREAK_SPEED_ID = EuropaRPG.id("rpg_break_speed");
    private static final Identifier SCALE_ID = EuropaRPG.id("rpg_race_scale");

    private static final Map<UUID, NbtElement> LAST_FULL_SYNC = new HashMap<>();
    private static final Map<UUID, Long> LAST_COMBAT = new HashMap<>();

    private Progression() {}

    public static int xpForNextLevel(int level) {
        return 100 + 25 * level * level;
    }

    public static Resource resource(PlayerRpgData d) {
        return d.rpgClass.resource;
    }

    /** Máximo do recurso da classe. Para recursos de poucas unidades (Oportunidade, Cargas) é fixo. */
    public static float maxMana(PlayerRpgData d) {
        Resource r = resource(d);
        if (r.smallPool()) return r.baseMax;
        Race race = Races.of(d);
        float raceMana = race == null ? 0 : race.traits().mana();
        if (r != Resource.MANA) return r.baseMax + 0.5f * (float) Talents.total(d, TalentEffect.MAX_MANA) + 0.5f * d.subclass.stats.mana();
        float mana = BASE_MANA + 2f * d.level;
        mana += d.rpgClass.stats.mana() + d.rpgClass.stats.manaPerLevel() * d.level;
        mana += d.subclass.stats.mana();
        mana += (float) Talents.total(d, TalentEffect.MAX_MANA);
        return Math.max(20f, mana + raceMana);
    }

    public static float manaRegen(PlayerRpgData d) {
        Resource r = resource(d);
        Race race = Races.of(d);
        float raceRegen = race == null ? 0 : race.traits().manaRegen();
        float talent = (float) Talents.total(d, TalentEffect.MANA_REGEN);
        return switch (r) {
            case MANA -> d.rpgClass.stats.manaRegen() + talent + raceRegen;
            case FURY, OPPORTUNITY -> 0f;
            case CHARGES -> r.regen;
            default -> r.regen + talent * 0.5f + raceRegen;
        };
    }

    /** Multiplicador de poder: nível + talentos + raça. Afixos de equipamento entram em {@link #powerScale(ServerPlayerEntity, PlayerRpgData)}. */
    public static float powerScale(PlayerRpgData d) {
        Race race = Races.of(d);
        return (float) (1f + 0.04f * (d.level - 1) + Talents.total(d, TalentEffect.ABILITY_POWER) + (race == null ? 0 : race.traits().power()));
    }

    public static float powerScale(ServerPlayerEntity p, PlayerRpgData d) {
        return powerScale(d) + (float) RarityRoller.equipped(p, "power");
    }

    public static void addXp(ServerPlayerEntity player, int amount) {
        PlayerRpgData d = RpgAttachments.get(player);
        if (!d.hasClass() || amount <= 0 || d.level >= MAX_LEVEL) return;
        Race race = Races.of(d);
        if (race != null) amount = (int) Math.round(amount * (1 + race.traits().xp()));
        d.xp += amount;
        boolean leveled = false;
        while (d.level < MAX_LEVEL && d.xp >= xpForNextLevel(d.level)) {
            d.xp -= xpForNextLevel(d.level);
            d.level++;
            leveled = true;
        }
        if (d.level >= MAX_LEVEL) d.xp = 0;
        if (leveled) onLevelUp(player, d);
    }

    private static void onLevelUp(ServerPlayerEntity player, PlayerRpgData d) {
        applyAttributes(player);
        player.setHealth(player.getMaxHealth());
        if (!resource(d).combatBuilt) d.mana = maxMana(d);
        player.sendMessage(Text.translatable("rpg.europa.level_up", d.level).formatted(Formatting.GOLD, Formatting.BOLD));
        player.sendMessage(Text.translatable("rpg.europa.talent_point", Talents.availablePoints(d)).formatted(Formatting.YELLOW), true);
        player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.8f, 1.2f);
        player.getServerWorld().spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 40, 0.5, 1, 0.5, 0.3);
        if (d.level == SUBCLASS_LEVEL && d.subclass == RpgSubclass.NONE) Messages.sendSubclassChoice(player, d);
        if (d.level == MAX_LEVEL) player.sendMessage(Text.translatable("rpg.europa.max_level_title").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
    }

    /** Recalcula e aplica todos os modificadores de atributo do RPG. Idempotente. */
    public static void applyAttributes(ServerPlayerEntity player) {
        PlayerRpgData d = RpgAttachments.get(player);
        int lvl = d.level;
        StatBlock c = d.rpgClass.stats, s = d.subclass.stats;
        double health = c.health() + c.healthPerLevel() * lvl + s.health() + Talents.total(d, TalentEffect.MAX_HEALTH);
        double armor = c.armor() + c.armorPerLevel() * lvl + s.armor() + Talents.total(d, TalentEffect.ARMOR);
        double toughness = Talents.total(d, TalentEffect.TOUGHNESS);
        double knockback = 0.05 * Talents.total(d, TalentEffect.TOUGHNESS);
        double damage = c.damage() + c.damagePerLevel() * lvl + s.damage() + Talents.total(d, TalentEffect.ATTACK_DAMAGE);
        double speed = c.speed() + s.speed() + Professions.travelSpeed(d);
        double attackSpeed = c.attackSpeed() + s.attackSpeed();
        double luck = Professions.fishingLuck(d);
        double breakSpeed = 0.004 * (Professions.level(d, com.europa.rpg.profession.Profession.ANOMALY_GEOLOGIST) - 1);
        double scale = 0;

        Race race = Races.of(d);
        if (race != null) {
            Race.Traits t = race.traits();
            health += t.health();
            armor += t.armor();
            toughness += t.toughness();
            damage += t.damage();
            speed += t.speed();
            luck += t.luck();
            scale = race.scale() - 1.0;
        }
        if (Blessings.has(d, Patron.AUREON, 2)) health += 4;
        if (Blessings.has(d, Patron.VAELOR, 1)) damage += 1;
        if (Blessings.has(d, Patron.THALOR, 2)) { armor += 2; toughness += 1; }
        if (Blessings.has(d, Patron.THALOR, 3)) breakSpeed += 0.2;
        if (Blessings.has(d, Patron.SYLVARA, 1)) luck += 2;

        set(player, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_ID, Math.floor(health), EntityAttributeModifier.Operation.ADD_VALUE);
        set(player, EntityAttributes.GENERIC_ARMOR, ARMOR_ID, armor, EntityAttributeModifier.Operation.ADD_VALUE);
        set(player, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, TOUGHNESS_ID, toughness, EntityAttributeModifier.Operation.ADD_VALUE);
        set(player, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, KNOCKBACK_ID, knockback, EntityAttributeModifier.Operation.ADD_VALUE);
        set(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE_ID, damage, EntityAttributeModifier.Operation.ADD_VALUE);
        set(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_ID, speed, EntityAttributeModifier.Operation.ADD_VALUE);
        set(player, EntityAttributes.GENERIC_ATTACK_SPEED, ATTACK_SPEED_ID, attackSpeed, EntityAttributeModifier.Operation.ADD_VALUE);
        set(player, EntityAttributes.GENERIC_LUCK, LUCK_ID, luck, EntityAttributeModifier.Operation.ADD_VALUE);
        set(player, EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, BREAK_SPEED_ID, breakSpeed, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        set(player, EntityAttributes.GENERIC_SCALE, SCALE_ID, scale, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void set(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attr, Identifier id, double value,
                            EntityAttributeModifier.Operation op) {
        EntityAttributeInstance inst = player.getAttributeInstance(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (value != 0) inst.addPersistentModifier(new EntityAttributeModifier(id, value, op));
    }

    // ------------------------------------------------------------------ recursos

    public static void markCombat(ServerPlayerEntity p) {
        LAST_COMBAT.put(p.getUuid(), p.getServer().getOverworld().getTime());
    }

    /** Ganho de recurso construído em combate (Fúria, Oportunidade, Espírito, Fé). */
    public static void gain(ServerPlayerEntity p, Resource onlyFor, float amount) {
        PlayerRpgData d = RpgAttachments.get(p);
        if (resource(d) != onlyFor) return;
        d.mana = Math.min(maxMana(d), d.mana + amount);
    }

    /** Chamado a cada tick do servidor por jogador. */
    public static void tick(ServerPlayerEntity player, long now) {
        PlayerRpgData d = RpgAttachments.get(player);
        if (d.hasClass()) {
            Resource r = resource(d);
            float max = maxMana(d);
            if (r.combatBuilt) {
                long last = LAST_COMBAT.getOrDefault(player.getUuid(), 0L);
                if (now - last > COMBAT_GRACE && d.mana > 0) d.mana = Math.max(0, d.mana - (r == Resource.FURY ? 3f : 0.5f) / 20f);
            } else if (d.mana < max) {
                float regen = manaRegen(d);
                if (r == Resource.BOND && now % 20 == 0) d.mana += Summons.owned(player).size();
                d.mana = Math.min(max, d.mana + regen / 20f);
            }
            if (d.mana > max) d.mana = max;
            double regen = Talents.total(d, TalentEffect.REGEN);
            if (regen > 0 && now % 100 == 0 && player.getHealth() < player.getMaxHealth()) player.heal((float) regen);
        }
        if (now % 20 == 0) {
            Blessings.tick(player, d, now);
            Races.tick(player, d);
            Professions.tick(player);
            setBonuses(player);
        }
        if (now % 5 == 0) sync(player);
        if (now % 10 == 0) fullSync(player, false);
    }

    /** Bônus de conjunto: 4 peças do mesmo material. */
    private static void setBonuses(ServerPlayerEntity p) {
        RegistryEntry<ArmorMaterial> mat = null;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack s = p.getEquippedStack(slot);
            if (!(s.getItem() instanceof ArmorItem a)) return;
            if (mat == null) mat = a.getMaterial();
            else if (!mat.equals(a.getMaterial())) return;
        }
        if (mat == null) return;
        if (mat.equals(ModArmorMaterials.MITHRIL)) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 0, true, false, true));
            gain(p, resource(RpgAttachments.get(p)), 1f);
        } else if (mat.equals(ModArmorMaterials.ADAMANTITE)) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 0, true, false, true));
        }
    }

    // ------------------------------------------------------------------ sincronização

    /** Estado leve (HUD) — enviado com frequência. */
    public static void sync(ServerPlayerEntity player) {
        if (!ServerPlayNetworking.canSend(player, RpgSyncPayload.ID)) return; // cliente sem o mod
        PlayerRpgData d = RpgAttachments.get(player);
        long now = player.getServer().getOverworld().getTime();
        AbilityDef primary = AbilityHandler.abilityFor(d, AbilityHandler.PRIMARY);
        AbilityDef ultimate = AbilityHandler.abilityFor(d, AbilityHandler.ULTIMATE);
        AbilityDef racial = AbilityHandler.abilityFor(d, AbilityHandler.RACIAL);
        ServerPlayNetworking.send(player, new RpgSyncPayload(
                d.rpgClass.id(), d.subclass.id(), resource(d).id(), d.level, d.xp,
                d.level >= MAX_LEVEL ? 0 : xpForNextLevel(d.level),
                d.mana, maxMana(d),
                (int) d.cooldownRemaining("primary", now), primary == null ? 0 : AbilityHandler.effectiveCooldown(d, primary),
                (int) d.cooldownRemaining("ultimate", now), ultimate == null ? 0 : AbilityHandler.effectiveCooldown(d, ultimate),
                (int) d.cooldownRemaining("racial", now), racial == null ? 0 : AbilityHandler.effectiveCooldown(d, racial)));
    }

    /** Estado completo (tela de personagem) — enviado apenas quando muda, ou quando forçado. */
    public static void fullSync(ServerPlayerEntity player, boolean force) {
        if (!ServerPlayNetworking.canSend(player, RpgFullSyncPayload.ID)) return;
        PlayerRpgData d = RpgAttachments.get(player);
        NbtElement encoded = PlayerRpgData.CODEC.encodeStart(NbtOps.INSTANCE, d).getOrThrow();
        if (!(encoded instanceof NbtCompound nbt)) return;
        NbtCompound comparable = nbt.copy();
        comparable.remove("mana");
        comparable.remove("cooldowns");
        if (!force && comparable.equals(LAST_FULL_SYNC.get(player.getUuid()))) return;
        LAST_FULL_SYNC.put(player.getUuid(), comparable);
        ServerPlayNetworking.send(player, new RpgFullSyncPayload(nbt));
    }

    public static void forget(ServerPlayerEntity player) {
        LAST_FULL_SYNC.remove(player.getUuid());
        LAST_COMBAT.remove(player.getUuid());
    }
}
