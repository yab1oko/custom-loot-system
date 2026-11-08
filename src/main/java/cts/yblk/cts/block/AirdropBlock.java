package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;

public class AirdropBlock extends CanStashBlock {
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();
    private static boolean breaking;

    public static boolean isBreaking() {
        return breaking;
    }

    public AirdropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientBlockExtensions> consumer) {
        consumer.accept(cts.yblk.cts.client.AirdropBreakParticles.EXTENSION);
    }

    @Override
    public boolean usesPlayerFacing() {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        if (!AirdropStructure.canPlace(context.getLevel(), pos, facing)) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(LOOTED, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        Direction facing = state.getValue(FACING);
        AirdropStructure.place(level, pos, facing);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && !breaking) {
            breaking = true;
            try {
                AirdropStructure.remove(level, pos, state.getValue(FACING));
            } finally {
                breaking = false;
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static void destroyStructure(Level level, BlockPos master) {
        if (level.isClientSide || breaking) {
            return;
        }
        BlockState state = level.getBlockState(master);
        if (!(state.getBlock() instanceof AirdropBlock)) {
            return;
        }
        breaking = true;
        try {
            AirdropStructure.remove(level, master, state.getValue(FACING));
        } finally {
            breaking = false;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.AIRDROP.get(), CanStashBlockEntity::serverTick);
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        VoxelShape cell = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, cell);
        shapes.put(Direction.EAST, cell);
        shapes.put(Direction.SOUTH, cell);
        shapes.put(Direction.WEST, cell);
        return shapes;
    }
}
