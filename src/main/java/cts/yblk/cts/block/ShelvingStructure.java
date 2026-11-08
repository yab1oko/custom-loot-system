package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class ShelvingStructure {
    private static final int[][] PARTS = {
            {-1, 0}, {1, 0},
            {-1, 1}, {0, 1}, {1, 1}
    };

    private ShelvingStructure() {
    }

    public static BlockPos offset(Direction facing, int localX, int localY) {
        return switch (facing) {
            case SOUTH -> new BlockPos(-localX, localY, 0);
            case WEST -> new BlockPos(0, localY, -localX);
            case EAST -> new BlockPos(0, localY, localX);
            default -> new BlockPos(localX, localY, 0);
        };
    }

    public static List<BlockPos> partPositions(BlockPos master, Direction facing) {
        List<BlockPos> positions = new ArrayList<>(PARTS.length);
        for (int[] part : PARTS) {
            positions.add(master.offset(offset(facing, part[0], part[1])));
        }
        return positions;
    }

    public static boolean canPlace(LevelAccessor level, BlockPos master, Direction facing, BlockPos skip) {
        for (BlockPos part : partPositions(master, facing)) {
            if (part.equals(skip)) {
                continue;
            }
            BlockState state = level.getBlockState(part);
            if (!state.canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    public static BlockPos masterFromPart(BlockPos part, Direction facing, int localX, int localY) {
        return part.subtract(offset(facing, localX, localY));
    }

    public static int[][] parts() {
        return PARTS;
    }
}
