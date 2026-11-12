package com.europa.rpg.quest;

import com.europa.rpg.rpg.Messages;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Map;

public final class QuestManager {
    private QuestManager() {}

    public enum Availability { AVAILABLE, ACTIVE, COMPLETED, LOCKED_LEVEL, LOCKED_PREREQ }

    public static Availability availability(PlayerRpgData d, Quest q) {
        if (d.completedQuests.contains(q.id())) return Availability.COMPLETED;
        if (d.activeQuests.containsKey(q.id())) return Availability.ACTIVE;
        if (q.prerequisite() != null && !d.completedQuests.contains(q.prerequisite())) return Availability.LOCKED_PREREQ;
        if (d.level < q.minLevel()) return Availability.LOCKED_LEVEL;
        return Availability.AVAILABLE;
    }

    /** Mostra o quadro de missões no chat, com botões clicáveis. */
    public static void showBoard(ServerPlayerEntity p) {
        PlayerRpgData d = RpgAttachments.get(p);
        if (!d.hasClass()) {
            Messages.sendClassChoice(p);
            return;
        }
        p.sendMessage(Text.translatable("rpg.europa.board.header").formatted(Formatting.GOLD, Formatting.BOLD));
        for (Quest q : Quests.ALL.values()) {
            Availability a = availability(d, q);
            MutableText line = Text.literal(" ");
            if (q.faction() != null) {
                var f = com.europa.rpg.economy.Factions.byId(q.faction());
                line.append(Text.literal("[").append(Text.translatable(f.translationKey())).append("] ").formatted(f.color()));
            }
            line.append(Text.translatable(q.titleKey()).formatted(color(a)));
            MutableText hover = Text.translatable(q.descKey()).append("\n")
                    .append(Text.translatable("rpg.europa.board.reward", q.xpReward()).formatted(Formatting.GREEN));
            if (q.fragments() > 0) hover.append("\n").append(Text.translatable("rpg.europa.board.fragments", q.fragments()).formatted(Formatting.YELLOW));
            if (q.faction() != null) hover.append("\n").append(Text.translatable("rpg.europa.board.rep", q.repReward(),
                    Text.translatable(com.europa.rpg.economy.Factions.byId(q.faction()).translationKey())).formatted(Formatting.AQUA));
            for (Quest.Reward r : q.items()) {
                Item item = Registries.ITEM.get(r.item());
                hover.append("\n - ").append(Text.literal(r.count() + "x ")).append(item.getName());
            }
            line.styled(s -> s.withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, hover)));
            switch (a) {
                case AVAILABLE -> line.append(" ").append(Messages.button(Text.translatable("rpg.europa.board.accept"),
                        "/europa quest accept " + q.id(), hover, Formatting.GREEN));
                case ACTIVE -> {
                    int progress = d.activeQuests.get(q.id());
                    line.append(Text.literal(" (" + progress + "/" + q.count() + ") ").formatted(Formatting.YELLOW));
                    if (q.type() == Quest.Type.COLLECT) {
                        line.append(Messages.button(Text.translatable("rpg.europa.board.turnin"),
                                "/europa quest turnin " + q.id(), hover, Formatting.AQUA)).append(" ");
                    }
                    line.append(Messages.button(Text.translatable("rpg.europa.board.abandon"),
                            "/europa quest abandon " + q.id(), Text.translatable("rpg.europa.board.abandon"), Formatting.RED));
                }
                case COMPLETED -> line.append(Text.literal(" ✔").formatted(Formatting.DARK_GREEN));
                case LOCKED_LEVEL -> line.append(Text.translatable("rpg.europa.board.need_level", q.minLevel()).formatted(Formatting.DARK_GRAY));
                case LOCKED_PREREQ -> line.append(Text.translatable("rpg.europa.board.need_quest",
                        Text.translatable(Quests.get(q.prerequisite()).titleKey())).formatted(Formatting.DARK_GRAY));
            }
            p.sendMessage(line);
        }
    }

    private static Formatting color(Availability a) {
        return switch (a) {
            case AVAILABLE -> Formatting.WHITE;
            case ACTIVE -> Formatting.YELLOW;
            case COMPLETED -> Formatting.DARK_GREEN;
            default -> Formatting.DARK_GRAY;
        };
    }

    public static boolean accept(ServerPlayerEntity p, String id) {
        PlayerRpgData d = RpgAttachments.get(p);
        Quest q = Quests.get(id);
        if (q == null) return fail(p, "rpg.europa.quest.unknown");
        if (!d.hasClass()) return fail(p, "rpg.europa.no_class");
        Availability a = availability(d, q);
        if (a != Availability.AVAILABLE) return fail(p, "rpg.europa.quest.unavailable");
        if (d.activeQuests.size() >= Quests.MAX_ACTIVE) {
            p.sendMessage(Text.translatable("rpg.europa.quest.too_many", Quests.MAX_ACTIVE).formatted(Formatting.RED));
            return false;
        }
        d.activeQuests.put(id, 0);
        p.sendMessage(Text.translatable("rpg.europa.quest.accepted", Text.translatable(q.titleKey())).formatted(Formatting.GREEN));
        p.sendMessage(Text.translatable(q.descKey()).formatted(Formatting.GRAY));
        return true;
    }

    public static boolean abandon(ServerPlayerEntity p, String id) {
        PlayerRpgData d = RpgAttachments.get(p);
        if (d.activeQuests.remove(id) == null) return fail(p, "rpg.europa.quest.not_active");
        p.sendMessage(Text.translatable("rpg.europa.quest.abandoned", Text.translatable(Quests.get(id).titleKey())).formatted(Formatting.GRAY));
        return true;
    }

    /** Entrega itens de uma missão de coleta (consome do inventário). */
    public static boolean turnIn(ServerPlayerEntity p, String id) {
        PlayerRpgData d = RpgAttachments.get(p);
        Quest q = Quests.get(id);
        if (q == null || !d.activeQuests.containsKey(id)) return fail(p, "rpg.europa.quest.not_active");
        if (q.type() != Quest.Type.COLLECT) return fail(p, "rpg.europa.quest.not_collect");
        Item item = Registries.ITEM.get(q.target());
        int have = d.activeQuests.get(id);
        int needed = q.count() - have;
        PlayerInventory inv = p.getInventory();
        int taken = 0;
        for (int i = 0; i < inv.size() && taken < needed; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(item)) {
                int t = Math.min(needed - taken, s.getCount());
                s.decrement(t);
                taken += t;
            }
        }
        if (taken == 0) {
            p.sendMessage(Text.translatable("rpg.europa.quest.nothing_to_turn_in", item.getName()).formatted(Formatting.RED));
            return false;
        }
        d.activeQuests.put(id, have + taken);
        progressMessage(p, q, have + taken);
        if (have + taken >= q.count()) complete(p, q);
        return true;
    }

    /** Chamado quando um jogador (ou seu lobo) mata uma entidade. */
    public static void onKill(ServerPlayerEntity p, Entity killed) {
        PlayerRpgData d = RpgAttachments.get(p);
        if (d.activeQuests.isEmpty()) return;
        Identifier type = Registries.ENTITY_TYPE.getId(killed.getType());
        for (Map.Entry<String, Integer> e : new ArrayList<>(d.activeQuests.entrySet())) {
            Quest q = Quests.get(e.getKey());
            if (q == null || q.type() != Quest.Type.KILL || !q.target().equals(type)) continue;
            int progress = e.getValue() + 1;
            d.activeQuests.put(q.id(), progress);
            progressMessage(p, q, progress);
            if (progress >= q.count()) complete(p, q);
        }
    }

    private static void progressMessage(ServerPlayerEntity p, Quest q, int progress) {
        p.sendMessage(Text.translatable(q.titleKey()).append(": " + Math.min(progress, q.count()) + "/" + q.count()).formatted(Formatting.YELLOW), true);
    }

    private static void complete(ServerPlayerEntity p, Quest q) {
        PlayerRpgData d = RpgAttachments.get(p);
        d.activeQuests.remove(q.id());
        d.completedQuests.add(q.id());
        p.sendMessage(Text.translatable("rpg.europa.quest.completed", Text.translatable(q.titleKey())).formatted(Formatting.GOLD, Formatting.BOLD));
        for (Quest.Reward r : q.items()) {
            ItemStack stack = new ItemStack(Registries.ITEM.get(r.item()), r.count());
            if (!p.getInventory().insertStack(stack)) p.dropItem(stack, false);
        }
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1f);
        if (q.fragments() > 0) com.europa.rpg.economy.Market.give(p, q.fragments());
        if (q.faction() != null) com.europa.rpg.economy.Factions.addRep(p, q.faction(), q.repReward());
        else com.europa.rpg.economy.Factions.addRep(p, "wanderers", 25);
        Progression.addXp(p, q.xpReward());
    }

    private static boolean fail(ServerPlayerEntity p, String key) {
        p.sendMessage(Text.translatable(key).formatted(Formatting.RED));
        return false;
    }
}
