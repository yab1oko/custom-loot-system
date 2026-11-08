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

public class ClothesBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public ClothesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(LOOTED) ? EMPTY_SHAPES : SHAPES).get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CLOTHES.get(), CanStashBlockEntity::serverTick);
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape north = empty
                ? Shapes.or(
                        voxelY(3.3D, 0.0D, 0.8D, 9.3D, 1.9D, 6.8D, 3.3D, 0.8D, -22.5D),
                        voxelY(2.9D, 0.0D, 10.2D, 13.9D, 1.5D, 16.0D, 2.9D, 10.2D, 22.5D),
                        voxelY(2.3D, 1.75D, 0.7D, 13.3D, 2.25D, 7.7D, 5.3D, 7.7D, -22.5D))
                : Shapes.or(
                        voxelY(1.6D, 0.0D, 2.7D, 7.6D, 1.9D, 8.7D, 1.6D, 2.7D, 22.5D),
                        voxelZ(7.7D, 2.3D, 2.1D, 13.7D, 4.3D, 8.1D, 7.7D, 2.3D, -22.5D),
                        Block.box(2.2D, 0.0D, 9.2D, 13.2D, 1.5D, 15.2D),
                        voxelY(1.3D, 1.75D, 4.7D, 8.3D, 2.25D, 15.7D, 1.3D, 7.7D, 22.5D));
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
