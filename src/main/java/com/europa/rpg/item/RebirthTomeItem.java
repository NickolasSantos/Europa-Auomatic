package com.europa.rpg.item;

import com.europa.rpg.rpg.Messages;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.rpg.RpgSubclass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Tomo do Renascimento: redefine classe e subclasse (mantém nível e missões). */
public class RebirthTomeItem extends Item {
    public RebirthTomeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user instanceof ServerPlayerEntity p) {
            PlayerRpgData d = RpgAttachments.get(p);
            if (!d.hasClass()) return TypedActionResult.fail(stack);
            d.rpgClass = RpgClass.NONE;
            d.subclass = RpgSubclass.NONE;
            d.cooldowns.clear();
            Progression.applyAttributes(p);
            Progression.sync(p);
            stack.decrementUnlessCreative(1, p);
            world.playSound(null, p.getBlockPos(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.6f, 1.4f);
            p.sendMessage(Text.translatable("rpg.europa.rebirth").formatted(Formatting.LIGHT_PURPLE));
            Messages.sendClassChoice(p);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.europa.rebirth_tome.tooltip").formatted(Formatting.GRAY, Formatting.ITALIC));
    }
}
