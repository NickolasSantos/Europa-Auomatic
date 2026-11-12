package com.europa.rpg.registry;

import com.europa.rpg.EuropaRPG;
import com.europa.rpg.entity.CatacombLordEntity;
import com.europa.rpg.entity.ServantEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntities {
    public static final EntityType<CatacombLordEntity> CATACOMB_LORD = Registry.register(Registries.ENTITY_TYPE,
            EuropaRPG.id("catacomb_lord"),
            EntityType.Builder.create(CatacombLordEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.7f, 2.4f)
                    .makeFireImmune()
                    .maxTrackingRange(10)
                    .build("catacomb_lord"));

    public static final EntityType<ServantEntity> SERVANT = Registry.register(Registries.ENTITY_TYPE,
            EuropaRPG.id("servant"),
            EntityType.Builder.create(ServantEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.95f)
                    .maxTrackingRange(8)
                    .build("servant"));

    private ModEntities() {}

    public static void init() {
        FabricDefaultAttributeRegistry.register(CATACOMB_LORD, CatacombLordEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SERVANT, ServantEntity.createAttributes());
    }
}
