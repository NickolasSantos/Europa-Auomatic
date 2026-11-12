package com.europa.rpg.ability;

import com.europa.rpg.race.Race;
import com.europa.rpg.race.Races;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.Resource;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.rpg.RpgSubclass;
import com.europa.rpg.talent.TalentEffect;
import com.europa.rpg.talent.Talents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Valida e executa habilidades. Slots: 0 primária (R), 1 suprema (G), 2 racial (V). */
public final class AbilityHandler {
    public static final int PRIMARY = 0;
    public static final int ULTIMATE = 1;
    public static final int RACIAL = 2;
    private static final String[] KEYS = {"primary", "ultimate", "racial"};

    private AbilityHandler() {}

    public static AbilityDef abilityFor(PlayerRpgData d, int slot) {
        String id = switch (slot) {
            case PRIMARY -> d.rpgClass.primaryAbility;
            case ULTIMATE -> d.subclass.ultimateAbility;
            default -> {
                Race r = Races.of(d);
                yield r == null ? "" : r.ability();
            }
        };
        return id.isEmpty() ? null : Abilities.get(id);
    }

    /** Custo na escala do recurso da classe (Oportunidade/Cargas usam poucas unidades). */
    public static int effectiveCost(PlayerRpgData d, AbilityDef def) {
        if (def.manaCost() <= 0) return 0;
        Resource r = Progression.resource(d);
        double reduction = Math.min(0.5, Talents.total(d, TalentEffect.MANA_COST));
        return Math.max(1, (int) Math.ceil(def.manaCost() * r.costScale * (1 - reduction)));
    }

    public static int effectiveCooldown(PlayerRpgData d, AbilityDef def) {
        Race r = Races.of(d);
        double reduction = Math.min(0.5, Talents.total(d, TalentEffect.COOLDOWN) + (r == null ? 0 : r.traits().cooldown()));
        return (int) Math.ceil(def.cooldown() * (1 - reduction));
    }

    public static void tryCast(ServerPlayerEntity player, int slot) {
        if (player.isSpectator() || !player.isAlive() || slot < 0 || slot > 2) return;
        PlayerRpgData d = RpgAttachments.get(player);
        if (slot == RACIAL) {
            if (!d.hasRace()) {
                player.sendMessage(Text.translatable("rpg.europa.no_race").formatted(Formatting.RED), true);
                return;
            }
        } else if (!d.hasClass()) {
            player.sendMessage(Text.translatable("rpg.europa.no_class").formatted(Formatting.RED), true);
            return;
        }
        if (slot == ULTIMATE && d.subclass == RpgSubclass.NONE) {
            player.sendMessage(Text.translatable("rpg.europa.no_subclass", Progression.SUBCLASS_LEVEL).formatted(Formatting.RED), true);
            return;
        }
        AbilityDef def = abilityFor(d, slot);
        if (def == null) return;
        String key = KEYS[slot];
        long now = player.getServer().getOverworld().getTime();
        long remaining = d.cooldownRemaining(key, now);
        if (remaining > 0) {
            player.sendMessage(Text.translatable("rpg.europa.on_cooldown", String.format("%.1f", remaining / 20f)).formatted(Formatting.RED), true);
            return;
        }
        int cost = effectiveCost(d, def);
        if (!player.isCreative() && d.mana < cost) {
            player.sendMessage(Text.translatable("rpg.europa.no_resource", Text.translatable(Progression.resource(d).translationKey())).formatted(Formatting.BLUE), true);
            return;
        }
        if (!def.action().cast(player, d, Progression.powerScale(player, d))) return;
        if (!player.isCreative()) d.mana -= cost;
        boolean overload = slot != RACIAL && player.getRandom().nextDouble() < Talents.total(d, TalentEffect.OVERLOAD);
        if (overload) player.sendMessage(Text.translatable("rpg.europa.overload").formatted(Formatting.LIGHT_PURPLE), true);
        else d.cooldowns.put(key, now + effectiveCooldown(d, def));
        Progression.sync(player);
    }
}
