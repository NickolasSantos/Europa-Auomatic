package com.europa.rpg.rpg;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Mensagens de chat clicáveis usadas como interface (escolha de classe, subclasse, missões). */
public final class Messages {
    private Messages() {}

    public static MutableText button(Text label, String command, Text hover, Formatting color) {
        return Text.literal("[").append(label).append("]").styled(s -> s
                .withColor(color)
                .withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
    }

    public static void sendClassChoice(ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("rpg.europa.choose_class").formatted(Formatting.GOLD, Formatting.BOLD));
        player.sendMessage(Text.translatable("rpg.europa.open_screen_hint").formatted(Formatting.GRAY));
        for (RpgClass c : RpgClass.all()) {
            String ability = "rpg.europa.ability." + c.primaryAbility;
            MutableText hover = Text.translatable(c.translationKey() + ".desc").append("\n")
                    .append(Text.translatable("rpg.europa.ability_label").formatted(Formatting.YELLOW))
                    .append(Text.translatable(ability)).append("\n")
                    .append(Text.translatable(ability + ".desc").formatted(Formatting.GRAY));
            player.sendMessage(Text.literal("  ").append(button(Text.translatable(c.translationKey()),
                    "/europa class " + c.id(), hover, c.color)));
        }
    }

    public static void sendSubclassChoice(ServerPlayerEntity player, PlayerRpgData d) {
        player.sendMessage(Text.translatable("rpg.europa.choose_subclass").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        for (RpgSubclass s : d.rpgClass.subclasses()) {
            MutableText hover = Text.translatable(s.translationKey() + ".desc").append("\n")
                    .append(Text.translatable("rpg.europa.ultimate_label").formatted(Formatting.YELLOW))
                    .append(Text.translatable("rpg.europa.ability." + s.ultimateAbility)).append("\n")
                    .append(Text.translatable("rpg.europa.ability." + s.ultimateAbility + ".desc").formatted(Formatting.GRAY));
            player.sendMessage(Text.literal("  ").append(button(Text.translatable(s.translationKey()),
                    "/europa subclass " + s.id(), hover, s.color)).append(" ")
                    .append(Text.translatable(s.translationKey() + ".desc").formatted(Formatting.GRAY)));
        }
    }
}
