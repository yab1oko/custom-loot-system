package cts.yblk.cts.compat.kubejs;

import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

final class CtsKubeHooks {
    private CtsKubeHooks() {
    }

    static void lootRolled(@Nullable Player player, BlockPos pos, boolean refilled) {
        CtsKubeEvents.LOOT_ROLLED.post(ScriptType.SERVER, new CtsLootRolledEvent(player, pos, refilled));
        if (refilled) {
            CtsKubeEvents.CONTAINER_REFILLED.post(ScriptType.SERVER, new CtsContainerEvent(player, pos));
        }
    }

    static void opened(@Nullable Player player, BlockPos pos) {
        CtsKubeEvents.CONTAINER_OPENED.post(ScriptType.SERVER, new CtsContainerEvent(player, pos));
    }

    static void airdropLanded(BlockPos pos) {
        CtsKubeEvents.AIRDROP_LANDED.post(ScriptType.SERVER, new CtsContainerEvent(null, pos));
    }
}
