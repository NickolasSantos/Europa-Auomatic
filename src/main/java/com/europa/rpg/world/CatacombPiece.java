package com.europa.rpg.world;

import com.europa.rpg.EuropaRPG;
import com.europa.rpg.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

/**
 * Peça única parametrizada por {@link Kind}. Usa coordenadas absolutas e decoração
 * determinística por hash de posição, para ficar consistente entre fronteiras de chunk.
 */
public class CatacombPiece extends StructurePiece {
    public enum Kind { BOSS, ROOM, VAULT, CORRIDOR, SHAFT }

    public static final RegistryKey<LootTable> ROOM_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, EuropaRPG.id("chests/catacomb_room"));
    public static final RegistryKey<LootTable> VAULT_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, EuropaRPG.id("chests/catacomb_vault"));

    private final Kind kind;
    private final int variant;

    public CatacombPiece(Kind kind, BlockBox box, int variant) {
        super(ModStructures.CATACOMB_PIECE, 0, box);
        this.kind = kind;
        this.variant = variant;
    }

    public CatacombPiece(NbtCompound nbt) {
        super(ModStructures.CATACOMB_PIECE, nbt);
        this.kind = Kind.values()[MathHelper.clamp(nbt.getInt("Kind"), 0, Kind.values().length - 1)];
        this.variant = nbt.getInt("Variant");
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("Kind", kind.ordinal());
        nbt.putInt("Variant", variant);
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator,
                         Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (kind) {
            case BOSS -> generateBoss(world, chunkBox);
            case ROOM, VAULT -> generateRoom(world, chunkBox, random);
            case CORRIDOR -> generateCorridor(world, chunkBox);
            case SHAFT -> generateShaft(world, chunkBox);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void set(StructureWorldAccess world, BlockBox chunkBox, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (chunkBox.contains(pos)) world.setBlockState(pos, state, 2);
    }

    private static int hash(int x, int y, int z) {
        return (int) ((MathHelper.hashCode(x, y, z) >>> 16) & 0xFF);
    }

    private static BlockState stoneWall(int x, int y, int z) {
        int h = hash(x, y, z) % 20;
        if (h < 5) return Blocks.MOSSY_STONE_BRICKS.getDefaultState();
        if (h < 8) return Blocks.CRACKED_STONE_BRICKS.getDefaultState();
        return Blocks.STONE_BRICKS.getDefaultState();
    }

    private static BlockState stoneFloor(int x, int y, int z) {
        int h = hash(x, y, z) % 10;
        if (h < 3) return Blocks.MOSSY_COBBLESTONE.getDefaultState();
        if (h < 5) return Blocks.GRAVEL.getDefaultState();
        return Blocks.COBBLESTONE.getDefaultState();
    }

    private static BlockState deepWall(int x, int y, int z) {
        int h = hash(x, y, z) % 20;
        if (h < 4) return Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState();
        if (h < 7) return Blocks.DEEPSLATE_TILES.getDefaultState();
        return Blocks.DEEPSLATE_BRICKS.getDefaultState();
    }

    private static BlockState hangingLantern() {
        return Blocks.SOUL_LANTERN.getDefaultState().with(LanternBlock.HANGING, true);
    }

    // ------------------------------------------------------------------ boss

    private void generateBoss(StructureWorldAccess world, BlockBox cb) {
        BlockBox b = boundingBox;
        for (int x = b.getMinX(); x <= b.getMaxX(); x++) {
            for (int z = b.getMinZ(); z <= b.getMaxZ(); z++) {
                for (int y = b.getMinY(); y <= b.getMaxY(); y++) {
                    boolean edge = x == b.getMinX() || x == b.getMaxX() || z == b.getMinZ() || z == b.getMaxZ();
                    BlockState s;
                    if (y == b.getMinY()) {
                        s = hash(x, y, z) % 12 == 0 ? Blocks.GILDED_BLACKSTONE.getDefaultState() : Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState();
                    } else if (y == b.getMaxY()) {
                        s = Blocks.DEEPSLATE_TILES.getDefaultState();
                    } else if (edge) {
                        s = deepWall(x, y, z);
                    } else {
                        s = Blocks.AIR.getDefaultState();
                    }
                    set(world, cb, x, y, z, s);
                }
            }
        }
        int cx = b.getCenter().getX(), cz = b.getCenter().getZ(), fy = b.getMinY();
        // Pilares nos quatro quadrantes
        int[] off = {-7, 6};
        for (int ox : off) for (int oz : off) {
            for (int y = fy + 1; y < b.getMaxY(); y++) {
                for (int dx = 0; dx < 2; dx++) for (int dz = 0; dz < 2; dz++) {
                    set(world, cb, cx + ox + dx, y, cz + oz + dz, Blocks.POLISHED_BLACKSTONE.getDefaultState());
                }
            }
            set(world, cb, cx + ox, b.getMaxY() - 1, cz + oz - 1, hangingLantern());
        }
        // Plataforma e altar central
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            set(world, cb, cx + dx, fy + 1, cz + dz, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState());
        }
        for (int dx = -2; dx <= 2; dx += 4) for (int dz = -2; dz <= 2; dz += 4) {
            set(world, cb, cx + dx, fy + 2, cz + dz, Blocks.SOUL_LANTERN.getDefaultState());
        }
        set(world, cb, cx, fy + 2, cz, ModBlocks.BOSS_ALTAR.getDefaultState());
        // Lanternas extras no teto
        for (int dx = -8; dx <= 8; dx += 8) for (int dz = -8; dz <= 8; dz += 8) {
            if (dx == 0 && dz == 0) continue;
            set(world, cb, cx + dx, b.getMaxY() - 1, cz + dz, hangingLantern());
        }
    }

    // ------------------------------------------------------------------ salas

    private void generateRoom(StructureWorldAccess world, BlockBox cb, Random random) {
        BlockBox b = boundingBox;
        for (int x = b.getMinX(); x <= b.getMaxX(); x++) {
            for (int z = b.getMinZ(); z <= b.getMaxZ(); z++) {
                for (int y = b.getMinY(); y <= b.getMaxY(); y++) {
                    boolean edge = x == b.getMinX() || x == b.getMaxX() || z == b.getMinZ() || z == b.getMaxZ() || y == b.getMaxY();
                    BlockState s = y == b.getMinY() ? stoneFloor(x, y, z) : edge ? stoneWall(x, y, z) : Blocks.AIR.getDefaultState();
                    set(world, cb, x, y, z, s);
                }
            }
        }
        int cx = b.getCenter().getX(), cz = b.getCenter().getZ(), fy = b.getMinY();
        // Teias nos cantos superiores
        int top = b.getMaxY() - 1;
        for (int x : new int[]{b.getMinX() + 1, b.getMaxX() - 1}) for (int z : new int[]{b.getMinZ() + 1, b.getMaxZ() - 1}) {
            set(world, cb, x, top, z, Blocks.COBWEB.getDefaultState());
            if (hash(x, top, z) % 2 == 0) set(world, cb, x, top - 1, z, Blocks.COBWEB.getDefaultState());
        }
        // Spawner central (deslocado se for a sala do poço, que usa o centro)
        EntityType<?> mob = kind == Kind.VAULT ? EntityType.CAVE_SPIDER : switch (variant) {
            case 0 -> EntityType.ZOMBIE;
            case 1 -> EntityType.SKELETON;
            default -> EntityType.SPIDER;
        };
        BlockPos spawnerPos = new BlockPos(cx + 2, fy + 1, cz + 2);
        if (cb.contains(spawnerPos)) {
            world.setBlockState(spawnerPos, Blocks.SPAWNER.getDefaultState(), 2);
            BlockEntity be = world.getBlockEntity(spawnerPos);
            if (be instanceof MobSpawnerBlockEntity spawner) spawner.setEntityType(mob, random);
        }
        // Baús
        RegistryKey<LootTable> loot = kind == Kind.VAULT ? VAULT_LOOT : ROOM_LOOT;
        chest(world, cb, random, b.getMinX() + 1, fy + 1, b.getMinZ() + 1, Direction.SOUTH, loot);
        chest(world, cb, random, b.getMaxX() - 1, fy + 1, b.getMaxZ() - 1, Direction.NORTH, loot);
        if (kind == Kind.VAULT) {
            chest(world, cb, random, b.getMinX() + 1, fy + 1, b.getMaxZ() - 1, Direction.NORTH, ROOM_LOOT);
            set(world, cb, cx, b.getMaxY() - 1, cz, hangingLantern());
        }
    }

    private static void chest(StructureWorldAccess world, BlockBox cb, Random random, int x, int y, int z, Direction facing,
                              RegistryKey<LootTable> loot) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!cb.contains(pos)) return;
        world.setBlockState(pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing), 2);
        LootableInventory.setLootTable(world, random, pos, loot);
    }

    // ------------------------------------------------------------------ corredores

    private void generateCorridor(StructureWorldAccess world, BlockBox cb) {
        BlockBox b = boundingBox;
        boolean alongX = variant == 0;
        for (int x = b.getMinX(); x <= b.getMaxX(); x++) {
            for (int z = b.getMinZ(); z <= b.getMaxZ(); z++) {
                int cross = alongX ? z : x;
                int crossMin = alongX ? b.getMinZ() : b.getMinX();
                int crossMax = alongX ? b.getMaxZ() : b.getMaxX();
                int along = alongX ? x - b.getMinX() : z - b.getMinZ();
                for (int y = b.getMinY(); y <= b.getMaxY(); y++) {
                    BlockState s;
                    if (y == b.getMinY()) s = stoneFloor(x, y, z);
                    else if (y == b.getMaxY() || cross == crossMin || cross == crossMax) s = stoneWall(x, y, z);
                    else s = Blocks.AIR.getDefaultState();
                    set(world, cb, x, y, z, s);
                }
                int center = (crossMin + crossMax) / 2;
                if (cross == center && along % 4 == 2) set(world, cb, x, b.getMaxY() - 1, z, hangingLantern());
            }
        }
    }

    // ------------------------------------------------------------------ poço de acesso

    private void generateShaft(StructureWorldAccess world, BlockBox cb) {
        BlockBox b = boundingBox;
        int roomTop = b.getMinY() + CatacombStructure.ROOM_HEIGHT - 2; // teto da sala (y absoluto)
        int cx = b.getCenter().getX(), cz = b.getCenter().getZ();
        for (int y = b.getMinY(); y <= b.getMaxY(); y++) {
            boolean insideRoom = y < roomTop;
            for (int x = b.getMinX(); x <= b.getMaxX(); x++) {
                for (int z = b.getMinZ(); z <= b.getMaxZ(); z++) {
                    boolean edge = x == b.getMinX() || x == b.getMaxX() || z == b.getMinZ() || z == b.getMaxZ();
                    if (insideRoom) continue; // dentro da sala só o pilar da escada
                    if (edge) {
                        // Abertura na superfície no lado sul para sair do poço
                        boolean exit = y >= b.getMaxY() - 1 && z == b.getMaxZ() && x == cx;
                        set(world, cb, x, y, z, exit ? Blocks.AIR.getDefaultState() : stoneWall(x, y, z));
                    } else {
                        set(world, cb, x, y, z, Blocks.AIR.getDefaultState());
                    }
                }
            }
            // Pilar de suporte da escada dentro da sala + escada contínua
            if (insideRoom) set(world, cb, cx, y, cz - 2, stoneWall(cx, y, cz - 2));
            set(world, cb, cx, y, cz - 1, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH));
        }
        // Marcadores na superfície
        set(world, cb, b.getMinX(), b.getMaxY() + 1, b.getMinZ(), Blocks.SOUL_LANTERN.getDefaultState());
        set(world, cb, b.getMaxX(), b.getMaxY() + 1, b.getMaxZ(), Blocks.SOUL_LANTERN.getDefaultState());
    }
}
