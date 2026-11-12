package com.europa.rpg.command;

import com.europa.rpg.blessing.Blessings;
import com.europa.rpg.blessing.Patron;
import com.europa.rpg.profession.Profession;
import com.europa.rpg.profession.Professions;
import com.europa.rpg.quest.QuestManager;
import com.europa.rpg.quest.Quests;
import com.europa.rpg.rpg.Messages;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgActions;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.rpg.RpgSubclass;
import com.europa.rpg.talent.TalentNode;
import com.europa.rpg.talent.Talents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
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
                        .executes(ctx -> { Messages.sendClassChoice(p(ctx)); return 1; })
                        .then(argument("class", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(RpgClass.all().stream().map(RpgClass::id), b))
                                .executes(ctx -> bool(RpgActions.chooseClass(p(ctx), StringArgumentType.getString(ctx, "class"))))))
                .then(literal("subclass")
                        .executes(ctx -> { Messages.sendSubclassChoice(p(ctx), RpgAttachments.get(p(ctx))); return 1; })
                        .then(argument("subclass", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(
                                        RpgAttachments.get(c.getSource().getPlayerOrThrow()).rpgClass.subclasses().stream().map(RpgSubclass::id), b))
                                .executes(ctx -> bool(RpgActions.chooseSubclass(p(ctx), StringArgumentType.getString(ctx, "subclass"))))))
                .then(literal("talent").then(argument("talent", StringArgumentType.word())
                        .suggests((c, b) -> CommandSource.suggestMatching(Talents.all().stream().map(TalentNode::id), b))
                        .executes(ctx -> bool(RpgActions.unlockTalent(p(ctx), StringArgumentType.getString(ctx, "talent"))))))
                .then(literal("patron").then(argument("patron", StringArgumentType.word())
                        .suggests((c, b) -> CommandSource.suggestMatching(Patron.all().stream().map(Patron::id), b))
                        .executes(ctx -> bool(Blessings.pledge(p(ctx), StringArgumentType.getString(ctx, "patron"))))))
                .then(literal("race").then(argument("race", StringArgumentType.word())
                        .suggests((c, b) -> CommandSource.suggestMatching(com.europa.rpg.race.Race.all().stream().map(com.europa.rpg.race.Race::id), b))
                        .executes(ctx -> bool(RpgActions.chooseRace(p(ctx), StringArgumentType.getString(ctx, "race"))))))
                .then(literal("shop")
                        .executes(ctx -> { com.europa.rpg.economy.Market.showShop(p(ctx)); return 1; })
                        .then(literal("buy").then(argument("offer", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(com.europa.rpg.economy.Market.OFFERS.stream().map(com.europa.rpg.economy.Market.Offer::id), b))
                                .executes(ctx -> bool(com.europa.rpg.economy.Market.buy(p(ctx), StringArgumentType.getString(ctx, "offer"))))))
                        .then(literal("price").executes(ctx -> { com.europa.rpg.economy.Market.showPrice(p(ctx), p(ctx).getMainHandStack()); return 1; })))
                .then(literal("stats").executes(RpgCommand::stats))
                .then(literal("quest")
                        .executes(ctx -> { QuestManager.showBoard(p(ctx)); return 1; })
                        .then(literal("accept").then(questArg().executes(ctx -> bool(QuestManager.accept(p(ctx), q(ctx))))))
                        .then(literal("abandon").then(questArg().executes(ctx -> bool(QuestManager.abandon(p(ctx), q(ctx))))))
                        .then(literal("turnin").then(questArg().executes(ctx -> bool(QuestManager.turnIn(p(ctx), q(ctx)))))))
                .then(literal("admin").requires(s -> s.hasPermissionLevel(2))
                        .then(literal("addxp").then(argument("target", EntityArgumentType.player())
                                .then(argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                                    Progression.addXp(EntityArgumentType.getPlayer(ctx, "target"), IntegerArgumentType.getInteger(ctx, "amount"));
                                    return ok(ctx);
                                }))))
                        .then(literal("setlevel").then(argument("target", EntityArgumentType.player())
                                .then(argument("level", IntegerArgumentType.integer(1, Progression.MAX_LEVEL)).executes(ctx -> {
                                    ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "target");
                                    PlayerRpgData d = RpgAttachments.get(t);
                                    d.level = IntegerArgumentType.getInteger(ctx, "level");
                                    d.xp = 0;
                                    Progression.applyAttributes(t);
                                    Progression.sync(t);
                                    return ok(ctx);
                                }))))
                        .then(literal("profxp").then(argument("target", EntityArgumentType.player())
                                .then(argument("profession", StringArgumentType.word())
                                        .suggests((c, b) -> CommandSource.suggestMatching(Arrays.stream(Profession.values()).map(Profession::id), b))
                                        .then(argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                                            Profession prof = Profession.byId(StringArgumentType.getString(ctx, "profession"));
                                            if (prof == null) return 0;
                                            Professions.addXp(EntityArgumentType.getPlayer(ctx, "target"), prof, IntegerArgumentType.getInteger(ctx, "amount"));
                                            return ok(ctx);
                                        })))))
                        .then(literal("setfavor").then(argument("target", EntityArgumentType.player())
                                .then(argument("favor", IntegerArgumentType.integer(0, Patron.MAX_FAVOR)).executes(ctx -> {
                                    ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "target");
                                    RpgAttachments.get(t).favor = IntegerArgumentType.getInteger(ctx, "favor");
                                    Progression.applyAttributes(t);
                                    return ok(ctx);
                                }))))
                        .then(literal("addrep").then(argument("target", EntityArgumentType.player())
                                .then(argument("faction", StringArgumentType.word())
                                        .suggests((c, b) -> CommandSource.suggestMatching(com.europa.rpg.economy.Factions.ALL.stream().map(com.europa.rpg.economy.Factions.Faction::id), b))
                                        .then(argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                                            com.europa.rpg.economy.Factions.addRep(EntityArgumentType.getPlayer(ctx, "target"),
                                                    StringArgumentType.getString(ctx, "faction"), IntegerArgumentType.getInteger(ctx, "amount"));
                                            return ok(ctx);
                                        })))))
                        .then(literal("rarity").then(argument("tier", IntegerArgumentType.integer(0, 6)).executes(ctx -> {
                            var stack = p(ctx).getMainHandStack();
                            var data = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
                            if (data != null) stack.remove(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
                            com.europa.rpg.item.RarityRoller.apply(stack, com.europa.rpg.item.Rarity.values()[IntegerArgumentType.getInteger(ctx, "tier")], p(ctx).getRandom());
                            return ok(ctx);
                        })))
                        .then(literal("reset").then(argument("target", EntityArgumentType.player()).executes(ctx -> {
                            ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "target");
                            t.setAttached(RpgAttachments.PLAYER_DATA, new PlayerRpgData());
                            Progression.applyAttributes(t);
                            Progression.sync(t);
                            return ok(ctx);
                        })))));
    }

    private static int ok(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.literal("OK"), true);
        return 1;
    }

    private static RequiredArgumentBuilder<ServerCommandSource, String> questArg() {
        return argument("quest", StringArgumentType.word())
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

    private static int stats(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = p(ctx);
        PlayerRpgData d = RpgAttachments.get(p);
        p.sendMessage(Text.translatable("rpg.europa.stats.header").formatted(Formatting.GOLD, Formatting.BOLD));
        p.sendMessage(Text.translatable("rpg.europa.stats.class", Text.translatable(d.rpgClass.translationKey()).formatted(d.rpgClass.color),
                Text.translatable(d.subclass.translationKey()).formatted(d.subclass.color)));
        p.sendMessage(Text.translatable("rpg.europa.stats.level", d.level, d.xp, Progression.xpForNextLevel(d.level)));
        p.sendMessage(Text.translatable("rpg.europa.stats.mana", (int) d.mana, (int) Progression.maxMana(d)));
        p.sendMessage(Text.translatable("rpg.europa.stats.talents", Talents.availablePoints(d)));
        p.sendMessage(Text.translatable("rpg.europa.stats.quests", d.completedQuests.size(), Quests.ALL.size()));
        p.sendMessage(Text.translatable("rpg.europa.open_screen_hint").formatted(Formatting.GRAY));
        return 1;
    }
}
