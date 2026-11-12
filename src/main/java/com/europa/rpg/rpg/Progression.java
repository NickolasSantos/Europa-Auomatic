package com.europa.rpg.rpg;

import com.europa.rpg.EuropaRPG;
import com.europa.rpg.network.RpgSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/** XP, níveis, mana e bônus de atributos por classe/subclasse/nível. */
public final class Progression {
    public static final int MAX_LEVEL = 50;
    public static final int SUBCLASS_LEVEL = 10;
    public static final float BASE_MANA = 100f;

    private static final Identifier HEALTH_ID = EuropaRPG.id("rpg_health");
    private static final Identifier ARMOR_ID = EuropaRPG.id("rpg_armor");
    private static final Identifier DAMAGE_ID = EuropaRPG.id("rpg_damage");
    private static final Identifier SPEED_ID = EuropaRPG.id("rpg_speed");
    private static final Identifier ATTACK_SPEED_ID = EuropaRPG.id("rpg_attack_speed");

    private Progression() {}

    public static int xpForNextLevel(int level) {
        return 100 + 25 * level * level;
    }

    public static float maxMana(PlayerRpgData d) {
        float mana = BASE_MANA + 2f * d.level;
        if (d.rpgClass == RpgClass.MAGE) mana += 50f + 4f * d.level;
        if (d.subclass == RpgSubclass.PYROMANCER || d.subclass == RpgSubclass.CRYOMANCER) mana += 50f;
        return mana;
    }

    /** Multiplicador de dano de habilidades que escala com o nível. */
    public static float powerScale(PlayerRpgData d) {
        return 1f + 0.04f * (d.level - 1);
    }

    public static void addXp(ServerPlayerEntity player, int amount) {
        PlayerRpgData d = RpgAttachments.get(player);
        if (!d.hasClass() || amount <= 0 || d.level >= MAX_LEVEL) return;
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
        d.mana = maxMana(d);
        player.sendMessage(Text.translatable("rpg.europa.level_up", d.level).formatted(Formatting.GOLD, Formatting.BOLD));
        player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.8f, 1.2f);
        player.getServerWorld().spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 40, 0.5, 1, 0.5, 0.3);
        if (d.level == SUBCLASS_LEVEL && d.subclass == RpgSubclass.NONE) {
            Messages.sendSubclassChoice(player, d);
        }
    }

    /** Recalcula e aplica todos os modificadores de atributo do RPG. Idempotente. */
    public static void applyAttributes(ServerPlayerEntity player) {
        PlayerRpgData d = RpgAttachments.get(player);
        double health = 0, armor = 0, damage = 0, speed = 0, attackSpeed = 0;
        int lvl = d.level;
        switch (d.rpgClass) {
            case WARRIOR -> { health = 4 + 0.5 * lvl; armor = 2 + 0.1 * lvl; damage = 0.05 * lvl; }
            case MAGE -> health = 0.25 * lvl;
            case ROGUE -> { health = 0.3 * lvl; speed = 0.01; damage = 0.04 * lvl; attackSpeed = 0.3; }
            case RANGER -> { health = 2 + 0.35 * lvl; speed = 0.005; }
            case NONE -> {}
        }
        switch (d.subclass) {
            case BERSERKER -> damage += 2;
            case PALADIN -> { armor += 4; health += 4; }
            case ASSASSIN -> damage += 1.5;
            case SHADOWDANCER -> speed += 0.01;
            case BEASTMASTER -> health += 4;
            default -> {}
        }
        set(player, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_ID, Math.floor(health));
        set(player, EntityAttributes.GENERIC_ARMOR, ARMOR_ID, armor);
        set(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE_ID, damage);
        set(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_ID, speed);
        set(player, EntityAttributes.GENERIC_ATTACK_SPEED, ATTACK_SPEED_ID, attackSpeed);
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void set(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attr, Identifier id, double value) {
        EntityAttributeInstance inst = player.getAttributeInstance(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (value != 0) {
            inst.addPersistentModifier(new EntityAttributeModifier(id, value, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    /** Chamado a cada tick do servidor por jogador. */
    public static void tick(ServerPlayerEntity player, long now) {
        PlayerRpgData d = RpgAttachments.get(player);
        if (!d.hasClass()) {
            if (now % 20 == 0) sync(player);
            return;
        }
        float max = maxMana(d);
        if (d.mana < max) d.mana = Math.min(max, d.mana + d.rpgClass.manaRegen / 20f);
        if (d.mana > max) d.mana = max;
        if (now % 5 == 0) sync(player);
    }

    public static void sync(ServerPlayerEntity player) {
        if (!ServerPlayNetworking.canSend(player, RpgSyncPayload.ID)) return; // cliente sem o mod
        PlayerRpgData d = RpgAttachments.get(player);
        long now = player.getServer().getOverworld().getTime();
        ServerPlayNetworking.send(player, new RpgSyncPayload(
                d.rpgClass.id(), d.subclass.id(), d.level, d.xp,
                d.level >= MAX_LEVEL ? 0 : xpForNextLevel(d.level),
                d.mana, maxMana(d),
                (int) d.cooldownRemaining("primary", now), d.rpgClass.primaryCooldown,
                (int) d.cooldownRemaining("ultimate", now), d.subclass.ultimateCooldown));
    }
}
