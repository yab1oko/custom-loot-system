package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;

public class CorpseBlock extends CanStashBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 1, 2);
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    public CorpseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LOOTED, false)
                .setValue(VARIANT, 1));
    }

    @Override
    public boolean usesPlayerFacing() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOOTED, VARIANT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(LOOTED, false)
                .setValue(VARIANT, randomVariant(context.getLevel().getRandom()));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        int variant = state.hasProperty(VARIANT) ? state.getValue(VARIANT) : randomVariant(level.getRandom());
        level.setBlock(pos, state
                        .setValue(FACING, state.getValue(FACING))
                        .setValue(LOOTED, false)
                        .setValue(VARIANT, variant),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    public static int randomVariant(RandomSource random) {
        return 1 + random.nextInt(2);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CORPSE.get(), CanStashBlockEntity::serverTick);
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        VoxelShape north = Shapes.or(
                // head (+ layer), approx AABB for -22.5° Z tilt
                Block.box(2.5D, 0.0D, -10.4D, 12.0D, 7.1D, -1.4D),
                Block.box(4.0D, 0.0D, -2.0D, 12.0D, 4.1D, 10.0D),
                Block.box(3.5D, 0.0D, -2.5D, 12.5D, 4.6D, 10.5D),
                voxelY(0.0D, 0.0D, -2.0D, 4.0D, 3.8D, 10.0D, 2.0D, -2.0D, -22.5D),
                voxelY(-0.5D, 0.0D, -2.5D, 4.5D, 4.3D, 10.5D, 2.0D, -2.0D, -22.5D),
                voxelY(14.0D, 0.0D, -1.7D, 18.0D, 4.0D, 10.3D, 16.0D, 4.0D, 22.5D),
                voxelY(13.5D, 0.0D, -2.2D, 18.5D, 4.5D, 10.8D, 16.0D, 4.0D, 22.5D),
                voxelY(4.8D, 0.0D, 9.9D, 8.8D, 3.9D, 21.9D, 7.0D, 8.0D, -22.5D),
                voxelY(4.3D, 0.0D, 9.4D, 9.3D, 4.4D, 22.4D, 7.0D, 8.0D, -22.5D),
                voxelY(7.5D, 0.0D, 9.4D, 11.5D, 4.0D, 21.4D, 11.0D, 6.6D, 22.5D),
                voxelY(7.0D, 0.0D, 8.9D, 12.0D, 4.5D, 21.9D, 11.0D, 6.6D, 22.5D)
        );
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.EAST, rotateClockwise(north));
        shapes.put(Direction.SOUTH, rotateClockwise(shapes.get(Direction.EAST)));
        shapes.put(Direction.WEST, rotateClockwise(shapes.get(Direction.SOUTH)));
        return shapes;
    }

    private static VoxelShape voxelY(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                    double originX, double originZ, double degrees) {
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

    private static boolean containsY(double x, double z, double minX, double maxX, double minZ, double maxZ,
                                    double originX, double originZ, double cos, double sin) {
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
