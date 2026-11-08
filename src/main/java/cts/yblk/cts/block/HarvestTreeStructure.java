package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.blockentity.HarvestTreeBlockEntity;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Multiblock oak-style tree: stump column + branch layers + foliage.
 * Master is always the base stump ({@link ModBlocks#HARVEST_TREE}).
 */
public final class HarvestTreeStructure {
    public static final int MAX_HEIGHT = 14;
    private static final int CLEAR_RADIUS = 4;

    public enum Size {
        MEDIUM,
        LARGE
    }

    private HarvestTreeStructure() {
    }

    public record Piece(int dx, int dy, int dz, Kind kind, int variant, Direction facing, int style) {
        public BlockPos at(BlockPos master) {
            return master.offset(dx, dy, dz);
        }
    }

    public enum Kind {
        TRUNK,
        BRANCH,
        LEAVES
    }

    public static List<Piece> generate(RandomSource random, Size size) {
        return size == Size.LARGE ? generateLarge(random) : generateMedium(random);
    }

    /** Medium: 2–3 stump, leafy branch layers, modest canopy. */
    private static List<Piece> generateMedium(RandomSource random) {
        int stumpCount = 2 + random.nextInt(2);
        int midBranches = 1 + random.nextInt(2);
        int topBranches = 1 + random.nextInt(2);
        List<Piece> pieces = new ArrayList<>();

        for (int y = 1; y < stumpCount; y++) {
            pieces.add(trunk(0, y, 0));
        }

        int y = stumpCount;
        int leafyStart = stumpCount;
        for (int i = 0; i < midBranches; i++) {
            pieces.add(branch(0, y, 0, random));
            y++;
        }
        for (int i = 0; i < topBranches; i++) {
            pieces.add(branch(0, y, 0, random));
            y++;
        }
        int leafyEnd = y - 1;

        Set<Long> leafKeys = new HashSet<>();
        for (int by = leafyStart; by <= leafyEnd; by++) {
            addLeavesMedium(pieces, leafKeys, by, by == leafyEnd, random);
        }
        return pieces;
    }

    /**
     * Large: 3–5 stump, 1 bare branch, 3–4 leafy branches,
     * wider chaotic canopy (not a solid cube).
     */
    private static List<Piece> generateLarge(RandomSource random) {
        int stumpCount = 3 + random.nextInt(3); // 3-5 including master
        int leafyBranches = 3 + random.nextInt(2); // 3-4
        List<Piece> pieces = new ArrayList<>();

        for (int y = 1; y < stumpCount; y++) {
            pieces.add(trunk(0, y, 0));
        }

        int y = stumpCount;
        pieces.add(branch(0, y, 0, random)); // 1 bare branch layer
        y++;
        int leafyStart = y;
        for (int i = 0; i < leafyBranches; i++) {
            pieces.add(branch(0, y, 0, random));
            y++;
        }
        int leafyEnd = y - 1;

        Set<Long> leafKeys = new HashSet<>();
        for (int by = leafyStart; by <= leafyEnd; by++) {
            addLeavesLarge(pieces, leafKeys, by, by == leafyEnd, random);
        }
        return pieces;
    }

    /** Original small-tree canopy: asymmetric, not a full cube. */
    private static void addLeavesMedium(List<Piece> pieces, Set<Long> leafKeys, int y, boolean crown, RandomSource random) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < 0.82F) {
                putLeaf(pieces, leafKeys, dir.getStepX(), y, dir.getStepZ(), random);
            }
        }
        float diagonalChance = crown ? 0.72F : 0.38F;
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                if (random.nextFloat() < diagonalChance) {
                    putLeaf(pieces, leafKeys, dx, y, dz, random);
                }
            }
        }
        if (random.nextFloat() < 0.28F) {
            Direction poke = randomHorizontal(random);
            putLeaf(pieces, leafKeys, poke.getStepX() * 2, y, poke.getStepZ() * 2, random);
        }
        if (crown && random.nextFloat() < 0.35F) {
            int dx = random.nextBoolean() ? 1 : -1;
            int dz = random.nextBoolean() ? 1 : -1;
            putLeaf(pieces, leafKeys, dx * 2, y, dz, random);
            if (random.nextBoolean()) {
                putLeaf(pieces, leafKeys, dx, y, dz * 2, random);
            }
        }
        if (crown) {
            putLeaf(pieces, leafKeys, 0, y + 1, 0, random);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() < 0.7F) {
                    putLeaf(pieces, leafKeys, dir.getStepX(), y + 1, dir.getStepZ(), random);
                }
            }
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dz = -1; dz <= 1; dz += 2) {
                    if (random.nextFloat() < 0.4F) {
                        putLeaf(pieces, leafKeys, dx, y + 1, dz, random);
                    }
                }
            }
            if (random.nextFloat() < 0.45F) {
                Direction tip = randomHorizontal(random);
                putLeaf(pieces, leafKeys, tip.getStepX(), y + 2, tip.getStepZ(), random);
            }
        }
    }

    /**
     * Wide canopy for large trees: mostly connected, mild asymmetry.
     * Outer leaves only attach next to an already placed inner leaf (no floating blobs).
     */
    private static void addLeavesLarge(List<Piece> pieces, Set<Long> leafKeys, int y, boolean crown, RandomSource random) {
        // Inner 3x3 ring — almost always filled (connected core).
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < 0.95F) {
                putLeaf(pieces, leafKeys, dir.getStepX(), y, dir.getStepZ(), random);
            }
        }
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                if (random.nextFloat() < (crown ? 0.88F : 0.7F)) {
                    putLeaf(pieces, leafKeys, dx, y, dz, random);
                }
            }
        }

        // Radius-2 extensions only when the inward neighbor leaf exists.
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int ix = dir.getStepX();
            int iz = dir.getStepZ();
            if (!hasLeaf(leafKeys, ix, y, iz) || random.nextFloat() >= 0.78F) {
                continue;
            }
            putLeaf(pieces, leafKeys, ix * 2, y, iz * 2, random);
            // Mild side bulges attached to that outer leaf.
            Direction left = dir.getCounterClockWise();
            Direction right = dir.getClockWise();
            if (random.nextFloat() < 0.4F) {
                putLeaf(pieces, leafKeys, ix * 2 + left.getStepX(), y, iz * 2 + left.getStepZ(), random);
            }
            if (random.nextFloat() < 0.4F) {
                putLeaf(pieces, leafKeys, ix * 2 + right.getStepX(), y, iz * 2 + right.getStepZ(), random);
            }
        }
        // Outer diagonals only if the matching inner diagonal is present.
        int[] signs = {-1, 1};
        for (int sx : signs) {
            for (int sz : signs) {
                if (hasLeaf(leafKeys, sx, y, sz) && random.nextFloat() < (crown ? 0.55F : 0.3F)) {
                    putLeaf(pieces, leafKeys, sx * 2, y, sz * 2, random);
                }
            }
        }

        // Rare short bulge (radius 3) — only attached to an existing radius-2 leaf.
        if (random.nextFloat() < 0.3F) {
            Direction poke = randomHorizontal(random);
            int bx = poke.getStepX() * 2;
            int bz = poke.getStepZ() * 2;
            if (hasLeaf(leafKeys, bx, y, bz)) {
                putLeaf(pieces, leafKeys, poke.getStepX() * 3, y, poke.getStepZ() * 3, random);
            }
        }

        if (crown) {
            putLeaf(pieces, leafKeys, 0, y + 1, 0, random);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() < 0.9F) {
                    putLeaf(pieces, leafKeys, dir.getStepX(), y + 1, dir.getStepZ(), random);
                }
            }
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dz = -1; dz <= 1; dz += 2) {
                    if (random.nextFloat() < 0.7F) {
                        putLeaf(pieces, leafKeys, dx, y + 1, dz, random);
                    }
                }
            }
            // Upper outer ring only if inner leaf below/aside exists.
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                int ix = dir.getStepX();
                int iz = dir.getStepZ();
                if (hasLeaf(leafKeys, ix, y + 1, iz) && random.nextFloat() < 0.5F) {
                    putLeaf(pieces, leafKeys, ix * 2, y + 1, iz * 2, random);
                }
            }
            if (random.nextFloat() < 0.8F) {
                putLeaf(pieces, leafKeys, 0, y + 2, 0, random);
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (random.nextFloat() < 0.65F) {
                        putLeaf(pieces, leafKeys, dir.getStepX(), y + 2, dir.getStepZ(), random);
                    }
                }
            }
        }
    }

    private static boolean hasLeaf(Set<Long> leafKeys, int dx, int dy, int dz) {
        return leafKeys.contains(BlockPos.asLong(dx, dy, dz));
    }

    private static Piece trunk(int dx, int dy, int dz) {
        return new Piece(dx, dy, dz, Kind.TRUNK, 0, Direction.NORTH, 0);
    }

    private static Piece branch(int dx, int dy, int dz, RandomSource random) {
        return new Piece(dx, dy, dz, Kind.BRANCH, random.nextInt(3), randomHorizontal(random), 0);
    }

    private static Direction randomHorizontal(RandomSource random) {
        return Direction.Plane.HORIZONTAL.getRandomDirection(random);
    }

    private static void putLeaf(List<Piece> pieces, Set<Long> leafKeys, int dx, int dy, int dz, RandomSource random) {
        long key = BlockPos.asLong(dx, dy, dz);
        if (leafKeys.add(key)) {
            pieces.add(new Piece(dx, dy, dz, Kind.LEAVES, 0, randomHorizontal(random), random.nextInt(2)));
        }
    }

    public static boolean hasSpace(LevelAccessor level, BlockPos master, Size size) {
        int maxY = size == Size.LARGE ? 13 : 9;
        int radius = size == Size.LARGE ? 3 : 2;
        int canopyStart = size == Size.LARGE ? 3 : 2;
        for (int y = 1; y <= maxY; y++) {
            if (!level.getBlockState(master.above(y)).canBeReplaced()) {
                return false;
            }
        }
        for (int y = canopyStart; y <= maxY; y++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    if (!level.getBlockState(master.offset(dx, y, dz)).canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean canGrow(LevelAccessor level, BlockPos master, List<Piece> pieces) {
        for (Piece piece : pieces) {
            BlockPos at = piece.at(master);
            BlockState state = level.getBlockState(at);
            if (!state.canBeReplaced() && !isTreePart(state)) {
                return false;
            }
        }
        return true;
    }

    public static void grow(LevelAccessor level, BlockPos master, RandomSource random, Size size) {
        List<Piece> pieces = generate(random, size);
        if (!canGrow(level, master, pieces)) {
            pieces = minimal(random, size);
            if (!canGrow(level, master, pieces)) {
                return;
            }
        }
        clearParts(level, master);
        for (Piece piece : pieces) {
            level.setBlock(piece.at(master), stateFor(piece), Block.UPDATE_ALL);
        }
    }

    private static List<Piece> minimal(RandomSource random, Size size) {
        if (size == Size.LARGE) {
            List<Piece> pieces = new ArrayList<>();
            pieces.add(trunk(0, 1, 0));
            pieces.add(trunk(0, 2, 0));
            pieces.add(branch(0, 3, 0, random));
            pieces.add(branch(0, 4, 0, random));
            pieces.add(branch(0, 5, 0, random));
            Set<Long> keys = new HashSet<>();
            addLeavesLarge(pieces, keys, 4, false, random);
            addLeavesLarge(pieces, keys, 5, true, random);
            return pieces;
        }
        List<Piece> pieces = new ArrayList<>();
        pieces.add(trunk(0, 1, 0));
        pieces.add(branch(0, 2, 0, random));
        pieces.add(branch(0, 3, 0, random));
        Set<Long> keys = new HashSet<>();
        addLeavesMedium(pieces, keys, 2, false, random);
        addLeavesMedium(pieces, keys, 3, true, random);
        return pieces;
    }

    public static void clearParts(LevelAccessor level, BlockPos master) {
        for (BlockPos at : collectParts(level, master)) {
            if (isTreePart(level.getBlockState(at))) {
                level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public static void removeAll(LevelAccessor level, BlockPos master) {
        clearParts(level, master);
        if (level.getBlockState(master).is(ModBlocks.HARVEST_TREE.get())) {
            level.setBlock(master, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public static List<BlockPos> collectParts(BlockGetter level, BlockPos master) {
        List<BlockPos> found = new ArrayList<>();
        for (int dy = 0; dy <= MAX_HEIGHT; dy++) {
            for (int dx = -CLEAR_RADIUS; dx <= CLEAR_RADIUS; dx++) {
                for (int dz = -CLEAR_RADIUS; dz <= CLEAR_RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos at = master.offset(dx, dy, dz);
                    if (!isTreePart(level.getBlockState(at))) {
                        continue;
                    }
                    BlockPos owner = findMaster(level, at);
                    if (master.equals(owner) || owner == null) {
                        found.add(at);
                    }
                }
            }
        }
        return found;
    }

    public static BlockPos findMaster(BlockGetter level, BlockPos pos) {
        BlockState self = level.getBlockState(pos);
        if (self.is(ModBlocks.HARVEST_TREE.get())) {
            return pos.immutable();
        }
        BlockPos best = null;
        int bestDist = Integer.MAX_VALUE;
        for (int dx = -CLEAR_RADIUS; dx <= CLEAR_RADIUS; dx++) {
            for (int dz = -CLEAR_RADIUS; dz <= CLEAR_RADIUS; dz++) {
                for (int dy = 0; dy <= MAX_HEIGHT; dy++) {
                    BlockPos candidate = pos.offset(dx, -dy, dz);
                    if (!level.getBlockState(candidate).is(ModBlocks.HARVEST_TREE.get())) {
                        continue;
                    }
                    int dist = Math.abs(dx) + Math.abs(dz) + dy;
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = candidate.immutable();
                    }
                }
            }
        }
        return best;
    }

    public static Size sizeOf(BlockGetter level, BlockPos master) {
        if (level.getBlockEntity(master) instanceof HarvestTreeBlockEntity tree) {
            return tree.getTreeSize();
        }
        return Size.MEDIUM;
    }

    public static ItemStack itemFor(BlockGetter level, BlockPos anyTreePos) {
        BlockPos master = findMaster(level, anyTreePos);
        if (master != null && sizeOf(level, master) == Size.LARGE) {
            return new ItemStack(ModItems.HARVEST_TREE_LARGE.get());
        }
        return new ItemStack(ModItems.HARVEST_TREE.get());
    }

    public static boolean isTreePart(BlockState state) {
        return state.is(ModBlocks.HARVEST_TREE_TRUNK.get())
                || state.is(ModBlocks.HARVEST_TREE_BRANCH.get())
                || state.is(ModBlocks.HARVEST_TREE_LEAVES.get());
    }

    public static boolean isTreeBlock(BlockState state) {
        return state.is(ModBlocks.HARVEST_TREE.get()) || isTreePart(state);
    }

    public static boolean isChopTarget(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!isTreeBlock(state)) {
            return false;
        }
        BlockPos master = findMaster(level, pos);
        if (master == null) {
            return false;
        }
        BlockState masterState = level.getBlockState(master);
        return masterState.is(ModBlocks.HARVEST_TREE.get())
                && masterState.hasProperty(HarvestTreeBlock.BROKEN)
                && !masterState.getValue(HarvestTreeBlock.BROKEN);
    }

    private static BlockState stateFor(Piece piece) {
        return switch (piece.kind()) {
            case TRUNK -> ModBlocks.HARVEST_TREE_TRUNK.get().defaultBlockState();
            case BRANCH -> ModBlocks.HARVEST_TREE_BRANCH.get().defaultBlockState()
                    .setValue(HarvestTreeBranchBlock.VARIANT, piece.variant())
                    .setValue(HarvestTreeBranchBlock.FACING, piece.facing());
            case LEAVES -> ModBlocks.HARVEST_TREE_LEAVES.get().defaultBlockState()
                    .setValue(HarvestTreeLeavesBlock.STYLE, piece.style())
                    .setValue(HarvestTreeLeavesBlock.FACING, piece.facing());
        };
    }
}
