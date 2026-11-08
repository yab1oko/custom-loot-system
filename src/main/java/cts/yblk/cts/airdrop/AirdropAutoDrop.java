package cts.yblk.cts.airdrop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.config.CtsConfig;

/**
 * Persistent periodic airdrop: random point within radius of a fixed center, default loot table.
 */
@Mod.EventBusSubscriber(modid = Cts.MODID)
public final class AirdropAutoDrop {
    private static final String DATA_NAME = Cts.MODID + "_airdrop_auto";

    private AirdropAutoDrop() {
    }

    public static State get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(State::load, State::new, DATA_NAME);
    }

    public static void enable(ServerLevel level, int centerX, int centerZ, int radius, int intervalMinutes, String lootTable) {
        State state = get(level);
        state.enabled = true;
        state.dimension = level.dimension().location().toString();
        state.centerX = centerX;
        state.centerZ = centerZ;
        state.radius = Math.max(1, radius);
        state.intervalTicks = Math.max(20, intervalMinutes * 60 * 20);
        state.ticksRemaining = state.intervalTicks;
        state.lootTable = lootTable;
        state.setDirty();
    }

    public static void clear(ServerLevel level) {
        State state = get(level);
        state.enabled = false;
        state.ticksRemaining = 0;
        state.setDirty();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();
        State state = get(overworld);
        if (!state.enabled) {
            return;
        }
        state.ticksRemaining--;
        if (state.ticksRemaining % 20 == 0) {
            state.setDirty();
        }
        if (state.ticksRemaining > 0) {
            return;
        }
        state.ticksRemaining = state.intervalTicks;
        state.setDirty();

        ResourceLocation dimId = ResourceLocation.tryParse(state.dimension);
        ServerLevel target = dimId == null ? overworld : server.getLevel(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimId));
        if (target == null) {
            target = overworld;
        }

        double angle = target.random.nextDouble() * Math.PI * 2.0D;
        double dist = Math.sqrt(target.random.nextDouble()) * state.radius;
        int x = state.centerX + (int) Math.round(Math.cos(angle) * dist);
        int z = state.centerZ + (int) Math.round(Math.sin(angle) * dist);
        String table = state.lootTable == null || state.lootTable.isBlank()
                ? CtsConfig.defaultAirdropLootTable()
                : state.lootTable;

        server.getPlayerList().broadcastSystemMessage(
                Component.translatable("command.cts.airdrop.warn"), false);
        AirdropScheduler.schedule(target, x, z, x, z, table, AirdropScheduler.WARNING_DELAY_TICKS);
    }

    public static final class State extends SavedData {
        private boolean enabled;
        private String dimension = Level.OVERWORLD.location().toString();
        private int centerX;
        private int centerZ;
        private int radius = 64;
        private int intervalTicks = 3 * 60 * 60 * 20;
        private int ticksRemaining;
        private String lootTable = CtsConfig.FALLBACK_AIRDROP_LOOT_TABLE;

        public boolean isEnabled() {
            return enabled;
        }

        public int getCenterX() {
            return centerX;
        }

        public int getCenterZ() {
            return centerZ;
        }

        public int getRadius() {
            return radius;
        }

        public int getIntervalTicks() {
            return intervalTicks;
        }

        public int getTicksRemaining() {
            return ticksRemaining;
        }

        public String getLootTable() {
            return lootTable;
        }

        public String getDimension() {
            return dimension;
        }

        public static State load(CompoundTag tag) {
            State state = new State();
            state.enabled = tag.getBoolean("Enabled");
            state.dimension = tag.contains("Dimension") ? tag.getString("Dimension") : Level.OVERWORLD.location().toString();
            state.centerX = tag.getInt("CenterX");
            state.centerZ = tag.getInt("CenterZ");
            state.radius = Math.max(1, tag.getInt("Radius"));
            state.intervalTicks = Math.max(20, tag.getInt("IntervalTicks"));
            state.ticksRemaining = Math.max(0, tag.getInt("TicksRemaining"));
            state.lootTable = tag.contains("LootTable") ? tag.getString("LootTable") : CtsConfig.FALLBACK_AIRDROP_LOOT_TABLE;
            return state;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putBoolean("Enabled", enabled);
            tag.putString("Dimension", dimension);
            tag.putInt("CenterX", centerX);
            tag.putInt("CenterZ", centerZ);
            tag.putInt("Radius", radius);
            tag.putInt("IntervalTicks", intervalTicks);
            tag.putInt("TicksRemaining", ticksRemaining);
            tag.putString("LootTable", lootTable == null ? "" : lootTable);
            return tag;
        }
    }
}
