package com.europa.rpg.ability;

import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.rpg.RpgSubclass;
import com.europa.rpg.talent.TalentEffect;
import com.europa.rpg.talent.Talents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Valida e executa habilidades. Slot 0 = primária (classe, tecla R), slot 1 = suprema (subclasse, tecla G). */
public final class AbilityHandler {
    public static final int PRIMARY = 0;
    public static final int ULTIMATE = 1;

    private AbilityHandler() {}

    public static AbilityDef abilityFor(PlayerRpgData d, int slot) {
        String id = slot == PRIMARY ? d.rpgClass.primaryAbility : d.subclass.ultimateAbility;
        return id.isEmpty() ? null : Abilities.get(id);
    }

    public static int effectiveCost(PlayerRpgData d, AbilityDef def) {
        double reduction = Math.min(0.5, Talents.total(d, TalentEffect.MANA_COST));
        return (int) Math.ceil(def.manaCost() * (1 - reduction));
    }

    public static int effectiveCooldown(PlayerRpgData d, AbilityDef def) {
        double reduction = Math.min(0.5, Talents.total(d, TalentEffect.COOLDOWN));
        return (int) Math.ceil(def.cooldown() * (1 - reduction));
    }

    public static void tryCast(ServerPlayerEntity player, int slot) {
        if (player.isSpectator() || !player.isAlive()) return;
        PlayerRpgData d = RpgAttachments.get(player);
        if (!d.hasClass()) {
            player.sendMessage(Text.translatable("rpg.europa.no_class").formatted(Formatting.RED), true);
            return;
        }
        if (slot == ULTIMATE && d.subclass == RpgSubclass.NONE) {
            player.sendMessage(Text.translatable("rpg.europa.no_subclass", Progression.SUBCLASS_LEVEL).formatted(Formatting.RED), true);
            return;
        }
        AbilityDef def = abilityFor(d, slot);
        if (def == null) return;
        String key = slot == PRIMARY ? "primary" : "ultimate";
        long now = player.getServer().getOverworld().getTime();
        long remaining = d.cooldownRemaining(key, now);
        if (remaining > 0) {
            player.sendMessage(Text.translatable("rpg.europa.on_cooldown", String.format("%.1f", remaining / 20f)).formatted(Formatting.RED), true);
            return;
        }
        int cost = effectiveCost(d, def);
        if (!player.isCreative() && d.mana < cost) {
            player.sendMessage(Text.translatable("rpg.europa.no_mana").formatted(Formatting.BLUE), true);
            return;
        }
        if (!def.action().cast(player, d, Progression.powerScale(d))) return;
        if (!player.isCreative()) d.mana -= cost;
        boolean overload = player.getRandom().nextDouble() < Talents.total(d, TalentEffect.OVERLOAD);
        if (overload) {
            player.sendMessage(Text.translatable("rpg.europa.overload").formatted(Formatting.LIGHT_PURPLE), true);
        } else {
            d.cooldowns.put(key, now + effectiveCooldown(d, def));
        }
        Progression.sync(player);
    }
}
