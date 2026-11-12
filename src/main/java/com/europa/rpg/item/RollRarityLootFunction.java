package com.europa.rpg.item;

import com.europa.rpg.EuropaRPG;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;

/** Função de loot "europa:roll_rarity" (parâmetro min_tier 0..6). Também é aplicada a todos os baús de estrutura. */
public class RollRarityLootFunction extends ConditionalLootFunction {
    public static final MapCodec<RollRarityLootFunction> CODEC = RecordCodecBuilder.mapCodec(i -> addConditionsField(i)
            .and(Codec.intRange(0, 6).optionalFieldOf("min_tier", 0).forGetter(f -> f.minTier))
            .apply(i, RollRarityLootFunction::new));
    public static final LootFunctionType<RollRarityLootFunction> TYPE = Registry.register(Registries.LOOT_FUNCTION_TYPE,
            EuropaRPG.id("roll_rarity"), new LootFunctionType<>(CODEC));

    private final int minTier;

    public RollRarityLootFunction(List<LootCondition> conditions, int minTier) {
        super(conditions);
        this.minTier = minTier;
    }

    @Override
    public LootFunctionType<RollRarityLootFunction> getType() {
        return TYPE;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        return RarityRoller.roll(stack, context.getRandom(), minTier);
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (source.isBuiltin() && key.getValue().getPath().startsWith("chests/")) {
                builder.modifyPools(pool -> pool.apply(ConditionalLootFunction.builder(c -> new RollRarityLootFunction(c, 0))));
            }
        });
    }
}
