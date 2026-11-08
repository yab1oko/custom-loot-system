package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.BarricadeBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;

/**
 * Wall barricade.
 * <p>
 * {@link #FACING} = direction into the room (toward the player when placed on a wall),
 * same as ladders. Planks sit on the opposite side — against the wall.
 * Models are authored on the north face (Z ≈ 0–2).
 */
public class BarricadeBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);
    public static final IntegerProperty SPIN = IntegerProperty.create("spin", 0, 3);
    public static final BooleanProperty BROKEN = BooleanProperty.create("broken");

    private static final VoxelShape[] NORTH_BASE = {variant1(), variant2(), variant3()};
    /** Indexed by wall-side direction (opposite of FACING). */
    private static final Map<Direction, VoxelShape[][]> SHAPES = buildShapes();

    public BarricadeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VARIANT, 0)
                .setValue(SPIN, 0)
                .setValue(BROKEN, false));
    }

    /** Side of the block that touches the wall (planks live here). */
    public static Direction wallSide(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT, SPIN, BROKEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        // Into the room / toward the player (ladder-style).
        Direction intoRoom = clicked.getAxis().isHorizontal()
                ? clicked
                : context.getHorizontalDirection();
        RandomSource random = context.getLevel().getRandom();
        return this.defaultBlockState()
                .setValue(FACING, intoRoom)
                .setValue(VARIANT, random.nextInt(3))
                // Spin stays 0: 90° Z turns the whole frame on its side and looks broken.
                .setValue(SPIN, 0)
                .setValue(BROKEN, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Broken = intangible in survival (no LMB hit). Creative keeps the outline for editing.
        if (state.getValue(BROKEN) && !isCreativeContext(context)) {
            return Shapes.empty();
        }
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BROKEN) ? Shapes.empty() : shapeFor(state);
    }

    private static boolean isCreativeContext(CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            return entity instanceof Player player && player.getAbilities().instabuild;
        }
        return false;
    }

    private static VoxelShape shapeFor(BlockState state) {
        Direction wall = wallSide(state);
        return SHAPES.get(wall)[state.getValue(VARIANT)][state.getValue(SPIN)];
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return player.getAbilities().instabuild ? 1.0F : 0.0F;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BarricadeBlockEntity barricade)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && BarricadeBlockEntity.canConfigure(player)) {
            if (!level.isClientSide) {
                NetworkHooks.openScreen((ServerPlayer) player, barricade, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarricadeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.BARRICADE.get(), BarricadeBlockEntity::serverTick);
    }

    public static boolean isBreakableTarget(BlockState state) {
        return state.getBlock() instanceof BarricadeBlock
                && state.hasProperty(BROKEN)
                && !state.getValue(BROKEN);
    }

    private static Map<Direction, VoxelShape[][]> buildShapes() {
        Map<Direction, VoxelShape[][]> shapes = new EnumMap<>(Direction.class);
        for (Direction wall : Direction.Plane.HORIZONTAL) {
            VoxelShape[][] byVariant = new VoxelShape[3][4];
            // Same CW steps as WeaponCase.rotateClockwise, indexed by wall:
            // NORTH=0, EAST=1, SOUTH=2, WEST=3. Renderer applies -wallYRot.
            int ySteps = wallSteps(wall);
            for (int variant = 0; variant < 3; variant++) {
                for (int spin = 0; spin < 4; spin++) {
                    VoxelShape shape = rotateZ90(NORTH_BASE[variant], spin);
                    for (int i = 0; i < ySteps; i++) {
                        shape = rotateClockwiseY(shape);
                    }
                    byVariant[variant][spin] = shape;
                }
            }
            shapes.put(wall, byVariant);
        }
        return shapes;
    }

    /** Clockwise 90° steps to move north-face planks onto {@code wall}. */
    private static int wallSteps(Direction wall) {
        return switch (wall) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    /** Degrees for PoseStack so it matches {@link #wallSteps} (negate: Y+ is CCW). */
    public static float wallYRot(BlockState state) {
        return wallSteps(wallSide(state)) * 90.0F;
    }

    private static VoxelShape rotateZ90(VoxelShape shape, int steps) {
        steps &= 3;
        VoxelShape result = shape;
        for (int i = 0; i < steps; i++) {
            VoxelShape[] rotated = {Shapes.empty()};
            result.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    rotated[0] = Shapes.or(rotated[0],
                            Shapes.box(1.0D - maxY, minX, minZ, 1.0D - minY, maxX, maxZ)));
            result = rotated[0];
        }
        return result;
    }

    /** Same as WeaponCase.rotateClockwise: north face → east face. */
    private static VoxelShape rotateClockwiseY(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0],
                        Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
        return rotated[0];
    }

    // North-authored planks (match barricade_1/2/3.json, Z ≈ 0–2).
    private static VoxelShape variant1() {
        return Shapes.or(
                plank(10.94D, 0.87D, 1.1D, 14.94D, 20.87D, 2.1D, 8.0D, 9.5D, 45.0D),
                plank(-2.53D, 14.18D, 0.1D, 17.47D, 18.18D, 1.1D, 9.0D, 11.5D, 45.0D),
                plank(-0.41D, 0.90D, 1.1D, 17.59D, 4.90D, 2.1D, 11.0D, 8.5D, 0.0D),
                plank(-3.45D, 3.49D, 0.1D, 14.55D, 7.49D, 1.1D, 5.0D, 9.5D, 22.5D)
        );
    }

    private static VoxelShape variant2() {
        return Shapes.or(
                plank(-0.63D, 2.56D, 1.0D, 19.37D, 6.56D, 2.0D, 8.0D, 9.5D, -22.5D),
                plank(1.99D, -0.05D, 0.0D, 5.99D, 17.95D, 1.0D, 8.0D, 9.5D, -45.0D),
                plank(1.41D, 13.10D, 1.0D, 19.41D, 17.10D, 2.0D, 8.0D, 9.5D, 22.5D),
                plank(10.68D, 1.03D, 0.0D, 14.68D, 21.03D, 1.0D, 8.0D, 9.5D, -22.5D)
        );
    }

    private static VoxelShape variant3() {
        return Shapes.or(
                plank(1.06D, -0.87D, 1.0D, 5.06D, 19.13D, 2.0D, 8.0D, 10.5D, -22.5D),
                plank(-2.55D, 12.51D, 0.0D, 15.45D, 16.51D, 1.0D, 7.0D, 10.5D, -45.0D),
                plank(11.60D, 4.09D, 1.0D, 15.60D, 22.09D, 2.0D, 8.0D, 15.5D, -22.5D),
                plank(-0.47D, 4.82D, 0.0D, 19.53D, 8.82D, 1.0D, 8.0D, 11.5D, -22.5D)
        );
    }

    private static VoxelShape plank(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                   double originX, double originY, double degrees) {
        if (degrees == 0.0D) {
            return Block.box(minX, minY, minZ, maxX, maxY, maxZ);
        }
        double cos = Math.cos(Math.toRadians(degrees));
        double sin = Math.sin(Math.toRadians(degrees));
        double step = 0.5D;
        VoxelShape shape = Shapes.empty();
        double z0 = Math.floor(minZ / step) * step;
        double z1 = Math.ceil(maxZ / step) * step;
        for (double z = z0; z < z1; z += step) {
            for (double x = -4.0D; x < 20.0D; x += step) {
                Double runStart = null;
                for (double y = -3.0D; y <= 23.0D; y += step) {
                    boolean inside = y < 23.0D && containsRotated(
                            x + step * 0.5D, y + step * 0.5D,
                            minX, maxX, minY, maxY, originX, originY, cos, sin);
                    if (inside && runStart == null) {
                        runStart = y;
                    } else if (!inside && runStart != null) {
                        shape = Shapes.joinUnoptimized(shape,
                                Block.box(x, runStart, z, x + step, y, z + step), BooleanOp.OR);
                        runStart = null;
                    }
                }
            }
        }
        return shape.optimize();
    }

    private static boolean containsRotated(double x, double y, double minX, double maxX, double minY, double maxY,
                                           double originX, double originY, double cos, double sin) {
        double dx = x - originX;
        double dy = y - originY;
        double localX = originX + dx * cos + dy * sin;
        double localY = originY - dx * sin + dy * cos;
        return localX >= minX && localX <= maxX && localY >= minY && localY <= maxY;
    }
}
