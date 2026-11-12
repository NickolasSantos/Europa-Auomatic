package com.europa.rpg.registry;

import com.europa.rpg.EuropaRPG;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public final class ModArmorMaterials {
    public static final RegistryEntry<ArmorMaterial> MITHRIL = register("mithril", 3, 7, 5, 2, 20, 1.0f, 0f, () -> ModItems.MITHRIL_INGOT);
    public static final RegistryEntry<ArmorMaterial> ADAMANTITE = register("adamantite", 3, 8, 6, 3, 12, 3.0f, 0.1f, () -> ModItems.ADAMANTITE_INGOT);

    private ModArmorMaterials() {}

    private static RegistryEntry<ArmorMaterial> register(String name, int helmet, int chest, int legs, int boots,
                                                        int enchantability, float toughness, float kbRes, Supplier<Item> repair) {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, helmet);
        defense.put(ArmorItem.Type.CHESTPLATE, chest);
        defense.put(ArmorItem.Type.LEGGINGS, legs);
        defense.put(ArmorItem.Type.BOOTS, boots);
        defense.put(ArmorItem.Type.BODY, chest);
        return Registry.registerReference(Registries.ARMOR_MATERIAL, EuropaRPG.id(name), new ArmorMaterial(
                defense, enchantability, SoundEvents.ITEM_ARMOR_EQUIP_IRON, () -> Ingredient.ofItems(repair.get()),
                List.of(new ArmorMaterial.Layer(EuropaRPG.id(name))), toughness, kbRes));
    }

    public static void init() {}
}
