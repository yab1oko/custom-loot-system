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

public class WeaponCaseBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public WeaponCaseBlock(Properties properties) {
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
        return createLidStashTicker(level, type, ModBlockEntities.WEAPON_CASE.get());
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape north = empty
                ? Shapes.or(
                        Block.box(-3.0D, 0.0D, 2.0D, 19.0D, 5.0D, 14.0D),
                        voxelX(5.0D, 0.39942D, 14.57401D, 11.0D, 2.39942D, 15.57401D, 1.0D, 8.0D, -22.5D),
                        voxelX(-1.0D, 5.75D, 2.0D, 3.0D, 6.75D, 14.0D, 5.0D, 2.0D, -22.5D),
                        voxelX(13.0D, 5.75D, 2.0D, 17.0D, 6.75D, 14.0D, 5.0D, 2.0D, -22.5D),
                        voxelX(-3.0D, 5.0D, 2.0D, 19.0D, 6.0D, 14.0D, 5.0D, 2.0D, -22.5D),
                        voxelX(0.5D, 4.0D, 13.75D, 1.5D, 6.0D, 14.75D, 5.0D, 2.0D, -22.5D),
                        voxelX(14.5D, 4.0D, 13.75D, 15.5D, 6.0D, 14.75D, 5.0D, 2.0D, -22.5D))
                : Shapes.or(
                        Block.box(-3.0D, 0.0D, 2.0D, 19.0D, 5.0D, 14.0D),
                        voxelX(5.0D, 0.3233D, 14.95669D, 11.0D, 2.3233D, 15.95669D, 0.0D, 8.0D, -22.5D),
                        Block.box(-1.0D, 6.0D, 2.0D, 3.0D, 6.5D, 14.0D),
                        Block.box(13.0D, 6.0D, 2.0D, 17.0D, 6.5D, 14.0D),
                        Block.box(-3.0D, 5.0D, 2.0D, 19.0D, 6.0D, 14.0D),
                        Block.box(0.5D, 4.0D, 14.0D, 1.5D, 6.0D, 14.5D),
                        Block.box(14.5D, 4.0D, 14.0D, 15.5D, 6.0D, 14.5D));
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
                double worldY = originY + dy * cos - dz * sin;
                double worldZ = originZ + dy * sin + dz * cos;
                minWorldY = Math.min(minWorldY, worldY);
                maxWorldY = Math.max(maxWorldY, worldY);
                minWorldZ = Math.min(minWorldZ, worldZ);
                maxWorldZ = Math.max(maxWorldZ, worldZ);
            }
        }
        double yStart = Math.floor(minWorldY / step) * step;
        double zStart = Math.floor(minWorldZ / step) * step;
        VoxelShape shape = Shapes.empty();
        for (double x = minX; x < maxX; x += step) {
            for (double y = yStart; y < maxWorldY; y += step) {
                Double runStart = null;
                for (double z = zStart; z <= maxWorldZ + step; z += step) {
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
