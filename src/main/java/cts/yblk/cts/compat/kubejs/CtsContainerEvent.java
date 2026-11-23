package cts.yblk.cts.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class CtsContainerEvent extends EventJS {
    private final Player player;
    private final BlockPos pos;

    public CtsContainerEvent(@Nullable Player player, BlockPos pos) {
        this.player = player;
        this.pos = pos;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public BlockPos getBlockPos() {
        return pos;
    }
}
