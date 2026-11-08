package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import cts.yblk.cts.registry.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public final class AirdropStructure {
    private AirdropStructure() {
    }

    public static int[][] footprint() {
        // BB model spans roughly X -16..18, Z -2..32 from master → cells (-1..0, 0..1).
        return new int[][]{{0, 0}, {-1, 0}, {0, 1}, {-1, 1}};
    }

    public static BlockPos offset(Direction facing, int localX, int localZ) {
        return switch (facing) {
            case EAST -> new BlockPos(-localZ, 0, localX);
            case SOUTH -> new BlockPos(-localX, 0, -localZ);
            case WEST -> new BlockPos(localZ, 0, -localX);
            default -> new BlockPos(localX, 0, localZ);
        };
    }

    public static List<BlockPos> allPositions(BlockPos master, Direction facing) {
        List<BlockPos> list = new ArrayList<>(8);
        for (int[] part : footprint()) {
            BlockPos base = master.offset(offset(facing, part[0], part[1]));
            list.add(base);
            list.add(base.above());
        }
        return list;
    }

    public static boolean canPlace(LevelAccessor level, BlockPos master, Direction facing) {
        for (BlockPos at : allPositions(master, facing)) {
            if (!level.getBlockState(at).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    public static BlockPos masterFromPart(BlockGetter level, BlockPos part) {
        BlockState partState = level.getBlockState(part);
        Direction facing = partState.hasProperty(AirdropPartBlock.FACING)
                ? partState.getValue(AirdropPartBlock.FACING)
                : Direction.NORTH;
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos layer = part.below(dy);
            for (int[] local : footprint()) {
                BlockPos candidate = layer.subtract(offset(facing, local[0], local[1]));
                if (level.getBlockState(candidate).is(ModBlocks.AIRDROP.get())) {
                    return candidate;
                }
            }
        }
        return part;
    }

    public static void place(LevelAccessor level, BlockPos master, Direction facing) {
        level.setBlock(master, ModBlocks.AIRDROP.get().defaultBlockState()
                        .setValue(AirdropBlock.FACING, facing)
                        .setValue(AirdropBlock.LOOTED, false),
                Block.UPDATE_ALL);
        for (BlockPos at : allPositions(master, facing)) {
            if (at.equals(master)) {
                continue;
            }
            level.setBlock(at, ModBlocks.AIRDROP_PART.get().defaultBlockState()
                            .setValue(AirdropPartBlock.FACING, facing),
                    Block.UPDATE_ALL);
        }
    }

    public static void remove(LevelAccessor level, BlockPos master, Direction facing) {
        for (BlockPos at : allPositions(master, facing)) {
            BlockState state = level.getBlockState(at);
            if (state.is(ModBlocks.AIRDROP.get()) || state.is(ModBlocks.AIRDROP_PART.get())) {
                level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public static BlockPos pickMaster(LevelAccessor level, int x, int z, Direction facing, RandomSource random) {
        for (int attempt = 0; attempt < 16; attempt++) {
            int ox = attempt == 0 ? 0 : random.nextInt(5) - 2;
            int oz = attempt == 0 ? 0 : random.nextInt(5) - 2;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + ox, z + oz);
            BlockPos master = new BlockPos(x + ox, y, z + oz);
            if (canPlace(level, master, facing)) {
                return master;
            }
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }
}
