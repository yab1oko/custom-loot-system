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

public class DirtyBackpackBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public DirtyBackpackBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(LOOTED) ? EMPTY_SHAPES : SHAPES).get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.DIRTY_BACKPACK.get(), CanStashBlockEntity::serverTick);
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape north = empty
                ? Shapes.or(
                        Block.box(1.4D, 0.0D, 1.5D, 11.4D, 6.0D, 12.5D),
                        Block.box(1.4D, 6.0D, 1.5D, 11.4D, 7.0D, 6.5D),
                        Block.box(2.9D, 6.0D, 6.5D, 4.4D, 6.5D, 8.0D),
                        Block.box(8.4D, 6.0D, 6.5D, 9.9D, 6.5D, 8.0D),
                        voxelZ(10.7D, 0.0D, 0.7D, 13.7D, 3.0D, 10.7D, 10.7D, 5.0D, 22.5D))
                : Shapes.or(
                        Block.box(3.0D, 0.0D, 6.0D, 13.0D, 11.0D, 12.0D),
                        Block.box(3.0D, 11.0D, 8.4D, 13.0D, 14.0D, 11.4D),
                        Block.box(3.0D, 0.0D, 5.0D, 13.0D, 5.0D, 6.0D),
                        Block.box(4.5D, 5.0D, 5.5D, 6.0D, 6.5D, 6.0D),
                        Block.box(10.0D, 5.0D, 5.5D, 11.5D, 6.5D, 6.0D),
                        Block.box(6.0D, 11.0D, 11.6D, 10.0D, 13.5D, 12.4D),
                        Block.box(4.0D, 3.16D, 12.4D, 6.0D, 8.16D, 13.15D),
                        Block.box(10.0D, 2.16D, 12.4D, 12.0D, 8.16D, 13.15D),
                        voxelX(4.0D, 8.0D, 11.6D, 6.0D, 10.0D, 12.4D, 10.0D, 12.0D, -22.5D),
                        voxelX(10.0D, 8.0D, 11.6D, 12.0D, 10.0D, 12.4D, 10.0D, 12.0D, -22.5D));
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

    private static boolean containsX(double y, double z, double minY, double maxY, double minZ, double maxZ, double originY, double originZ, double cos, double sin) {
        double dy = y - originY;
        double dz = z - originZ;
        double localY = originY + dy * cos + dz * sin;
        double localZ = originZ - dy * sin + dz * cos;
        return localY >= minY && localY <= maxY && localZ >= minZ && localZ <= maxZ;
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
