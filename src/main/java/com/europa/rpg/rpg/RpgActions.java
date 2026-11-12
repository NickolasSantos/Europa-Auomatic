package com.europa.rpg.rpg;

import com.europa.rpg.talent.TalentNode;
import com.europa.rpg.talent.Talents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Ações do jogador, usadas tanto pelos comandos quanto pela tela de personagem. */
public final class RpgActions {
    private RpgActions() {}

    public static boolean chooseClass(ServerPlayerEntity p, String id) {
        PlayerRpgData d = RpgAttachments.get(p);
        RpgClass c = RpgClass.byId(id);
        if (c == RpgClass.NONE) return fail(p, "rpg.europa.unknown_class");
        if (d.hasClass()) return fail(p, "rpg.europa.already_class");
        d.rpgClass = c;
        d.subclass = RpgSubclass.NONE;
        d.mana = c.resource.combatBuilt ? 0 : Progression.maxMana(d);
        Progression.applyAttributes(p);
        p.setHealth(p.getMaxHealth());
        Progression.sync(p);
        p.sendMessage(Text.translatable("rpg.europa.class_chosen", Text.translatable(c.translationKey()).formatted(c.color)).formatted(Formatting.GOLD));
        p.sendMessage(Text.translatable("rpg.europa.controls").formatted(Formatting.GRAY));
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.5f, 1.4f);
        if (d.level >= Progression.SUBCLASS_LEVEL) Messages.sendSubclassChoice(p, d);
        return true;
    }

    public static boolean chooseSubclass(ServerPlayerEntity p, String id) {
        PlayerRpgData d = RpgAttachments.get(p);
        RpgSubclass s = RpgSubclass.byId(id);
        if (s == RpgSubclass.NONE || s.parent != d.rpgClass) return fail(p, "rpg.europa.unknown_subclass");
        if (d.subclass != RpgSubclass.NONE) return fail(p, "rpg.europa.already_subclass");
        if (d.level < Progression.SUBCLASS_LEVEL) {
            p.sendMessage(Text.translatable("rpg.europa.no_subclass", Progression.SUBCLASS_LEVEL).formatted(Formatting.RED));
            return false;
        }
        d.subclass = s;
        Progression.applyAttributes(p);
        Progression.sync(p);
        p.sendMessage(Text.translatable("rpg.europa.subclass_chosen", Text.translatable(s.translationKey()).formatted(s.color)).formatted(Formatting.LIGHT_PURPLE));
        return true;
    }

    public static boolean unlockTalent(ServerPlayerEntity p, String id) {
        PlayerRpgData d = RpgAttachments.get(p);
        TalentNode n = Talents.get(id);
        if (n == null) return false;
        Talents.Status st = Talents.canUnlock(d, n);
        if (st != Talents.Status.OK) {
            p.sendMessage(Text.translatable("rpg.europa.talent.status." + st.name().toLowerCase(java.util.Locale.ROOT)).formatted(Formatting.RED), true);
            return false;
        }
        Talents.unlock(d, n);
        Progression.applyAttributes(p);
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.6f, 1.5f);
        return true;
    }

    public static boolean chooseRace(ServerPlayerEntity p, String id) {
        PlayerRpgData d = RpgAttachments.get(p);
        com.europa.rpg.race.Race race = com.europa.rpg.race.Race.byId(id);
        if (race == null) return fail(p, "rpg.europa.unknown_race");
        if (d.hasRace()) return fail(p, "rpg.europa.already_race");
        d.race = race.id();
        if (!Progression.resource(d).combatBuilt) d.mana = Math.min(d.mana, Progression.maxMana(d));
        Progression.applyAttributes(p);
        p.sendMessage(Text.translatable("rpg.europa.race_chosen", Text.translatable(race.translationKey()).formatted(race.color())).formatted(Formatting.GOLD));
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.4f);
        return true;
    }

    /** Zera talentos (Tomo do Renascimento). */
    public static void resetTalents(ServerPlayerEntity p) {
        RpgAttachments.get(p).talents.clear();
        Progression.applyAttributes(p);
    }

    private static boolean fail(ServerPlayerEntity p, String key) {
        p.sendMessage(Text.translatable(key).formatted(Formatting.RED));
        return false;
    }
}
