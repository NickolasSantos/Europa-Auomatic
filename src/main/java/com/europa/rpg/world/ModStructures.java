package com.europa.rpg.world;

import com.europa.rpg.EuropaRPG;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.world.gen.structure.StructureType;

public final class ModStructures {
    public static final StructureType<CatacombStructure> CATACOMB = Registry.register(Registries.STRUCTURE_TYPE,
            EuropaRPG.id("catacomb"), () -> CatacombStructure.CODEC);
    public static final StructurePieceType CATACOMB_PIECE = Registry.register(Registries.STRUCTURE_PIECE,
            EuropaRPG.id("catacomb_piece"), (StructurePieceType.Simple) CatacombPiece::new);

    private ModStructures() {}

    public static void init() {}
}
