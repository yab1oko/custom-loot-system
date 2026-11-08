package cts.yblk.cts.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import cts.yblk.cts.registry.ModParticles;

/** Spawns a tall orange campfire-style smoke column for airdrops. */
public final class AirdropSmoke {
    private AirdropSmoke() {
    }

    public static void clientColumn(Level level, double x, double y, double z, RandomSource random) {
        if (!level.isClientSide) {
            return;
        }
        double sx = x + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1);
        double sy = y + random.nextDouble() * 0.6D;
        double sz = z + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1);
        level.addAlwaysVisibleParticle(ModParticles.AIRDROP_SMOKE.get(), true, sx, sy, sz, 0.0D, 0.07D, 0.0D);
    }

    public static void serverColumn(ServerLevel level, double x, double y, double z, RandomSource random) {
        double sx = x + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1);
        double sy = y + random.nextDouble() * 0.6D;
        double sz = z + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, ModParticles.AIRDROP_SMOKE.get(), true,
                    sx, sy, sz, 0, 0.0D, 0.07D, 0.0D, 1.0D);
        }
    }

    public static void serverColumnAtCrate(ServerLevel level, BlockPos master, RandomSource random) {
        // 2x2 crate: smoke from the center of the footprint
        serverColumn(level, master.getX() + 1.0D, master.getY() + 1.2D, master.getZ() + 1.0D, random);
    }
}
