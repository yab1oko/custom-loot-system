package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.HarvestTreeBlockEntity;
import cts.yblk.cts.item.HarvestTreeItem;
import cts.yblk.cts.registry.ModBlockEntities;

/**
 * Master stump of a multiblock harvest tree. Grown canopy is built from trunk/branch/leaves parts.
 */
public class HarvestTreeBlock extends BaseEntityBlock {
    public static final BooleanProperty BROKEN = BooleanProperty.create("broken");

    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape STUMP_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);
    private static boolean mutating;

    public HarvestTreeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BROKEN, false));
    }

    public static boolean isMutating() {
        return mutating;
    }

    public static void destroyWhole(Level level, BlockPos master) {
        if (level.isClientSide || mutating) {
            return;
        }
        mutating = true;
        HarvestTreeTrunkBlock.setClearing(true);
        try {
            HarvestTreeStructure.removeAll(level, master);
        } finally {
            HarvestTreeTrunkBlock.setClearing(false);
            mutating = false;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BROKEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        HarvestTreeStructure.Size size = sizeFrom(context.getItemInHand());
        if (!HarvestTreeStructure.hasSpace(context.getLevel(), context.getClickedPos(), size)) {
            return null;
        }
        return this.defaultBlockState().setValue(BROKEN, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        HarvestTreeStructure.Size size = sizeFrom(stack);
        if (level.getBlockEntity(pos) instanceof HarvestTreeBlockEntity tree) {
            tree.setTreeSize(size);
        }
        mutating = true;
        HarvestTreeTrunkBlock.setClearing(true);
        try {
            HarvestTreeStructure.grow(level, pos, level.getRandom(), size);
        } finally {
            HarvestTreeTrunkBlock.setClearing(false);
            mutating = false;
        }
    }

    private static HarvestTreeStructure.Size sizeFrom(ItemStack stack) {
        if (stack.getItem() instanceof HarvestTreeItem item) {
            return item.getTreeSize();
        }
        return HarvestTreeStructure.Size.MEDIUM;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && !mutating) {
            mutating = true;
            HarvestTreeTrunkBlock.setClearing(true);
            try {
                HarvestTreeStructure.clearParts(level, pos);
            } finally {
                HarvestTreeTrunkBlock.setClearing(false);
                mutating = false;
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BROKEN) ? STUMP_SHAPE : SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BROKEN) ? STUMP_SHAPE : SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return player.getAbilities().instabuild ? 1.0F : 0.0F;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return HarvestTreeStructure.itemFor(level, pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof HarvestTreeBlockEntity tree)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && HarvestTreeBlockEntity.canConfigure(player)) {
            if (!level.isClientSide) {
                NetworkHooks.openScreen((ServerPlayer) player, tree, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HarvestTreeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.HARVEST_TREE.get(), HarvestTreeBlockEntity::serverTick);
    }

    public static boolean isChopTarget(Level level, BlockPos pos) {
        return HarvestTreeStructure.isChopTarget(level, pos);
    }
}
