package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.Cts;
import cts.yblk.cts.item.LootTableFiles;
import cts.yblk.cts.menu.LootBookMenu;

import java.nio.file.Path;
import java.util.function.Supplier;

public class ExportLootBookPacket {
    private final String name;

    public ExportLootBookPacket(String name) {
        this.name = name;
    }

    public static void encode(ExportLootBookPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.name == null ? "" : packet.name, 256);
    }

    public static ExportLootBookPacket decode(FriendlyByteBuf buf) {
        return new ExportLootBookPacket(buf.readUtf(256));
    }

    public static void handle(ExportLootBookPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || !(player.containerMenu instanceof LootBookMenu menu)) {
                return;
            }
            menu.setName(packet.name);
            try {
                Path file = LootTableFiles.write(menu.book());
                player.displayClientMessage(Component.translatable("gui.cts.export_done", menu.book().name()), false);
                Cts.LOGGER.info("Exported loot table to {}", file.toAbsolutePath());
            } catch (Exception exception) {
                Cts.LOGGER.error("Failed to export loot table", exception);
                player.displayClientMessage(Component.translatable("gui.cts.export_fail"), false);
            }
        });
        context.get().setPacketHandled(true);
    }
}
