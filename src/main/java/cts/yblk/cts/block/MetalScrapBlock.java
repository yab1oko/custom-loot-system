package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;

public class MetalScrapBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public MetalScrapBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(LOOTED) ? EMPTY_SHAPES : SHAPES).get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.METAL_SCRAP.get(), CanStashBlockEntity::serverTick);
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape north = empty
                ? Shapes.or(
                        voxelY(2.0D, 0.0D, 6.0D, 13.0D, 2.0D, 8.0D, 2.0D, 8.0D, 22.5D),
                        voxelY(9.5D, 2.0D, 3.5D, 15.5D, 2.5D, 9.5D, 9.5D, 3.5D, -22.5D),
                        voxelX(1.5D, 2.0D, 7.0D, 7.5D, 3.0D, 13.0D, 2.0D, 7.0D, 22.5D),
                        voxelY(12.0D, 0.0D, 4.0D, 14.0D, 2.0D, 15.0D, 12.0D, 4.0D, -22.5D))
                : Shapes.or(
                        voxelY(2.0D, 0.0D, 6.0D, 4.0D, 2.0D, 17.0D, 2.0D, 6.0D, 45.0D),
                        voxelX(3.0D, 0.0D, 1.0D, 9.0D, 1.0D, 7.0D, 0.0D, 1.0D, -22.5D),
                        voxelY(9.1D, 2.0D, 6.5D, 15.1D, 2.5D, 12.5D, 9.1D, 6.5D, -22.5D),
                        voxelZ(6.0D, 4.55D, 6.5D, 12.0D, 5.55D, 12.5D, 6.0D, 4.8D, -22.5D),
                        voxelX(1.5D, 2.0D, 9.0D, 7.5D, 3.0D, 15.0D, 2.0D, 9.0D, 22.5D),
                        Block.box(12.0D, 0.0D, 1.0D, 14.0D, 2.0D, 12.0D),
                        voxelY(6.0D, 3.0D, 3.0D, 8.0D, 5.0D, 14.0D, 6.0D, 3.0D, -22.5D));
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.EAST, rotateClockwise(north));
        shapes.put(Direction.SOUTH, rotateClockwise(shapes.get(Direction.EAST)));
        shapes.put(Direction.WEST, rotateClockwise(shapes.get(Direction.SOUTH)));
        return shapes;
    }

    private static VoxelShape voxelX(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double originY, double originZ, double degrees) {
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        double minWorldY = Double.POSITIVE_INFINITY;
        double maxWorldY = Double.NEGATIVE_INFINITY;
        double minWorldZ = Double.POSITIVE_INFINITY;
        double maxWorldZ = Double.NEGATIVE_INFINITY;
        for (double localY : new double[]{minY, maxY}) {
            for (double localZ : new double[]{minZ, maxZ}) {
                double dy = localY - originY;
                double dz = localZ - originZ;
                minWorldY = Math.min(minWorldY, originY + dy * cos - dz * sin);
                maxWorldY = Math.max(maxWorldY, originY + dy * cos - dz * sin);
                minWorldZ = Math.min(minWorldZ, originZ + dy * sin + dz * cos);
                maxWorldZ = Math.max(maxWorldZ, originZ + dy * sin + dz * cos);
            }
        }
        VoxelShape shape = Shapes.empty();
        for (double x = minX; x < maxX; x += step) {
            for (double y = Math.floor(minWorldY / step) * step; y < maxWorldY; y += step) {
                Double runStart = null;
                for (double z = Math.floor(minWorldZ / step) * step; z <= maxWorldZ + step; z += step) {
                    boolean inside = z < maxWorldZ && containsX(y + step * 0.5D, z + step * 0.5D, minY, maxY, minZ, maxZ, originY, originZ, cos, sin);
                    if (inside && runStart == null) {
                        runStart = z;
                    } else if (!inside && runStart != null) {
                        shape = Shapes.joinUnoptimized(shape, Block.box(x, y, runStart, Math.min(x + step, maxX), y + step, z), BooleanOp.OR);
                        runStart = null;
                    }
                }
            }
        }
        return shape.optimize();
    }

    private static VoxelShape voxelY(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double originX, double originZ, double degrees) {
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        double minWorldX = Double.POSITIVE_INFINITY;
        double maxWorldX = Double.NEGATIVE_INFINITY;
        double minWorldZ = Double.POSITIVE_INFINITY;
        double maxWorldZ = Double.NEGATIVE_INFINITY;
        for (double localX : new double[]{minX, maxX}) {
            for (double localZ : new double[]{minZ, maxZ}) {
                double dx = localX - originX;
                double dz = localZ - originZ;
                minWorldX = Math.min(minWorldX, originX + dx * cos + dz * sin);
                maxWorldX = Math.max(maxWorldX, originX + dx * cos + dz * sin);
                minWorldZ = Math.min(minWorldZ, originZ - dx * sin + dz * cos);
                maxWorldZ = Math.max(maxWorldZ, originZ - dx * sin + dz * cos);
            }
        }
        VoxelShape shape = Shapes.empty();
        for (double z = Math.floor(minWorldZ / step) * step; z < maxWorldZ; z += step) {
            Double runStart = null;
            for (double x = Math.floor(minWorldX / step) * step; x <= maxWorldX + step; x += step) {
                boolean inside = x < maxWorldX && containsY(x + step * 0.5D, z + step * 0.5D, minX, maxX, minZ, maxZ, originX, originZ, cos, sin);
                if (inside && runStart == null) {
                    runStart = x;
                } else if (!inside && runStart != null) {
                    shape = Shapes.joinUnoptimized(shape, Block.box(runStart, minY, z, x, maxY, z + step), BooleanOp.OR);
                    runStart = null;
                }
            }
        }
        return shape.optimize();
    }

    private static VoxelShape voxelZ(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double originX, double originY, double degrees) {
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        double minWorldX = Double.POSITIVE_INFINITY;
        double maxWorldX = Double.NEGATIVE_INFINITY;
        double minWorldY = Double.POSITIVE_INFINITY;
        double maxWorldY = Double.NEGATIVE_INFINITY;
        for (double localX : new double[]{minX, maxX}) {
            for (double localY : new double[]{minY, maxY}) {
                double dx = localX - originX;
                double dy = localY - originY;
                minWorldX = Math.min(minWorldX, originX + dx * cos - dy * sin);
                maxWorldX = Math.max(maxWorldX, originX + dx * cos - dy * sin);
                minWorldY = Math.min(minWorldY, originY + dx * sin + dy * cos);
                maxWorldY = Math.max(maxWorldY, originY + dx * sin + dy * cos);
            }
        }
        VoxelShape shape = Shapes.empty();
        for (double z = minZ; z < maxZ; z += step) {
            for (double y = Math.floor(minWorldY / step) * step; y < maxWorldY; y += step) {
                Double runStart = null;
                for (double x = Math.floor(minWorldX / step) * step; x <= maxWorldX + step; x += step) {
                    boolean inside = x < maxWorldX && containsZ(x + step * 0.5D, y + step * 0.5D, minX, maxX, minY, maxY, originX, originY, cos, sin);
                    if (inside && runStart == null) {
                        runStart = x;
                    } else if (!inside && runStart != null) {
                        shape = Shapes.joinUnoptimized(shape, Block.box(runStart, y, z, x, y + step, Math.min(z + step, maxZ)), BooleanOp.OR);
                        runStart = null;
                    }
                }
            }
        }
        return shape.optimize();
    }

    private static boolean containsX(double y, double z, double minY, double maxY, double minZ, double maxZ, double originY, double originZ, double cos, double sin) {
        double dy = y - originY;
        double dz = z - originZ;
        double localY = originY + dy * cos + dz * sin;
        double localZ = originZ - dy * sin + dz * cos;
        return localY >= minY && localY <= maxY && localZ >= minZ && localZ <= maxZ;
    }

    private static boolean containsY(double x, double z, double minX, double maxX, double minZ, double maxZ, double originX, double originZ, double cos, double sin) {
        double dx = x - originX;
        double dz = z - originZ;
        double localX = originX + dx * cos - dz * sin;
        double localZ = originZ + dx * sin + dz * cos;
        return localX >= minX && localX <= maxX && localZ >= minZ && localZ <= maxZ;
    }

    private static boolean containsZ(double x, double y, double minX, double maxX, double minY, double maxY, double originX, double originY, double cos, double sin) {
        double dx = x - originX;
        double dy = y - originY;
        double localX = originX + dx * cos + dy * sin;
        double localY = originY - dx * sin + dy * cos;
        return localX >= minX && localX <= maxX && localY >= minY && localY <= maxY;
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0], Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
        return rotated[0];
    }
}
