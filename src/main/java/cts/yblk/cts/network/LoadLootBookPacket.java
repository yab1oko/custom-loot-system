package cts.yblk.cts.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import cts.yblk.cts.item.LootBookData;
import cts.yblk.cts.item.LootTableImporter;
import cts.yblk.cts.menu.LootBookMenu;

import java.util.Optional;
import java.util.function.Supplier;

public class LoadLootBookPacket {
    private final String id;

    public LoadLootBookPacket(String id) {
        this.id = id;
    }

    public static void encode(LoadLootBookPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.id == null ? "" : packet.id, 256);
    }

    public static LoadLootBookPacket decode(FriendlyByteBuf buf) {
        return new LoadLootBookPacket(buf.readUtf(256));
    }

    public static void handle(LoadLootBookPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || player.getServer() == null || !(player.containerMenu instanceof LootBookMenu menu)) {
                return;
            }
            if (!player.getAbilities().instabuild) {
                return;
            }
            Optional<LootBookData> loaded = LootTableImporter.load(player.getServer(), packet.id);
            if (loaded.isEmpty()) {
                player.displayClientMessage(Component.translatable("gui.cts.load_fail", packet.id), false);
                return;
            }
            menu.replaceBook(loaded.get());
            CompoundTag tag = new CompoundTag();
            menu.book().write(tag);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncLootBookPacket(tag));
            player.displayClientMessage(Component.translatable("gui.cts.load_done", menu.book().name()), false);
        });
        context.get().setPacketHandled(true);
    }
}
