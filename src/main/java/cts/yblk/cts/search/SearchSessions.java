package cts.yblk.cts.search;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side record of an in-progress container search.
 * Opening the loot is rejected until this timer has elapsed.
 */
@Mod.EventBusSubscriber(modid = Cts.MODID)
public final class SearchSessions {
    private static final int GRACE_TICKS = 3;
    private static final int STALE_TICKS = 40;
    private static final Map<UUID, Active> ACTIVE = new ConcurrentHashMap<>();

    private SearchSessions() {
    }

    public static void start(ServerPlayer player, BlockPos inventory) {
        if (!(player.level().getBlockEntity(inventory) instanceof CanStashBlockEntity stash)) {
            return;
        }
        int required = Math.max(1, stash.getEffectiveSearchSeconds() * 20);
        ACTIVE.put(player.getUUID(), new Active(inventory.immutable(), player.serverLevel().getGameTime(), required));
    }

    public static void stop(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
    }

    public static boolean allowOpen(ServerPlayer player, BlockPos inventory) {
        Active active = ACTIVE.get(player.getUUID());
        if (active == null || !active.pos.equals(inventory)) {
            return false;
        }
        if (player.distanceToSqr(Vec3.atCenterOf(inventory)) > 64.0D) {
            ACTIVE.remove(player.getUUID());
            return false;
        }
        long elapsed = player.serverLevel().getGameTime() - active.startTick;
        if (elapsed + GRACE_TICKS < active.requiredTicks) {
            return false;
        }
        ACTIVE.remove(player.getUUID());
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            long now = level.getGameTime();
            Iterator<Map.Entry<UUID, Active>> iterator = ACTIVE.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Active> entry = iterator.next();
                ServerPlayer player = level.getPlayerByUUID(entry.getKey()) instanceof ServerPlayer found ? found : null;
                if (player == null || player.serverLevel() != level) {
                    continue;
                }
                Active active = entry.getValue();
                boolean stale = now - active.startTick > active.requiredTicks + STALE_TICKS;
                boolean far = player.distanceToSqr(Vec3.atCenterOf(active.pos)) > 64.0D;
                if (stale || far) {
                    iterator.remove();
                }
            }
        }
    }

    private static final class Active {
        private final BlockPos pos;
        private final long startTick;
        private final int requiredTicks;

        private Active(BlockPos pos, long startTick, int requiredTicks) {
            this.pos = pos;
            this.startTick = startTick;
            this.requiredTicks = requiredTicks;
        }
    }
}
