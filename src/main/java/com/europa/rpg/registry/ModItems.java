package com.europa.rpg.registry;

import com.europa.rpg.EuropaRPG;
import com.europa.rpg.item.ArcaneStaffItem;
import com.europa.rpg.item.LegendaryWeaponItem;
import com.europa.rpg.item.RebirthTomeItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Rarity;

public final class ModItems {
    // Materiais
    public static final Item RAW_MITHRIL = register("raw_mithril", new Item(new Item.Settings()));
    public static final Item MITHRIL_INGOT = register("mithril_ingot", new Item(new Item.Settings()));
    public static final Item RAW_ADAMANTITE = register("raw_adamantite", new Item(new Item.Settings()));
    public static final Item ADAMANTITE_INGOT = register("adamantite_ingot", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
    public static final Item SOUL_SHARD = register("soul_shard", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

    // Ferramentas e armas
    public static final Item MITHRIL_SWORD = register("mithril_sword", sword(ModToolMaterials.MITHRIL));
    public static final Item MITHRIL_PICKAXE = register("mithril_pickaxe", pickaxe(ModToolMaterials.MITHRIL));
    public static final Item MITHRIL_AXE = register("mithril_axe", axe(ModToolMaterials.MITHRIL));
    public static final Item MITHRIL_SHOVEL = register("mithril_shovel", shovel(ModToolMaterials.MITHRIL));
    public static final Item ADAMANTITE_SWORD = register("adamantite_sword", sword(ModToolMaterials.ADAMANTITE));
    public static final Item ADAMANTITE_PICKAXE = register("adamantite_pickaxe", pickaxe(ModToolMaterials.ADAMANTITE));
    public static final Item ADAMANTITE_AXE = register("adamantite_axe", axe(ModToolMaterials.ADAMANTITE));
    public static final Item ADAMANTITE_SHOVEL = register("adamantite_shovel", shovel(ModToolMaterials.ADAMANTITE));

    // Armaduras
    public static final Item MITHRIL_HELMET = register("mithril_helmet", armor(ModArmorMaterials.MITHRIL, ArmorItem.Type.HELMET, 25));
    public static final Item MITHRIL_CHESTPLATE = register("mithril_chestplate", armor(ModArmorMaterials.MITHRIL, ArmorItem.Type.CHESTPLATE, 25));
    public static final Item MITHRIL_LEGGINGS = register("mithril_leggings", armor(ModArmorMaterials.MITHRIL, ArmorItem.Type.LEGGINGS, 25));
    public static final Item MITHRIL_BOOTS = register("mithril_boots", armor(ModArmorMaterials.MITHRIL, ArmorItem.Type.BOOTS, 25));
    public static final Item ADAMANTITE_HELMET = register("adamantite_helmet", armor(ModArmorMaterials.ADAMANTITE, ArmorItem.Type.HELMET, 40));
    public static final Item ADAMANTITE_CHESTPLATE = register("adamantite_chestplate", armor(ModArmorMaterials.ADAMANTITE, ArmorItem.Type.CHESTPLATE, 40));
    public static final Item ADAMANTITE_LEGGINGS = register("adamantite_leggings", armor(ModArmorMaterials.ADAMANTITE, ArmorItem.Type.LEGGINGS, 40));
    public static final Item ADAMANTITE_BOOTS = register("adamantite_boots", armor(ModArmorMaterials.ADAMANTITE, ArmorItem.Type.BOOTS, 40));

    // Lendários (apenas em dungeons / missões / chefe)
    public static final Item FLAMEBRAND = register("flamebrand", new LegendaryWeaponItem(LegendaryWeaponItem.Effect.FLAME, 4, -2.4f));
    public static final Item FROSTFANG = register("frostfang", new LegendaryWeaponItem(LegendaryWeaponItem.Effect.FROST, 3, -2.0f));
    public static final Item SOULREAPER = register("soulreaper", new LegendaryWeaponItem(LegendaryWeaponItem.Effect.SOUL, 6, -2.8f));
    public static final Item STORMBREAKER = register("stormbreaker", new LegendaryWeaponItem(LegendaryWeaponItem.Effect.STORM, 7, -3.0f));
    public static final Item ARCANE_STAFF = register("arcane_staff", new ArcaneStaffItem(new Item.Settings().maxCount(1).maxDamage(600).rarity(Rarity.EPIC)));

    // Utilidades
    public static final Item CATACOMB_KEY = register("catacomb_key", new Item(new Item.Settings().maxCount(16).rarity(Rarity.RARE)));
    public static final Item REBIRTH_TOME = register("rebirth_tome", new RebirthTomeItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item CATACOMB_LORD_SPAWN_EGG = register("catacomb_lord_spawn_egg",
            new SpawnEggItem(ModEntities.CATACOMB_LORD, 0x1d1d2b, 0x7b2fbe, new Item.Settings()));

    private ModItems() {}

    private static Item sword(ToolMaterial m) {
        return new SwordItem(m, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(m, 3, -2.4f)));
    }

    private static Item pickaxe(ToolMaterial m) {
        return new PickaxeItem(m, new Item.Settings().attributeModifiers(PickaxeItem.createAttributeModifiers(m, 1f, -2.8f)));
    }

    private static Item axe(ToolMaterial m) {
        return new AxeItem(m, new Item.Settings().attributeModifiers(AxeItem.createAttributeModifiers(m, 5.5f, -3.0f)));
    }

    private static Item shovel(ToolMaterial m) {
        return new ShovelItem(m, new Item.Settings().attributeModifiers(ShovelItem.createAttributeModifiers(m, 1.5f, -3.0f)));
    }

    private static Item armor(RegistryEntry<ArmorMaterial> material, ArmorItem.Type type, int durabilityMultiplier) {
        return new ArmorItem(material, type, new Item.Settings().maxDamage(type.getMaxDamage(durabilityMultiplier)));
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, EuropaRPG.id(name), item);
    }

    public static void init() {}
}
