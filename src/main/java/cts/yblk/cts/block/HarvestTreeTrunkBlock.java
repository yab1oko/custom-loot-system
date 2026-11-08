package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import cts.yblk.cts.blockentity.HarvestTreeBlockEntity;
import cts.yblk.cts.registry.ModBlocks;

public class HarvestTreeTrunkBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static boolean clearing;

    public HarvestTreeTrunkBlock(Properties properties) {
        super(properties);
    }

    public static boolean isClearing() {
        return clearing;
    }

    public static void setClearing(boolean value) {
        clearing = value;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
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
        BlockPos master = HarvestTreeStructure.findMaster(level, pos);
        if (master == null || !(level.getBlockEntity(master) instanceof HarvestTreeBlockEntity tree)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && HarvestTreeBlockEntity.canConfigure(player)) {
            if (!level.isClientSide) {
                NetworkHooks.openScreen((ServerPlayer) player, tree, master);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && !clearing && !HarvestTreeBlock.isMutating()) {
            BlockPos master = HarvestTreeStructure.findMaster(level, pos);
            if (master != null && level.getBlockState(master).is(ModBlocks.HARVEST_TREE.get())) {
                HarvestTreeBlock.destroyWhole(level, master);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
