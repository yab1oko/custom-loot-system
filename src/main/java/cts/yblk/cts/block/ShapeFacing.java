package cts.yblk.cts.block;

import net.minecraft.core.Direction;

/**
 * Animated props render facing the other way from the clockwise shape tables, so east and west swap.
 * Static props keep the original table. Toolbox corrects facing on its own.
 */
final class ShapeFacing {
    private ShapeFacing() {
    }

    static Direction of(Direction facing) {
        return facing.getAxis() == Direction.Axis.X ? facing.getOpposite() : facing;
    }
}
