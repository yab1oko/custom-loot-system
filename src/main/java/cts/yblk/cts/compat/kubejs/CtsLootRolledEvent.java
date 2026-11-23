package cts.yblk.cts.compat.kubejs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class CtsLootRolledEvent extends CtsContainerEvent {
    private final boolean refilled;

    public CtsLootRolledEvent(@Nullable Player player, BlockPos pos, boolean refilled) {
        super(player, pos);
        this.refilled = refilled;
    }

    public boolean isRefilled() {
        return refilled;
    }
}
