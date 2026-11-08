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

public class BoxesBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public BoxesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(LOOTED) ? EMPTY_SHAPES : SHAPES).get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.BOXES.get(), CanStashBlockEntity::serverTick);
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape north = empty
                ? Shapes.or(
                        Block.box(1.0D, 0.1D, 2.4D, 9.0D, 9.1D, 8.4D),
                        voxelY(1.0D, 0.1D, 2.15D, 4.0D, 9.1D, 2.65D, 1.0D, 2.4D, 22.5D),
                        voxelY(9.0D, 0.1D, 2.15D, 12.0D, 9.1D, 2.65D, 9.0D, 2.4D, 22.5D),
                        voxelX(6.0D, 0.0D, 11.1D, 11.0D, 7.0D, 15.1D, 0.0D, 11.1D, -22.5D))
                : Shapes.or(
                        Block.box(1.0D, 0.1D, 0.4D, 9.0D, 6.1D, 9.4D),
                        voxelZ(0.75D, 6.1D, 0.4D, 1.25D, 9.1D, 9.4D, 1.0D, 6.1D, 22.5D),
                        voxelZ(8.75D, 6.1D, 0.4D, 9.25D, 9.1D, 9.4D, 9.0D, 6.1D, -22.5D),
                        Block.box(10.0D, 0.0D, 3.3D, 15.0D, 4.0D, 10.3D),
                        voxelY(6.3D, 0.0D, 10.1D, 11.3D, 4.0D, 15.1D, 8.3D, 12.1D, -22.5D));
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.EAST, rotateClockwise(north));
        shapes.put(Direction.SOUTH, rotateClockwise(shapes.get(Direction.EAST)));
        shapes.put(Direction.WEST, rotateClockwise(shapes.get(Direction.SOUTH)));
        return shapes;
    }

    private static VoxelShape voxelY(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double originX, double originZ, double degrees) {
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        VoxelShape shape = Shapes.empty();
        for (double z = 0.0D; z < 16.0D; z += step) {
            Double runStart = null;
            for (double x = 0.0D; x <= 16.0D; x += step) {
                boolean inside = x < 16.0D && containsY(x + step * 0.5D, z + step * 0.5D, minX, maxX, minZ, maxZ, originX, originZ, cos, sin);
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
        VoxelShape shape = Shapes.empty();
        for (double z = Math.max(0.0D, minZ); z < Math.min(16.0D, maxZ); z += step) {
            for (double x = 0.0D; x < 16.0D; x += step) {
                Double runStart = null;
                for (double y = 0.0D; y <= 16.0D; y += step) {
                    boolean inside = y < 16.0D && containsZ(x + step * 0.5D, y + step * 0.5D, minX, maxX, minY, maxY, originX, originY, cos, sin);
                    if (inside && runStart == null) {
                        runStart = y;
                    } else if (!inside && runStart != null) {
                        shape = Shapes.joinUnoptimized(shape, Block.box(x, runStart, z, x + step, y, Math.min(16.0D, z + step)), BooleanOp.OR);
                        runStart = null;
                    }
                }
            }
        }
        return shape.optimize();
    }

    private static VoxelShape voxelX(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double originY, double originZ, double degrees) {
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        VoxelShape shape = Shapes.empty();
        for (double x = Math.max(0.0D, minX); x < Math.min(16.0D, maxX); x += step) {
            for (double y = 0.0D; y < 16.0D; y += step) {
                Double runStart = null;
                for (double z = 0.0D; z <= 16.0D; z += step) {
                    boolean inside = z < 16.0D && containsX(y + step * 0.5D, z + step * 0.5D, minY, maxY, minZ, maxZ, originY, originZ, cos, sin);
                    if (inside && runStart == null) {
                        runStart = z;
                    } else if (!inside && runStart != null) {
                        shape = Shapes.joinUnoptimized(shape, Block.box(x, y, runStart, Math.min(16.0D, x + step), Math.min(16.0D, y + step), z), BooleanOp.OR);
                        runStart = null;
                    }
                }
            }
        }
        return shape.optimize();
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

    private static boolean containsX(double y, double z, double minY, double maxY, double minZ, double maxZ, double originY, double originZ, double cos, double sin) {
        double dy = y - originY;
        double dz = z - originZ;
        double localY = originY + dy * cos + dz * sin;
        double localZ = originZ - dy * sin + dz * cos;
        return localY >= minY && localY <= maxY && localZ >= minZ && localZ <= maxZ;
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0], Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
        return rotated[0];
    }
}
