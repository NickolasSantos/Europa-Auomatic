package com.europa.rpg.client;

import com.europa.rpg.EuropaRPG;
import com.europa.rpg.item.GearProgress;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.registry.Registries;

/** Troca o modelo dos equipamentos do mod conforme a fase de evolução (base, Desperto, Ascendido). */
public final class GearVisuals {
    private static final String[] STAGED = {"mithril_sword", "mithril_pickaxe", "mithril_axe", "mithril_shovel",
            "adamantite_sword", "adamantite_pickaxe", "adamantite_axe", "adamantite_shovel",
            "mithril_helmet", "mithril_chestplate", "mithril_leggings", "mithril_boots",
            "adamantite_helmet", "adamantite_chestplate", "adamantite_leggings", "adamantite_boots",
            "flamebrand", "frostfang", "soulreaper", "stormbreaker", "arcane_staff"};

    private GearVisuals() {}

    public static void init() {
        for (String id : STAGED) {
            ModelPredicateProviderRegistry.register(Registries.ITEM.get(EuropaRPG.id(id)), EuropaRPG.id("stage"),
                    (stack, world, entity, seed) -> GearProgress.visualStage(stack) / 2f);
        }
    }
}
