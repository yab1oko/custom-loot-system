package cts.yblk.cts.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.item.LootTableFiles;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Cts.MODID)
public class ExportLootTableCommand {
    private static final String PACK_ID = "file/" + Cts.MODID;

    private ExportLootTableCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("exportlt")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    context.getSource().sendFailure(Component.translatable("command.cts.exportlt.usage"));
                    return 0;
                })
                .then(Commands.argument("table", StringArgumentType.string())
                        .suggests(ExportLootTableCommand::suggest)
                        .executes(context -> embed(context.getSource(), StringArgumentType.getString(context, "table")))));
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        if (remaining.isEmpty() || "all".startsWith(remaining)) {
            builder.suggest("all");
        }
        for (String id : LootTableFiles.listNames()) {
            String path = id.startsWith(Cts.MODID + ":") ? id.substring(Cts.MODID.length() + 1) : id;
            if (remaining.isEmpty() || id.contains(remaining) || path.toLowerCase(Locale.ROOT).contains(remaining)) {
                builder.suggest(path.contains(":") ? id : path);
            }
        }
        return builder.buildFuture();
    }

    private static int embed(CommandSourceStack source, String table) {
        MinecraftServer server = source.getServer();
        try {
            Path pack = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(Cts.MODID);
            Path tables = pack.resolve("data").resolve(Cts.MODID).resolve("loot_tables");
            boolean all = table.equalsIgnoreCase("all");
            int copied;
            String embeddedName = table;
            if (all) {
                copied = LootTableFiles.copyInto(tables);
                if (copied == 0) {
                    source.sendFailure(Component.translatable("command.cts.exportlt.empty"));
                    return 0;
                }
            } else {
                embeddedName = LootTableFiles.sanitizeId(table);
                if (embeddedName.isEmpty() || !LootTableFiles.copyOne(tables, embeddedName)) {
                    source.sendFailure(Component.translatable("command.cts.exportlt.missing", table));
                    return 0;
                }
                copied = 1;
            }
            Files.writeString(pack.resolve("pack.mcmeta"), """
                    {
                      "pack": {
                        "pack_format": 15,
                        "description": "Custom Loot System loot tables"
                      }
                    }
                    """, StandardCharsets.UTF_8);
            if (all) {
                source.sendSuccess(() -> Component.translatable("command.cts.exportlt.done", copied), true);
            } else {
                String name = embeddedName;
                source.sendSuccess(() -> Component.translatable("command.cts.exportlt.one", name), true);
            }
            reloadDatapacks(source);
            return Command.SINGLE_SUCCESS;
        } catch (Exception exception) {
            Cts.LOGGER.error("Failed to embed loot tables", exception);
            source.sendFailure(Component.translatable("command.cts.exportlt.fail"));
            return 0;
        }
    }

    /** Same flow as vanilla /reload, and always enables the CTS datapack. */
    private static void reloadDatapacks(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        PackRepository repository = server.getPackRepository();
        repository.reload();
        List<String> selected = new ArrayList<>(repository.getSelectedIds());
        Collection<String> disabled = server.getWorldData().getDataConfiguration().dataPacks().getDisabled();
        for (String id : repository.getAvailableIds()) {
            if (!disabled.contains(id) && !selected.contains(id)) {
                selected.add(id);
            }
        }
        if (repository.getPack(PACK_ID) != null && !selected.contains(PACK_ID)) {
            selected.add(PACK_ID);
        }
        source.sendSuccess(() -> Component.translatable("commands.reload.success"), true);
        ReloadCommand.reloadPacks(selected, source);
    }

}
