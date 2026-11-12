package com.europa.rpg.item;

import com.europa.rpg.registry.ModToolMaterials;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.List;

/** Armas lendárias com efeitos únicos ao acertar. */
public class LegendaryWeaponItem extends SwordItem {
    public enum Effect { FLAME, FROST, SOUL, STORM }

    private final Effect effect;

    public LegendaryWeaponItem(Effect effect, int damage, float speed) {
        super(ModToolMaterials.LEGENDARY, new Item.Settings()
                .rarity(Rarity.EPIC)
                .fireproof()
                .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.LEGENDARY, damage, speed)));
        this.effect = effect;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getWorld() instanceof ServerWorld world) {
            switch (effect) {
                case FLAME -> {
                    target.setOnFireFor(5);
                    world.spawnParticles(ParticleTypes.FLAME, target.getX(), target.getBodyY(0.5), target.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
                }
                case FROST -> {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
                    target.setFrozenTicks(Math.min(300, target.getFrozenTicks() + 80));
                    world.spawnParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getBodyY(0.5), target.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
                }
                case SOUL -> {
                    attacker.heal(2.0f);
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
                    world.spawnParticles(ParticleTypes.SOUL, target.getX(), target.getBodyY(0.5), target.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
                }
                case STORM -> {
                    if (world.random.nextFloat() < 0.2f) {
                        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                        if (bolt != null) {
                            bolt.refreshPositionAfterTeleport(target.getPos());
                            bolt.setCosmetic(true);
                            world.spawnEntity(bolt);
                        }
                        target.damage(attacker.getDamageSources().lightningBolt(), 6f);
                    }
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
    }
}
