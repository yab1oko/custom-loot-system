package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

public class ShelvingPartBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Stored as localX + 1 so values stay >= 0 (IntegerProperty requirement). */
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 2);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 1);

    public ShelvingPartBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OFFSET_X, 1)
                .setValue(OFFSET_Y, 0));
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientBlockExtensions> consumer) {
        consumer.accept(cts.yblk.cts.client.PropBreakParticles.EXTENSION);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OFFSET_X, OFFSET_Y);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        int localX = state.getValue(OFFSET_X) - 1;
        int localY = state.getValue(OFFSET_Y);
        BlockPos master = master(pos, state);
        BlockState masterState = level.getBlockState(master);
        boolean empty = true;
        int variant = 1;
        if (masterState.getBlock() instanceof ShelvingBlock) {
            empty = masterState.getValue(ShelvingBlock.LOOTED);
            variant = masterState.getValue(ShelvingBlock.VARIANT);
        }
        return ShelvingShapes.shape(facing, localX, localY, empty, variant);
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
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
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

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        BlockPos master = master(pos, state);
        BlockState masterState = level.getBlockState(master);
        if (masterState.getBlock() instanceof ShelvingBlock shelving) {
            return CopiedBlock.item(shelving, masterState, level, master);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos master = master(pos, state);
        if (!(level.getBlockEntity(master) instanceof CanStashBlockEntity stash)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && player.getAbilities().instabuild) {
            if (!level.isClientSide) {
                NetworkHooks.openScreen((ServerPlayer) player, stash, master);
            }
            return InteractionResult.CONSUME;
        }
        if (player.getAbilities().instabuild) {
            if (!level.isClientSide) {
                stash.openChest((ServerPlayer) player);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && !ShelvingBlock.isBreaking()) {
            BlockPos master = master(pos, state);
            BlockState masterState = level.getBlockState(master);
            if (masterState.getBlock() instanceof ShelvingBlock) {
                level.destroyBlock(master, false);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static BlockPos master(BlockPos part, BlockState state) {
        return ShelvingStructure.masterFromPart(part, state.getValue(FACING),
                state.getValue(OFFSET_X) - 1, state.getValue(OFFSET_Y));
    }

    public static int encodeOffsetX(int localX) {
        return localX + 1;
    }
}
