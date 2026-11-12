package com.europa.rpg.item;

import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.util.Targeting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Cajado Arcano: dispara um raio mágico usando mana. Magos causam +50% de dano. */
public class ArcaneStaffItem extends Item {
    public static final int MANA_COST = 10;

    public ArcaneStaffItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack, world.isClient());
        PlayerRpgData d = RpgAttachments.get(p);
        if (!p.isCreative() && d.mana < MANA_COST) {
            p.sendMessage(Text.translatable("rpg.europa.no_mana").formatted(Formatting.BLUE), true);
            return TypedActionResult.fail(stack);
        }
        if (!p.isCreative()) d.mana -= MANA_COST;
        float dmg = 6f * Progression.powerScale(d) * (d.rpgClass == RpgClass.MAGE ? 1.5f : 1f);
        LivingEntity target = Targeting.lookTarget(p, 40);
        Vec3d end = target != null ? target.getPos().add(0, target.getHeight() / 2, 0) : Targeting.blockHitPos(p, 40);
        Targeting.particleLine(p.getServerWorld(), ParticleTypes.WITCH, p.getEyePos().subtract(0, 0.3, 0), end, 0.35);
        if (target != null) target.damage(p.getDamageSources().indirectMagic(p, p), dmg);
        world.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 0.8f, 1.5f);
        p.getItemCooldownManager().set(this, 12);
        stack.damage(1, p, LivingEntity.getSlotForHand(hand));
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.europa.arcane_staff.tooltip", MANA_COST).formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
    }
}
