package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModBlocks;

public class ShelvingBlock extends CanStashBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 1, 3);
    private static boolean breaking;

    public static boolean isBreaking() {
        return breaking;
    }

    public ShelvingBlock(Properties properties) {
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
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        if (!ShelvingStructure.canPlace(context.getLevel(), pos, facing, pos)) {
            return null;
        }
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(VARIANT, randomVariant(context.getLevel().getRandom()));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        Direction facing = state.getValue(FACING);
        int variant = state.hasProperty(VARIANT) ? state.getValue(VARIANT) : randomVariant(level.getRandom());
        level.setBlock(pos, state.setValue(FACING, facing).setValue(LOOTED, false).setValue(VARIANT, variant),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        for (int[] part : ShelvingStructure.parts()) {
            BlockPos at = pos.offset(ShelvingStructure.offset(facing, part[0], part[1]));
            level.setBlock(at, ModBlocks.SHELVING_PART.get().defaultBlockState()
                    .setValue(ShelvingPartBlock.FACING, facing)
                    .setValue(ShelvingPartBlock.OFFSET_X, ShelvingPartBlock.encodeOffsetX(part[0]))
                    .setValue(ShelvingPartBlock.OFFSET_Y, part[1]),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && !breaking) {
            breaking = true;
            try {
                Direction facing = state.getValue(FACING);
                for (BlockPos part : ShelvingStructure.partPositions(pos, facing)) {
                    if (level.getBlockState(part).is(ModBlocks.SHELVING_PART.get())) {
                        level.setBlock(part, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            } finally {
                breaking = false;
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ShelvingShapes.shape(
                state.getValue(FACING),
                0,
                0,
                state.getValue(LOOTED),
                state.getValue(VARIANT));
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
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return player.getAbilities().instabuild ? 1.0F : 0.0F;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.SHELVING.get(), CanStashBlockEntity::serverTick);
    }

    public static int randomVariant(RandomSource random) {
        return 1 + random.nextInt(3);
    }

    public static BlockPos inventoryPos(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ShelvingPartBlock) {
            return ShelvingPartBlock.master(pos, state);
        }
        if (state.getBlock() instanceof FridgePartBlock) {
            return FridgePartBlock.master(pos);
        }
        if (state.getBlock() instanceof AirdropPartBlock || state.getBlock() instanceof AirdropBlock) {
            return AirdropStructure.masterFromPart(level, pos);
        }
        return pos;
    }
}
