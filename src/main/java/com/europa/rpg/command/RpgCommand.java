package com.europa.rpg.command;

import com.europa.rpg.quest.QuestManager;
import com.europa.rpg.quest.Quests;
import com.europa.rpg.rpg.Messages;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.rpg.RpgSubclass;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RpgCommand {
    private RpgCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("europa")
                .then(literal("class")
                        .executes(ctx -> { Messages.sendClassChoice(ctx.getSource().getPlayerOrThrow()); return 1; })
                        .then(argument("class", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(Arrays.stream(RpgClass.values())
                                        .filter(x -> x != RpgClass.NONE).map(RpgClass::id), b))
                                .executes(RpgCommand::chooseClass)))
                .then(literal("subclass")
                        .executes(ctx -> {
                            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                            Messages.sendSubclassChoice(p, RpgAttachments.get(p));
                            return 1;
                        })
                        .then(argument("subclass", StringArgumentType.word())
                                .suggests((c, b) -> {
                                    PlayerRpgData d = RpgAttachments.get(c.getSource().getPlayerOrThrow());
                                    return CommandSource.suggestMatching(Arrays.stream(d.rpgClass.subclasses()).map(RpgSubclass::id), b);
                                })
                                .executes(RpgCommand::chooseSubclass)))
                .then(literal("stats").executes(RpgCommand::stats))
                .then(literal("quest")
                        .executes(ctx -> { QuestManager.showBoard(ctx.getSource().getPlayerOrThrow()); return 1; })
                        .then(literal("accept").then(questArg().executes(ctx -> bool(QuestManager.accept(p(ctx), q(ctx))))))
                        .then(literal("abandon").then(questArg().executes(ctx -> bool(QuestManager.abandon(p(ctx), q(ctx))))))
                        .then(literal("turnin").then(questArg().executes(ctx -> bool(QuestManager.turnIn(p(ctx), q(ctx)))))))
                .then(literal("admin").requires(s -> s.hasPermissionLevel(2))
                        .then(literal("addxp").then(argument("target", EntityArgumentType.player())
                                .then(argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                                    ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "target");
                                    Progression.addXp(t, IntegerArgumentType.getInteger(ctx, "amount"));
                                    ctx.getSource().sendFeedback(() -> Text.literal("OK"), true);
                                    return 1;
                                }))))
                        .then(literal("setlevel").then(argument("target", EntityArgumentType.player())
                                .then(argument("level", IntegerArgumentType.integer(1, Progression.MAX_LEVEL)).executes(ctx -> {
                                    ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "target");
                                    PlayerRpgData d = RpgAttachments.get(t);
                                    d.level = IntegerArgumentType.getInteger(ctx, "level");
                                    d.xp = 0;
                                    Progression.applyAttributes(t);
                                    Progression.sync(t);
                                    ctx.getSource().sendFeedback(() -> Text.literal("OK"), true);
                                    return 1;
                                }))))
                        .then(literal("reset").then(argument("target", EntityArgumentType.player()).executes(ctx -> {
                            ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "target");
                            t.setAttached(RpgAttachments.PLAYER_DATA, new PlayerRpgData());
                            Progression.applyAttributes(t);
                            Progression.sync(t);
                            ctx.getSource().sendFeedback(() -> Text.literal("OK"), true);
                            return 1;
                        })))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String> questArg() {
        return CommandManager.argument("quest", StringArgumentType.word())
                .suggests((c, b) -> CommandSource.suggestMatching(Quests.ALL.keySet(), b));
    }

    private static ServerPlayerEntity p(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrThrow();
    }

    private static String q(CommandContext<ServerCommandSource> ctx) {
        return StringArgumentType.getString(ctx, "quest");
    }

    private static int bool(boolean b) {
        return b ? 1 : 0;
    }

    private static int chooseClass(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        PlayerRpgData d = RpgAttachments.get(p);
        RpgClass c = RpgClass.byId(StringArgumentType.getString(ctx, "class"));
        if (c == RpgClass.NONE) {
            p.sendMessage(Text.translatable("rpg.europa.unknown_class").formatted(Formatting.RED));
            return 0;
        }
        if (d.hasClass()) {
            p.sendMessage(Text.translatable("rpg.europa.already_class").formatted(Formatting.RED));
            return 0;
        }
        d.rpgClass = c;
        d.subclass = RpgSubclass.NONE;
        d.mana = Progression.maxMana(d);
        Progression.applyAttributes(p);
        p.setHealth(p.getMaxHealth());
        Progression.sync(p);
        p.sendMessage(Text.translatable("rpg.europa.class_chosen", Text.translatable(c.translationKey()).formatted(c.color)).formatted(Formatting.GOLD));
        p.sendMessage(Text.translatable("rpg.europa.controls").formatted(Formatting.GRAY));
        if (d.level >= Progression.SUBCLASS_LEVEL) Messages.sendSubclassChoice(p, d);
        return 1;
    }

    private static int chooseSubclass(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        PlayerRpgData d = RpgAttachments.get(p);
        RpgSubclass s = RpgSubclass.byId(StringArgumentType.getString(ctx, "subclass"));
        if (s == RpgSubclass.NONE || s.parent != d.rpgClass) {
            p.sendMessage(Text.translatable("rpg.europa.unknown_subclass").formatted(Formatting.RED));
            return 0;
        }
        if (d.subclass != RpgSubclass.NONE) {
            p.sendMessage(Text.translatable("rpg.europa.already_subclass").formatted(Formatting.RED));
            return 0;
        }
        if (d.level < Progression.SUBCLASS_LEVEL) {
            p.sendMessage(Text.translatable("rpg.europa.no_subclass", Progression.SUBCLASS_LEVEL).formatted(Formatting.RED));
            return 0;
        }
        d.subclass = s;
        Progression.applyAttributes(p);
        Progression.sync(p);
        p.sendMessage(Text.translatable("rpg.europa.subclass_chosen", Text.translatable(s.translationKey()).formatted(s.color)).formatted(Formatting.LIGHT_PURPLE));
        return 1;
    }

    private static int stats(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        PlayerRpgData d = RpgAttachments.get(p);
        p.sendMessage(Text.translatable("rpg.europa.stats.header").formatted(Formatting.GOLD, Formatting.BOLD));
        p.sendMessage(Text.translatable("rpg.europa.stats.class", Text.translatable(d.rpgClass.translationKey()).formatted(d.rpgClass.color),
                Text.translatable(d.subclass.translationKey()).formatted(d.subclass.color)));
        p.sendMessage(Text.translatable("rpg.europa.stats.level", d.level, d.xp, Progression.xpForNextLevel(d.level)));
        p.sendMessage(Text.translatable("rpg.europa.stats.mana", (int) d.mana, (int) Progression.maxMana(d)));
        p.sendMessage(Text.translatable("rpg.europa.stats.quests", d.completedQuests.size(), Quests.ALL.size()));
        return 1;
    }
}
