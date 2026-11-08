package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.VentilationGrilleBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;

/**
 * Wall ventilation grille. South-authored model (Z 15–16).
 * {@link #FACING} = into the room (ladder-style); grille sits on the opposite wall face.
 */
public class VentilationGrilleBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty BROKEN = BooleanProperty.create("broken");

    private static final VoxelShape SOUTH = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    public VentilationGrilleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(BROKEN, false));
    }

    public static Direction wallSide(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BROKEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        Direction intoRoom = clicked.getAxis().isHorizontal()
                ? clicked
                : context.getHorizontalDirection();
        return this.defaultBlockState()
                .setValue(FACING, intoRoom)
                .setValue(BROKEN, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(BROKEN) && !isCreativeContext(context)) {
            return Shapes.empty();
        }
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BROKEN) ? Shapes.empty() : SHAPES.get(state.getValue(FACING));
    }

    private static boolean isCreativeContext(CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            return entity instanceof Player player && player.getAbilities().instabuild;
        }
        return false;
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof VentilationGrilleBlockEntity grille)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && VentilationGrilleBlockEntity.canConfigure(player)) {
            if (!level.isClientSide) {
                NetworkHooks.openScreen((ServerPlayer) player, grille, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VentilationGrilleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.VENTILATION_GRILLE.get(), VentilationGrilleBlockEntity::serverTick);
    }

    public static boolean isOpenableTarget(BlockState state) {
        return state.getBlock() instanceof VentilationGrilleBlock
                && state.hasProperty(BROKEN)
                && !state.getValue(BROKEN);
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        // South-authored: FACING north (wall south) = base. Then CW per facing.
        shapes.put(Direction.NORTH, SOUTH);
        shapes.put(Direction.EAST, rotateClockwiseY(SOUTH));
        shapes.put(Direction.SOUTH, rotateClockwiseY(shapes.get(Direction.EAST)));
        shapes.put(Direction.WEST, rotateClockwiseY(shapes.get(Direction.SOUTH)));
        return shapes;
    }

    private static VoxelShape rotateClockwiseY(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0],
                        Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
        return rotated[0];
    }
}
