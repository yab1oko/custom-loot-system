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

public class SafeBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public SafeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean usesPlayerFacing() {
        return true;
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
        return createLidStashTicker(level, type, ModBlockEntities.SAFE.get());
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape body = Shapes.or(
                Block.box(2.0D, 1.0D, 2.0D, 14.0D, 13.0D, 14.0D),
                Block.box(2.1D, 0.0D, 2.1D, 4.1D, 1.0D, 4.1D),
                Block.box(11.9D, 0.0D, 2.1D, 13.9D, 1.0D, 4.1D),
                Block.box(11.9D, 0.0D, 11.9D, 13.9D, 1.0D, 13.9D),
                Block.box(2.1D, 0.0D, 11.9D, 4.1D, 1.0D, 13.9D),
                Block.box(13.0D, 2.5D, 14.0D, 14.0D, 4.5D, 14.5D),
                Block.box(13.0D, 9.5D, 14.0D, 14.0D, 11.5D, 14.5D));
        VoxelShape north = empty
                ? Shapes.or(
                        body,
                        voxelY(3.0D, 2.0D, 14.0D, 13.0D, 12.0D, 15.0D, 13.0D, 14.0D, 22.5D),
                        voxelY(4.0D, 6.0D, 15.0D, 6.0D, 8.0D, 16.0D, 13.0D, 14.0D, 22.5D))
                : Shapes.or(
                        body,
                        Block.box(3.0D, 2.0D, 14.0D, 13.0D, 12.0D, 15.0D),
                        Block.box(4.0D, 6.0D, 15.0D, 6.0D, 8.0D, 16.0D));
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
        double minWorldX = Double.POSITIVE_INFINITY;
        double maxWorldX = Double.NEGATIVE_INFINITY;
        double minWorldZ = Double.POSITIVE_INFINITY;
        double maxWorldZ = Double.NEGATIVE_INFINITY;
        for (double localX : new double[]{minX, maxX}) {
            for (double localZ : new double[]{minZ, maxZ}) {
                double dx = localX - originX;
                double dz = localZ - originZ;
                double worldX = originX + dx * cos + dz * sin;
                double worldZ = originZ - dx * sin + dz * cos;
                minWorldX = Math.min(minWorldX, worldX);
                maxWorldX = Math.max(maxWorldX, worldX);
                minWorldZ = Math.min(minWorldZ, worldZ);
                maxWorldZ = Math.max(maxWorldZ, worldZ);
            }
        }
        double xStart = Math.floor(minWorldX / step) * step;
        double zStart = Math.floor(minWorldZ / step) * step;
        VoxelShape shape = Shapes.empty();
        for (double z = zStart; z < maxWorldZ; z += step) {
            Double runStart = null;
            for (double x = xStart; x <= maxWorldX + step; x += step) {
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
