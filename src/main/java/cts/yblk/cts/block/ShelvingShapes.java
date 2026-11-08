package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Hitboxes clipped from the shelving Blockbench model (posts + shelves + solid content volumes).
 * Model space is relative to the master block for facing=north; other facings rotate around (8, 8).
 */
public final class ShelvingShapes {
    private static final Box[] FRAME = {
            box(-15, 0, 14, -14, 31, 15),
            box(30, 0, 14, 31, 31, 15),
            box(-15, 0, 1, -14, 31, 2),
            box(30, 0, 1, 31, 31, 2),
            box(-16, 8, 0, 32, 9, 16),
            box(-16, 19, 0, 32, 20, 16),
            box(-16, 31, 0, 32, 32, 16)
    };

    private static final Box[][] CONTENTS = {
            { // variant 1
                    box(17, 20, 3.3, 22, 24, 10.3),
                    box(-12.7, 20, 8.1, -7.7, 24, 13.1),
                    box(-9, 9.1, 0.4, -1, 18.1, 9.4)
            },
            { // variant 2
                    box(-2, 20, 3.3, 3, 24, 10.3),
                    box(16, 20.1, 0.4, 24, 29.1, 9.4),
                    box(-0.2, 9, 8, 4.8, 11, 13),
                    box(-3.2, 9, 4, 1.8, 11, 9)
            },
            { // variant 3
                    box(9, 20, 3.3, 14, 24, 10.3),
                    box(-10.7, 20, 3.1, -5.7, 24, 8.1),
                    box(11, 9.1, 0.4, 19, 18.1, 9.4),
                    box(18.4, 20.2, 8.7, 27.4, 21.2, 15.7),
                    box(7.4, 20.1, 1, 12.4, 21.1, 7),
                    box(20.4, 9.1, 6, 25.4, 10.1, 12)
            }
    };

    /** [facing][localX+1][localY][0=empty | variant] */
    private static final VoxelShape[][][][] CACHE = new VoxelShape[4][3][2][4];

    private ShelvingShapes() {
    }

    public static VoxelShape shape(Direction facing, int localX, int localY, boolean empty, int variant) {
        int f = facing.get2DDataValue();
        int vx = localX + 1;
        int v = empty ? 0 : Math.max(1, Math.min(3, variant));
        VoxelShape cached = CACHE[f][vx][localY][v];
        if (cached != null) {
            return cached;
        }
        VoxelShape built = build(facing, localX, localY, empty, v);
        CACHE[f][vx][localY][v] = built;
        return built;
    }

    private static VoxelShape build(Direction facing, int localX, int localY, boolean empty, int variant) {
        List<Box> source = new ArrayList<>(List.of(FRAME));
        if (!empty) {
            source.addAll(List.of(CONTENTS[variant - 1]));
        }
        BlockPos cell = ShelvingStructure.offset(facing, localX, localY);
        double originX = cell.getX() * 16.0D;
        double originY = cell.getY() * 16.0D;
        double originZ = cell.getZ() * 16.0D;
        VoxelShape result = Shapes.empty();
        for (Box box : source) {
            Box rotated = rotate(box, facing);
            Box clipped = rotated.clip(originX, originY, originZ, originX + 16.0D, originY + 16.0D, originZ + 16.0D);
            if (clipped == null) {
                continue;
            }
            result = Shapes.joinUnoptimized(result,
                    Block.box(
                            clipped.minX - originX,
                            clipped.minY - originY,
                            clipped.minZ - originZ,
                            clipped.maxX - originX,
                            clipped.maxY - originY,
                            clipped.maxZ - originZ),
                    BooleanOp.OR);
        }
        return result.optimize();
    }

    private static Box rotate(Box box, Direction facing) {
        if (facing == Direction.NORTH) {
            return box;
        }
        double minX = Double.POSITIVE_INFINITY;
        double minY = box.minY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = box.maxY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (double x : new double[]{box.minX, box.maxX}) {
            for (double z : new double[]{box.minZ, box.maxZ}) {
                double nx;
                double nz;
                switch (facing) {
                    case EAST -> {
                        nx = 16.0D - z;
                        nz = x;
                    }
                    case SOUTH -> {
                        nx = 16.0D - x;
                        nz = 16.0D - z;
                    }
                    case WEST -> {
                        nx = z;
                        nz = 16.0D - x;
                    }
                    default -> {
                        nx = x;
                        nz = z;
                    }
                }
                minX = Math.min(minX, nx);
                maxX = Math.max(maxX, nx);
                minZ = Math.min(minZ, nz);
                maxZ = Math.max(maxZ, nz);
            }
        }
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static Box box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private record Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        Box clip(double x0, double y0, double z0, double x1, double y1, double z1) {
            double nx0 = Math.max(minX, x0);
            double ny0 = Math.max(minY, y0);
            double nz0 = Math.max(minZ, z0);
            double nx1 = Math.min(maxX, x1);
            double ny1 = Math.min(maxY, y1);
            double nz1 = Math.min(maxZ, z1);
            if (nx0 >= nx1 || ny0 >= ny1 || nz0 >= nz1) {
                return null;
            }
            return new Box(nx0, ny0, nz0, nx1, ny1, nz1);
        }
    }
}
