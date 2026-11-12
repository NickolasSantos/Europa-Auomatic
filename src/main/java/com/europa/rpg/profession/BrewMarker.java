package com.europa.rpg.profession;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/** Marca temporária em poções preparadas (removida quando o jogador as retira). */
public final class BrewMarker {
    private static final String KEY = "europa_brewed";

    private BrewMarker() {}

    public static void mark(ItemStack stack) {
        if (stack.isEmpty() || !stack.contains(DataComponentTypes.POTION_CONTENTS)) return;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putBoolean(KEY, true));
    }

    /** Retorna true se a poção estava marcada (e remove a marca). */
    public static boolean consume(ItemStack stack) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null || !data.copyNbt().getBoolean(KEY)) return false;
        NbtCompound nbt = data.copyNbt();
        nbt.remove(KEY);
        if (nbt.isEmpty()) stack.remove(DataComponentTypes.CUSTOM_DATA);
        else stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return true;
    }
}
