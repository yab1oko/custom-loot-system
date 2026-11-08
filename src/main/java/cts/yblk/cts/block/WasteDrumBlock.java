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

public class WasteDrumBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public WasteDrumBlock(Properties properties) {
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
        return createLidStashTicker(level, type, ModBlockEntities.WASTE_DRUM.get());
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape body = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 25.0D, 15.0D);
        VoxelShape north = empty
                ? Shapes.or(
                        body,
                        voxelY(1.0D, 25.0D, 1.0D, 15.0D, 26.0D, 15.0D, 8.0D, 8.0D, 22.5D),
                        voxelY(1.0D, 26.0D, 0.75D, 15.0D, 26.5D, 1.75D, 8.0D, 8.0D, 22.5D),
                        voxelY(1.5D, 26.0D, 3.5D, 14.5D, 26.5D, 4.5D, 8.0D, 8.0D, 22.5D),
                        voxelY(1.5D, 26.0D, 11.5D, 14.5D, 26.5D, 12.5D, 8.0D, 8.0D, 22.5D),
                        voxelY(1.0D, 26.0D, 14.25D, 15.0D, 26.5D, 15.25D, 8.0D, 8.0D, 22.5D),
                        voxelY(0.75D, 26.0D, 1.5D, 1.75D, 26.5D, 14.5D, 8.0D, 8.0D, 22.5D),
                        voxelY(14.25D, 26.0D, 1.5D, 15.25D, 26.5D, 14.5D, 8.0D, 8.0D, 22.5D))
                : Shapes.or(
                        body,
                        Block.box(1.0D, 25.0D, 1.0D, 15.0D, 26.0D, 15.0D),
                        Block.box(1.0D, 26.0D, 1.0D, 15.0D, 26.5D, 1.5D),
                        Block.box(1.5D, 26.0D, 3.5D, 14.5D, 26.5D, 4.5D),
                        Block.box(1.5D, 26.0D, 11.5D, 14.5D, 26.5D, 12.5D),
                        Block.box(1.0D, 26.0D, 14.5D, 15.0D, 26.5D, 15.0D),
                        Block.box(1.0D, 26.0D, 1.5D, 1.5D, 26.5D, 14.5D),
                        Block.box(14.5D, 26.0D, 1.5D, 15.0D, 26.5D, 14.5D));
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
                boolean inside = x < maxWorldX && contains(x + step * 0.5D, z + step * 0.5D, minX, maxX, minZ, maxZ, originX, originZ, cos, sin);
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

    private static boolean contains(double x, double z, double minX, double maxX, double minZ, double maxZ, double originX, double originZ, double cos, double sin) {
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
