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

public class CrateBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public CrateBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(LOOTED) ? EMPTY_SHAPES : SHAPES).get(ShapeFacing.of(state.getValue(FACING)));
    }

    @Override
    protected boolean animatesLid() {
        return true;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createLidStashTicker(level, type, ModBlockEntities.CRATE.get());
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape body = Shapes.or(
                Block.box(2.0D, 1.0D, 2.0D, 14.0D, 13.0D, 14.0D),
                Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 2.0D),
                Block.box(1.0D, 0.0D, 14.0D, 15.0D, 1.0D, 15.0D),
                Block.box(1.0D, 0.0D, 2.0D, 2.0D, 1.0D, 14.0D),
                Block.box(14.0D, 0.0D, 2.0D, 15.0D, 1.0D, 14.0D),
                Block.box(1.0D, 1.0D, 1.0D, 2.0D, 13.0D, 2.0D),
                Block.box(14.0D, 1.0D, 1.0D, 15.0D, 13.0D, 2.0D),
                Block.box(1.0D, 1.0D, 14.0D, 2.0D, 13.0D, 15.0D),
                Block.box(14.0D, 1.0D, 14.0D, 15.0D, 13.0D, 15.0D));
        VoxelShape north = empty
                ? Shapes.or(
                        body,
                        voxelY(2.2D, 13.1D, 3.5D, 14.2D, 13.6D, 15.5D, 8.2D, 9.5D, -22.5D),
                        voxelY(1.2D, 13.0D, 2.5D, 15.2D, 14.0D, 3.5D, 8.2D, 9.5D, -22.5D),
                        voxelY(1.2D, 13.0D, 15.5D, 15.2D, 14.0D, 16.0D, 8.2D, 9.5D, -22.5D),
                        voxelY(1.2D, 13.0D, 3.5D, 2.2D, 14.0D, 15.5D, 8.2D, 9.5D, -22.5D),
                        voxelY(14.2D, 13.0D, 3.5D, 15.2D, 14.0D, 15.5D, 8.2D, 9.5D, -22.5D))
                : Shapes.or(
                        body,
                        Block.box(2.0D, 13.1D, 2.0D, 14.0D, 13.6D, 14.0D),
                        Block.box(1.0D, 13.0D, 1.0D, 15.0D, 14.0D, 2.0D),
                        Block.box(1.0D, 13.0D, 14.0D, 15.0D, 14.0D, 15.0D),
                        Block.box(1.0D, 13.0D, 2.0D, 2.0D, 14.0D, 14.0D),
                        Block.box(14.0D, 13.0D, 2.0D, 15.0D, 14.0D, 14.0D));
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
                    shape = Shapes.joinUnoptimized(shape, Block.box(runStart, minY, z, x, maxY, Math.min(16.0D, z + step)), BooleanOp.OR);
                    runStart = null;
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

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0], Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
        return rotated[0];
    }
}
