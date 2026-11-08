package cts.yblk.cts.airdrop;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.entity.AirdropEntity;
import cts.yblk.cts.registry.ModSounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = Cts.MODID)
public final class AirdropScheduler {
    private static final List<Pending> PENDING = new ArrayList<>();
    public static final int WARNING_DELAY_TICKS = 100;

    private AirdropScheduler() {
    }

    public static void schedule(ServerLevel level, int x1, int z1, int x2, int z2, String lootTable, int delayTicks) {
        PENDING.add(new Pending(level.dimension(), x1, z1, x2, z2, lootTable, delayTicks));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        Iterator<Pending> it = PENDING.iterator();
        while (it.hasNext()) {
            Pending pending = it.next();
            pending.ticksLeft--;
            if (pending.ticksLeft > 0) {
                continue;
            }
            it.remove();
            ServerLevel level = event.getServer().getLevel(pending.dimension);
            if (level == null) {
                continue;
            }
            spawn(level, pending);
        }
    }

    private static void spawn(ServerLevel level, Pending pending) {
        RandomSource random = level.random;
        int minX = Math.min(pending.x1, pending.x2);
        int maxX = Math.max(pending.x1, pending.x2);
        int minZ = Math.min(pending.z1, pending.z2);
        int maxZ = Math.max(pending.z1, pending.z2);
        int x = minX + random.nextInt(Math.max(1, maxX - minX + 1));
        int z = minZ + random.nextInt(Math.max(1, maxZ - minZ + 1));
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        double y = surface + AirdropEntity.dropHeight();
        y = Math.min(y, level.getMaxBuildHeight() - 4);

        AirdropEntity entity = AirdropEntity.create(level, x + 1.0D, y, z + 1.0D, pending.lootTable);
        if (entity != null) {
            level.addFreshEntity(entity);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.AIRDROP.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("command.cts.airdrop.incoming", x, z), false);
        }
    }

    private static final class Pending {
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        private final int x1;
        private final int z1;
        private final int x2;
        private final int z2;
        private final String lootTable;
        private int ticksLeft;

        private Pending(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
                        int x1, int z1, int x2, int z2, String lootTable, int ticksLeft) {
            this.dimension = dimension;
            this.x1 = x1;
            this.z1 = z1;
            this.x2 = x2;
            this.z2 = z2;
            this.lootTable = lootTable;
            this.ticksLeft = ticksLeft;
        }
    }
}
