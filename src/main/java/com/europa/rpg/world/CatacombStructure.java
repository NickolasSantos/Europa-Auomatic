package com.europa.rpg.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

/**
 * Catacumba: dungeon subterrânea gerada proceduralmente.
 * <pre>
 *            [sala]
 *              |
 *   [sala] -[CHEFE]- [sala]
 *              |
 *            [sala]
 * </pre>
 * Uma das salas é o Cofre (chave garantida); outra tem o poço com escada até a superfície.
 */
public class CatacombStructure extends Structure {
    public static final MapCodec<CatacombStructure> CODEC = Structure.createCodec(CatacombStructure::new);

    static final int BOSS_HALF = 12;
    static final int BOSS_HEIGHT = 12;
    static final int ROOM_HALF = 6;
    static final int ROOM_HEIGHT = 8;
    static final int CORRIDOR_LEN = 8;
    static final int CORRIDOR_HALF = 2;
    static final int CORRIDOR_HEIGHT = 5;

    public CatacombStructure(Config config) {
        super(config);
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context ctx) {
        ChunkPos cp = ctx.chunkPos();
        int cx = cp.getCenterX();
        int cz = cp.getCenterZ();
        ChunkRandom random = ctx.random();
        int surfaceCenter = ctx.chunkGenerator().getHeightOnGround(cx, cz, Heightmap.Type.WORLD_SURFACE_WG, ctx.world(), ctx.noiseConfig());
        int y = Math.min(-30 + random.nextInt(26), surfaceCenter - 30);
        if (y < ctx.world().getBottomY() + 8) return Optional.empty();

        int vaultIdx = random.nextInt(4);
        int shaftIdx = (vaultIdx + 1 + random.nextInt(3)) % 4;
        int[] mobs = new int[4];
        for (int i = 0; i < 4; i++) mobs[i] = random.nextInt(3);

        Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        int roomDist = BOSS_HALF + CORRIDOR_LEN + ROOM_HALF;
        Direction shaftDir = dirs[shaftIdx];
        int shaftX = cx + shaftDir.getOffsetX() * roomDist;
        int shaftZ = cz + shaftDir.getOffsetZ() * roomDist;
        int shaftTop = ctx.chunkGenerator().getHeightOnGround(shaftX, shaftZ, Heightmap.Type.WORLD_SURFACE_WG, ctx.world(), ctx.noiseConfig());

        BlockPos origin = new BlockPos(cx, y, cz);
        final int fy = y;
        return Optional.of(new StructurePosition(origin, collector -> {
            addPieces(collector, cx, fy, cz, dirs, roomDist, vaultIdx, shaftIdx, mobs, shaftTop);
        }));
    }

    private static void addPieces(StructurePiecesCollector c, int cx, int y, int cz, Direction[] dirs, int roomDist,
                                  int vaultIdx, int shaftIdx, int[] mobs, int shaftTop) {
        c.addPiece(new CatacombPiece(CatacombPiece.Kind.BOSS,
                new BlockBox(cx - BOSS_HALF, y, cz - BOSS_HALF, cx + BOSS_HALF, y + BOSS_HEIGHT - 1, cz + BOSS_HALF), 0));
        for (int i = 0; i < 4; i++) {
            Direction d = dirs[i];
            int rx = cx + d.getOffsetX() * roomDist;
            int rz = cz + d.getOffsetZ() * roomDist;
            c.addPiece(new CatacombPiece(i == vaultIdx ? CatacombPiece.Kind.VAULT : CatacombPiece.Kind.ROOM,
                    new BlockBox(rx - ROOM_HALF, y, rz - ROOM_HALF, rx + ROOM_HALF, y + ROOM_HEIGHT - 1, rz + ROOM_HALF), mobs[i]));
        }
        for (Direction d : dirs) {
            int a = BOSS_HALF, b = BOSS_HALF + CORRIDOR_LEN;
            BlockBox box;
            if (d.getAxis() == Direction.Axis.Z) {
                int z1 = cz + d.getOffsetZ() * a, z2 = cz + d.getOffsetZ() * b;
                box = new BlockBox(cx - CORRIDOR_HALF, y, Math.min(z1, z2), cx + CORRIDOR_HALF, y + CORRIDOR_HEIGHT - 1, Math.max(z1, z2));
            } else {
                int x1 = cx + d.getOffsetX() * a, x2 = cx + d.getOffsetX() * b;
                box = new BlockBox(Math.min(x1, x2), y, cz - CORRIDOR_HALF, Math.max(x1, x2), y + CORRIDOR_HEIGHT - 1, cz + CORRIDOR_HALF);
            }
            c.addPiece(new CatacombPiece(CatacombPiece.Kind.CORRIDOR, box, d.getAxis() == Direction.Axis.X ? 0 : 1));
        }
        Direction sd = dirs[shaftIdx];
        int sx = cx + sd.getOffsetX() * roomDist;
        int sz = cz + sd.getOffsetZ() * roomDist;
        int top = Math.max(shaftTop + 1, y + ROOM_HEIGHT + 2);
        c.addPiece(new CatacombPiece(CatacombPiece.Kind.SHAFT, new BlockBox(sx - 2, y + 1, sz - 2, sx + 2, top, sz + 2), 0));
    }

    @Override
    public StructureType<?> getType() {
        return ModStructures.CATACOMB;
    }
}
