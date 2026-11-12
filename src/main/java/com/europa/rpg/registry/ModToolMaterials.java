package com.europa.rpg.registry;

import net.minecraft.block.Block;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.ToolMaterial;

import java.util.function.Supplier;

public enum ModToolMaterials implements ToolMaterial {
    /** Entre ferro e diamante: rápido e leve. */
    MITHRIL(BlockTags.INCORRECT_FOR_IRON_TOOL, 900, 8.0f, 2.5f, 20, () -> Ingredient.ofItems(ModItems.MITHRIL_INGOT)),
    /** Acima do diamante, abaixo de netherite em dano, maior durabilidade. */
    ADAMANTITE(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2200, 8.5f, 3.5f, 12, () -> Ingredient.ofItems(ModItems.ADAMANTITE_INGOT)),
    /** Armas lendárias de dungeon. */
    LEGENDARY(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2600, 9.0f, 4.5f, 22, () -> Ingredient.ofItems(ModItems.SOUL_SHARD));

    private final TagKey<Block> inverseTag;
    private final int durability;
    private final float speed;
    private final float damage;
    private final int enchantability;
    private final Supplier<Ingredient> repair;

    ModToolMaterials(TagKey<Block> inverseTag, int durability, float speed, float damage, int enchantability, Supplier<Ingredient> repair) {
        this.inverseTag = inverseTag;
        this.durability = durability;
        this.speed = speed;
        this.damage = damage;
        this.enchantability = enchantability;
        this.repair = repair;
    }

    @Override public int getDurability() { return durability; }
    @Override public float getMiningSpeedMultiplier() { return speed; }
    @Override public float getAttackDamage() { return damage; }
    @Override public TagKey<Block> getInverseTag() { return inverseTag; }
    @Override public int getEnchantability() { return enchantability; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
}
