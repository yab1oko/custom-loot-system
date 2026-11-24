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
        double sy = y;
        double sz = z + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1);
        level.addAlwaysVisibleParticle(ModParticles.AIRDROP_SMOKE.get(), true, sx, sy, sz, 0.0D, 0.07D, 0.0D);
    }

    public static void serverColumn(ServerLevel level, double x, double y, double z, RandomSource random) {
        double sx = x + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1);
        double sy = y;
        double sz = z + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, ModParticles.AIRDROP_SMOKE.get(), true,
                    sx, sy, sz, 0, 0.0D, 0.07D, 0.0D, 1.0D);
        }
    }

    public static void serverColumnAtCrate(ServerLevel level, BlockPos master, RandomSource random) {
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(master);
        net.minecraft.core.Direction facing = state.hasProperty(cts.yblk.cts.block.AirdropBlock.FACING)
                ? state.getValue(cts.yblk.cts.block.AirdropBlock.FACING)
                : net.minecraft.core.Direction.NORTH;
        double x = 0.0D;
        double z = 0.0D;
        int n = 0;
        for (int[] part : cts.yblk.cts.block.AirdropStructure.footprint()) {
            BlockPos at = master.offset(cts.yblk.cts.block.AirdropStructure.offset(facing, part[0], part[1]));
            x += at.getX() + 0.5D;
            z += at.getZ() + 0.5D;
            n++;
        }
        if (n == 0) {
            serverColumn(level, master.getX() + 0.5D, master.getY() + 1.8D, master.getZ() + 0.5D, random);
            return;
        }
        serverColumn(level, x / n, master.getY() + 1.85D, z / n, random);
    }
}
