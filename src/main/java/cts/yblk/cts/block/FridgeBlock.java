package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModBlocks;

import java.util.EnumMap;
import java.util.Map;

public class FridgeBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();
    private static boolean breaking;

    public static boolean isBreaking() {
        return breaking;
    }

    public FridgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean usesPlayerFacing() {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockPos above = pos.above();
        if (!context.getLevel().getBlockState(above).canBeReplaced()) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        Direction facing = state.getValue(FACING);
        level.setBlock(pos, state.setValue(FACING, facing).setValue(LOOTED, false),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.setBlock(pos.above(), ModBlocks.FRIDGE_PART.get().defaultBlockState()
                        .setValue(FridgePartBlock.FACING, facing),
                Block.UPDATE_ALL);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && !breaking) {
            breaking = true;
            try {
                BlockPos above = pos.above();
                if (level.getBlockState(above).is(ModBlocks.FRIDGE_PART.get())) {
                    level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            } finally {
                breaking = false;
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(ShapeFacing.of(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected boolean animatesLid() {
        return true;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createLidStashTicker(level, type, ModBlockEntities.FRIDGE.get());
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        VoxelShape north = Shapes.or(
                Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 15.0D),
                Block.box(0.0D, 2.0D, 0.0D, 1.0D, 16.0D, 15.0D),
                Block.box(15.0D, 2.0D, 0.0D, 16.0D, 16.0D, 15.0D),
                Block.box(1.0D, 2.0D, 0.0D, 15.0D, 16.0D, 1.0D),
                Block.box(0.0D, 0.0D, 15.0D, 15.0D, 16.0D, 16.0D),
                Block.box(1.0D, 10.0D, 0.0D, 15.0D, 10.5D, 12.0D));
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.EAST, rotateClockwise(north));
        shapes.put(Direction.SOUTH, rotateClockwise(shapes.get(Direction.EAST)));
        shapes.put(Direction.WEST, rotateClockwise(shapes.get(Direction.SOUTH)));
        return shapes;
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0], Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
        return rotated[0];
    }
}
