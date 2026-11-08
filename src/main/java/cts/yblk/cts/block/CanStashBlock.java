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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import cts.yblk.cts.registry.ModTags;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;

public class CanStashBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LOOTED = BooleanProperty.create("looted");
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes(false);
    private static final Map<Direction, VoxelShape> EMPTY_SHAPES = buildShapes(true);

    public CanStashBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LOOTED, false));
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientBlockExtensions> consumer) {
        consumer.accept(cts.yblk.cts.client.PropBreakParticles.EXTENSION);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOOTED);
    }

    /**
     * Props that stick out of a single block keep the facing the player chose, like stairs.
     */
    public boolean usesPlayerFacing() {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = usesPlayerFacing()
                ? context.getHorizontalDirection()
                : Direction.from2DDataValue(context.getLevel().getRandom().nextInt(4));
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        Direction facing = usesPlayerFacing()
                ? state.getValue(FACING)
                : Direction.from2DDataValue(level.getRandom().nextInt(4));
        level.setBlock(pos, state.setValue(FACING, facing).setValue(LOOTED, false), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return CopiedBlock.item(this, state, level, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(LOOTED) ? EMPTY_SHAPES : SHAPES).get(state.getValue(FACING));
    }

    /** When true, world mesh is BER-driven (lid/door openness via {@link CanStashBlockEntity#clientTick}). */
    protected boolean animatesLid() {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return animatesLid() ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    @Nullable
    protected final <T extends BlockEntity> BlockEntityTicker<T> createLidStashTicker(
            Level level, BlockEntityType<T> type, BlockEntityType<CanStashBlockEntity> expected) {
        return level.isClientSide
                ? createTickerHelper(type, expected, CanStashBlockEntity::clientTick)
                : createTickerHelper(type, expected, CanStashBlockEntity::serverTick);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return player.getAbilities().instabuild ? 1.0F : 0.0F;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CanStashBlockEntity stash)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && player.getAbilities().instabuild) {
            if (!level.isClientSide) {
                NetworkHooks.openScreen((ServerPlayer) player, stash, pos);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CanStashBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CAN_STASH.get(), CanStashBlockEntity::serverTick);
    }

    public static boolean isSearchable(BlockState state) {
        return state.is(ModTags.PROP_CONTAINER);
    }

    private static Map<Direction, VoxelShape> buildShapes(boolean empty) {
        VoxelShape north = empty
                ? Shapes.or(
                        Block.box(3.8D, 0.5D, 1.5D, 8.8D, 7.5D, 6.5D),
                        Block.box(1.5D, 0.5D, 6.7D, 6.5D, 5.5D, 13.7D),
                        voxelCanZ(10.1D, 1.32D, 4.38D, 12.1D, 6.32D, 9.38D, 12.1D, 3.6D, 45.0D))
                : Shapes.or(
                        Block.box(3.8D, 0.0D, 1.5D, 8.8D, 7.0D, 6.5D),
                        voxelCan(16.8D, 0.0D, 8.0D, 21.8D, 2.0D, 13.0D, 11.3D, 18.5D, 45.0D),
                        voxelCan(2.2D, 0.0D, 10.6D, 7.2D, 7.0D, 15.6D, -3.3D, 5.1D, 22.5D));
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.EAST, rotateClockwise(north));
        shapes.put(Direction.SOUTH, rotateClockwise(shapes.get(Direction.EAST)));
        shapes.put(Direction.WEST, rotateClockwise(shapes.get(Direction.SOUTH)));
        return shapes;
    }

    private static VoxelShape voxelCan(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double originX, double originZ, double degrees) {
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        VoxelShape shape = Shapes.empty();
        for (double z = 0.0D; z < 16.0D; z += step) {
            Double runStart = null;
            for (double x = 0.0D; x <= 16.0D; x += step) {
                boolean inside = x < 16.0D && contains(x + step * 0.5D, z + step * 0.5D, minX, maxX, minZ, maxZ, originX, originZ, cos, sin);
                if (inside && runStart == null) {
                    runStart = x;
                } else if (!inside && runStart != null) {
                    shape = Shapes.joinUnoptimized(shape, Block.box(runStart, minY, z, x, maxY, z + step), BooleanOp.OR);
                    runStart = null;
                }
            }
        }
        return shape;
    }

    private static VoxelShape voxelCanZ(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double originX, double originY, double degrees) {
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        VoxelShape shape = Shapes.empty();
        for (double z = Math.max(0.0D, Math.floor(minZ / step) * step); z < Math.min(16.0D, maxZ); z += step) {
            for (double x = 0.0D; x < 16.0D; x += step) {
                Double runStart = null;
                for (double y = 0.0D; y <= 16.0D; y += step) {
                    boolean inside = y < 16.0D && containsZ(x + step * 0.5D, y + step * 0.5D, minX, maxX, minY, maxY, originX, originY, cos, sin);
                    if (inside && runStart == null) {
                        runStart = y;
                    } else if (!inside && runStart != null) {
                        shape = Shapes.joinUnoptimized(shape, Block.box(x, runStart, z, x + step, y, Math.min(16.0D, z + step)), BooleanOp.OR);
                        runStart = null;
                    }
                }
            }
        }
        return shape;
    }

    private static boolean containsZ(double x, double y, double minX, double maxX, double minY, double maxY, double originX, double originY, double cos, double sin) {
        double dx = x - originX;
        double dy = y - originY;
        double localX = originX + dx * cos + dy * sin;
        double localY = originY - dx * sin + dy * cos;
        return localX >= minX && localX <= maxX && localY >= minY && localY <= maxY;
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
