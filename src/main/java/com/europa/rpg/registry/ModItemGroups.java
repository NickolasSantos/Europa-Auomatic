package com.europa.rpg.registry;

import com.europa.rpg.EuropaRPG;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
    public static final ItemGroup MAIN = Registry.register(Registries.ITEM_GROUP, EuropaRPG.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.SOULREAPER))
                    .displayName(Text.translatable("itemGroup.europa.main"))
                    .entries((ctx, entries) -> {
                        for (Item item : Registries.ITEM) {
                            if (Registries.ITEM.getId(item).getNamespace().equals(EuropaRPG.MOD_ID)) entries.add(item);
                        }
                    })
                    .build());

    private ModItemGroups() {}

    public static void init() {}
}
