package cts.yblk.cts.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.airdrop.AirdropAutoDrop;
import cts.yblk.cts.airdrop.AirdropScheduler;
import cts.yblk.cts.config.CtsConfig;
import cts.yblk.cts.util.LootTableIds;

import java.util.ArrayList;
import java.util.List;

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
