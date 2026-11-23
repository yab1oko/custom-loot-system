package cts.yblk.cts.compat.kubejs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Always-loaded entry. The KubeJS classes are referenced only after the mod is confirmed loaded,
 * so a dedicated server without KubeJS never loads them.
 */
public final class CtsKubeBridge {
    private static final Logger LOGGER = LogManager.getLogger();

    private CtsKubeBridge() {
    }

    public static void lootRolled(Player player, BlockPos pos, boolean refilled) {
        if (!ModList.get().isLoaded("kubejs")) {
            return;
        }
        try {
            CtsKubeHooks.lootRolled(player, pos, refilled);
        } catch (Throwable error) {
            LOGGER.error("CTS KubeJS lootRolled failed", error);
        }
    }

    public static void opened(Player player, BlockPos pos) {
        if (!ModList.get().isLoaded("kubejs")) {
            return;
        }
        try {
            CtsKubeHooks.opened(player, pos);
        } catch (Throwable error) {
            LOGGER.error("CTS KubeJS containerOpened failed", error);
        }
    }

    public static void airdropLanded(BlockPos pos) {
        if (!ModList.get().isLoaded("kubejs")) {
            return;
        }
        try {
            CtsKubeHooks.airdropLanded(pos);
        } catch (Throwable error) {
            LOGGER.error("CTS KubeJS airdropLanded failed", error);
        }
    }
}
