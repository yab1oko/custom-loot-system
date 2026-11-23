package cts.yblk.cts.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import cts.yblk.cts.block.ShelvingBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.airdrop.AirdropAutoDrop;
import cts.yblk.cts.airdrop.AirdropScheduler;
import cts.yblk.cts.config.CtsConfig;
import cts.yblk.cts.util.LootTableIds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Cts.MODID)
public class AirdropCommand {
    private static final SuggestionProvider<CommandSourceStack> LOOT_TABLES = (ctx, builder) -> {
        List<String> suggestions = new ArrayList<>();
        for (ResourceLocation id : ctx.getSource().getServer().getLootData().getKeys(LootDataType.TABLE)) {
            suggestions.add(id.toString());
            if (Cts.MODID.equals(id.getNamespace())) {
                suggestions.add(id.getPath());
            }
        }
        return SharedSuggestionProvider.suggest(suggestions, builder);
    };

    private AirdropCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cts")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("airdrop")
                        .then(Commands.literal("auto")
                                .then(Commands.literal("clear")
                                        .executes(ctx -> clearAuto(ctx.getSource())))
                                .then(Commands.literal("status")
                                        .executes(ctx -> statusAuto(ctx.getSource())))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100000))
                                        .then(Commands.argument("interval_minutes", IntegerArgumentType.integer(1, 7 * 24 * 60))
                                                .executes(ctx -> enableAuto(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "radius"),
                                                        IntegerArgumentType.getInteger(ctx, "interval_minutes"),
                                                        CtsConfig.defaultAirdropLootTable()))
                                                .then(Commands.argument("loot_table", StringArgumentType.greedyString())
                                                        .suggests(LOOT_TABLES)
                                                        .executes(ctx -> enableAuto(ctx.getSource(),
                                                                IntegerArgumentType.getInteger(ctx, "radius"),
                                                                IntegerArgumentType.getInteger(ctx, "interval_minutes"),
                                                                StringArgumentType.getString(ctx, "loot_table")))))))
                        .then(Commands.argument("x1", IntegerArgumentType.integer())
                                .then(Commands.argument("z1", IntegerArgumentType.integer())
                                        .then(Commands.argument("x2", IntegerArgumentType.integer())
                                                .then(Commands.argument("z2", IntegerArgumentType.integer())
                                                        .executes(ctx -> summon(ctx.getSource(),
                                                                IntegerArgumentType.getInteger(ctx, "x1"),
                                                                IntegerArgumentType.getInteger(ctx, "z1"),
                                                                IntegerArgumentType.getInteger(ctx, "x2"),
                                                                IntegerArgumentType.getInteger(ctx, "z2"),
                                                                CtsConfig.defaultAirdropLootTable()))
                                                        .then(Commands.argument("loot_table", StringArgumentType.greedyString())
                                                                .suggests(LOOT_TABLES)
                                                                .executes(ctx -> summon(ctx.getSource(),
                                                                        IntegerArgumentType.getInteger(ctx, "x1"),
                                                                        IntegerArgumentType.getInteger(ctx, "z1"),
                                                                        IntegerArgumentType.getInteger(ctx, "x2"),
                                                                        IntegerArgumentType.getInteger(ctx, "z2"),
                                                                        StringArgumentType.getString(ctx, "loot_table"))))))))));
        event.getDispatcher().register(Commands.literal("cts")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("inspect")
                        .executes(ctx -> inspect(ctx.getSource())))
                .then(Commands.literal("exportlt")
                        .executes(ctx -> {
                            ctx.getSource().sendFailure(Component.translatable("command.cts.exportlt.usage"));
                            return 0;
                        })
                        .then(Commands.argument("table", StringArgumentType.string())
                                .suggests(ExportLootTableCommand::suggest)
                                .executes(ctx -> ExportLootTableCommand.embed(ctx.getSource(), StringArgumentType.getString(ctx, "table")))))
                .then(Commands.literal("refill")
                        .executes(ctx -> refillOne(ctx.getSource()))
                        .then(Commands.literal("nearby")
                                .executes(ctx -> refillNearby(ctx.getSource(), 16))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> refillNearby(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))))
                        .then(Commands.literal("all")
                                .then(Commands.literal("confirm")
                                        .executes(ctx -> refillAll(ctx.getSource()))))));
    }

    private static int refillOne(CommandSourceStack source) {
        BlockPos pos = lookingAt(source);
        if (pos == null) {
            source.sendFailure(Component.translatable("command.cts.refill.none"));
            return 0;
        }
        int count = refillAt(source, pos);
        if (count == 0) {
            source.sendFailure(Component.translatable("command.cts.refill.none"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.cts.refill.done", count), true);
        return count;
    }

    private static int refillNearby(CommandSourceStack source, int radius) {
        int clamped = Math.max(1, Math.min(64, radius));
        BlockPos center = BlockPos.containing(source.getPosition());
        ServerLevel level = source.getLevel();
        int count = 0;
        Set<BlockPos> done = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-clamped, -clamped, -clamped), center.offset(clamped, clamped, clamped))) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity == null || !done.add(entity.getBlockPos())) {
                continue;
            }
            count += refillEntity(source, entity);
        }
        int refilled = count;
        source.sendSuccess(() -> Component.translatable("command.cts.refill.nearby", refilled, clamped), true);
        return refilled;
    }

    private static int refillAll(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Set<Long> seen = new HashSet<>();
        int count = 0;
        List<BlockPos> anchors = new ArrayList<>();
        anchors.add(level.getSharedSpawnPos());
        for (ServerPlayer player : level.players()) {
            anchors.add(player.blockPosition());
        }
        int view = Math.max(4, level.getServer().getPlayerList().getViewDistance());
        for (BlockPos anchor : anchors) {
            ChunkPos center = new ChunkPos(anchor);
            for (int dx = -view; dx <= view; dx++) {
                for (int dz = -view; dz <= view; dz++) {
                    long key = ChunkPos.asLong(center.x + dx, center.z + dz);
                    if (!seen.add(key)) {
                        continue;
                    }
                    LevelChunk chunk = level.getChunkSource().getChunkNow(center.x + dx, center.z + dz);
                    if (chunk == null) {
                        continue;
                    }
                    for (BlockEntity entity : chunk.getBlockEntities().values()) {
                        count += refillEntity(source, entity);
                    }
                }
            }
        }
        int refilled = count;
        source.sendSuccess(() -> Component.translatable("command.cts.refill.all", refilled), true);
        return refilled;
    }

    private static int refillAt(CommandSourceStack source, BlockPos pos) {
        BlockEntity entity = source.getLevel().getBlockEntity(pos);
        if (entity instanceof CanStashBlockEntity stash) {
            BlockPos inventory = ShelvingBlock.inventoryPos(source.getLevel(), pos);
            if (!inventory.equals(pos) && source.getLevel().getBlockEntity(inventory) instanceof CanStashBlockEntity master) {
                return refillEntity(source, master);
            }
            return refillEntity(source, stash);
        }
        return refillEntity(source, entity);
    }

    private static int refillEntity(CommandSourceStack source, BlockEntity entity) {
        if (entity instanceof CanStashBlockEntity stash) {
            if (stash.isAirdropArmed()) {
                return 0;
            }
            Entity actor = source.getEntity();
            stash.forceRefill(actor instanceof net.minecraft.world.entity.player.Player player ? player : null);
            return 1;
        }
        if (entity instanceof LootPlateBlockEntity plate) {
            Entity actor = source.getEntity();
            plate.forceRefill(actor instanceof net.minecraft.world.entity.player.Player player ? player : null);
            return 1;
        }
        return 0;
    }

    private static int inspect(CommandSourceStack source) {
        BlockPos pos = lookingAt(source);
        if (pos == null || !(source.getLevel().getBlockEntity(pos) instanceof CanStashBlockEntity stash)) {
            if (pos != null && source.getLevel().getBlockEntity(pos) instanceof LootPlateBlockEntity plate) {
                source.sendSuccess(() -> Component.translatable("command.cts.inspect.plate",
                        plate.getMode().name(),
                        plate.getLootTableId().isBlank() ? "-" : plate.getLootTableId(),
                        plate.getRespawnSeconds(),
                        plate.isPerPlayerLoot()), false);
                return 1;
            }
            source.sendFailure(Component.translatable("command.cts.refill.none"));
            return 0;
        }
        BlockPos inventory = ShelvingBlock.inventoryPos(source.getLevel(), pos);
        if (source.getLevel().getBlockEntity(inventory) instanceof CanStashBlockEntity master) {
            stash = master;
        }
        CanStashBlockEntity found = stash;
        int cooldownSeconds = Math.max(0, (found.getCooldownTicks() + 19) / 20);
        source.sendSuccess(() -> Component.translatable("command.cts.inspect.stash",
                found.getMode().name(),
                found.getMode() == CanStashBlockEntity.Mode.LOOT_TABLE
                        ? (found.getLootTableId().isBlank() ? "-" : found.getLootTableId())
                        : Component.translatable("gui.cts.mode.items").getString(),
                found.getRespawnSeconds(),
                found.getSearchSeconds(),
                cooldownSeconds,
                found.isPerPlayerLoot(),
                found.getRefillMode().name(),
                found.requiresTool()), false);
        return 1;
    }

    private static BlockPos lookingAt(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) {
            return null;
        }
        Vec3 eye = living.getEyePosition();
        Vec3 end = eye.add(living.getLookAngle().scale(8.0D));
        BlockHitResult hit = living.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, living));
        if (hit.getType() != HitResult.Type.BLOCK) {
            BlockPos feet = living.blockPosition().below();
            BlockEntity under = living.level().getBlockEntity(feet);
            return under instanceof CanStashBlockEntity || under instanceof LootPlateBlockEntity ? feet : null;
        }
        BlockPos pos = hit.getBlockPos();
        BlockPos inventory = ShelvingBlock.inventoryPos(living.level(), pos);
        if (living.level().getBlockEntity(inventory) instanceof CanStashBlockEntity) {
            return inventory;
        }
        return pos;
    }

    private static int summon(CommandSourceStack source, int x1, int z1, int x2, int z2, String lootTable) {
        ServerLevel level = source.getLevel();
        String table = LootTableIds.normalize(lootTable);
        if (table.isEmpty()) {
            source.sendFailure(Component.translatable("command.cts.airdrop.bad_table", lootTable));
            return 0;
        }
        source.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("command.cts.airdrop.warn"), false);
        AirdropScheduler.schedule(level, x1, z1, x2, z2, table, AirdropScheduler.WARNING_DELAY_TICKS);
        source.sendSuccess(() -> Component.translatable("command.cts.airdrop.scheduled", x1, z1, x2, z2), true);
        return 1;
    }

    private static int enableAuto(CommandSourceStack source, int radius, int intervalMinutes, String lootTable) {
        String table = LootTableIds.normalize(lootTable);
        if (table.isEmpty()) {
            source.sendFailure(Component.translatable("command.cts.airdrop.bad_table", lootTable));
            return 0;
        }
        ServerLevel level = source.getLevel();
        int cx = (int) Math.floor(source.getPosition().x);
        int cz = (int) Math.floor(source.getPosition().z);
        AirdropAutoDrop.enable(level, cx, cz, radius, intervalMinutes, table);
        source.sendSuccess(() -> Component.translatable(
                "command.cts.airdrop.auto.enabled", radius, intervalMinutes, cx, cz, table), true);
        return 1;
    }

    private static int clearAuto(CommandSourceStack source) {
        AirdropAutoDrop.clear(source.getLevel());
        source.sendSuccess(() -> Component.translatable("command.cts.airdrop.auto.cleared"), true);
        return 1;
    }

    private static int statusAuto(CommandSourceStack source) {
        AirdropAutoDrop.State state = AirdropAutoDrop.get(source.getLevel());
        if (!state.isEnabled()) {
            source.sendSuccess(() -> Component.translatable("command.cts.airdrop.auto.disabled"), false);
            return 1;
        }
        int minutesLeft = Math.max(0, (state.getTicksRemaining() + 19) / (20 * 60));
        int intervalMin = Math.max(1, state.getIntervalTicks() / (20 * 60));
        source.sendSuccess(() -> Component.translatable(
                "command.cts.airdrop.auto.status",
                state.getRadius(),
                intervalMin,
                minutesLeft,
                state.getCenterX(),
                state.getCenterZ(),
                state.getLootTable()), false);
        return 1;
    }
}
