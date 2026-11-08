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

public class AmmoBoxBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public AmmoBoxBlock(Properties properties) {
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
        return createLidStashTicker(level, type, ModBlockEntities.AMMO_BOX.get());
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        // Closed body stays put. Open lid matches the end pose of animation "open":
        // +70° X around (8, 7, 13), then slide Y -6, Z +2.9 (pixels).
        VoxelShape body = Shapes.or(
                Block.box(2.0D, 0.0D, 3.0D, 4.0D, 1.0D, 13.0D),
                Block.box(12.0D, 0.0D, 3.0D, 14.0D, 1.0D, 13.0D),
                Block.box(1.0D, 1.0D, 3.0D, 15.0D, 7.0D, 13.0D),
                Block.box(0.5D, 1.0D, 4.0D, 1.0D, 7.0D, 5.5D),
                Block.box(0.5D, 1.0D, 10.5D, 1.0D, 7.0D, 12.0D),
                Block.box(15.0D, 1.0D, 4.0D, 15.5D, 7.0D, 5.5D),
                Block.box(15.0D, 1.0D, 10.5D, 15.5D, 7.0D, 12.0D));
        VoxelShape north = empty
                ? Shapes.or(
                        body,
                        voxelX(1.0D, 6.0D, 3.0D, 15.0D, 7.0D, 13.0D, 7.0D, 13.0D, 70.0D, -6.0D, 2.9D),
                        voxelX(3.0D, 7.0D, 3.0D, 5.0D, 7.5D, 13.0D, 7.0D, 13.0D, 70.0D, -6.0D, 2.9D),
                        voxelX(11.0D, 7.0D, 3.0D, 13.0D, 7.5D, 13.0D, 7.0D, 13.0D, 70.0D, -6.0D, 2.9D))
                : Shapes.or(
                        body,
                        Block.box(3.0D, 7.0D, 3.0D, 5.0D, 7.5D, 13.0D),
                        Block.box(11.0D, 7.0D, 3.0D, 13.0D, 7.5D, 13.0D),
                        Block.box(1.0D, 6.0D, 3.0D, 15.0D, 7.0D, 13.0D));
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.EAST, rotateClockwise(north));
        shapes.put(Direction.SOUTH, rotateClockwise(shapes.get(Direction.EAST)));
        shapes.put(Direction.WEST, rotateClockwise(shapes.get(Direction.SOUTH)));
        return shapes;
    }

    private static VoxelShape voxelX(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                     double originY, double originZ, double degrees, double posY, double posZ) {
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        VoxelShape shape = Shapes.empty();
        for (double x = Math.max(0.0D, minX); x < Math.min(16.0D, maxX); x += step) {
            for (double y = 0.0D; y < 16.0D; y += step) {
                Double runStart = null;
                for (double z = 0.0D; z <= 16.0D; z += step) {
                    boolean inside = z < 16.0D && containsX(y + step * 0.5D, z + step * 0.5D, minY, maxY, minZ, maxZ, originY, originZ, posY, posZ, cos, sin);
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

    private static boolean containsX(double y, double z, double minY, double maxY, double minZ, double maxZ,
                                     double originY, double originZ, double posY, double posZ, double cos, double sin) {
        double dy = y - originY - posY;
        double dz = z - originZ - posZ;
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
