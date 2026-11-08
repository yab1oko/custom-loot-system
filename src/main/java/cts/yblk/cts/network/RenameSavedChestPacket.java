package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import cts.yblk.cts.library.SavedChestLibrary;

import java.util.UUID;
import java.util.function.Supplier;

public class RenameSavedChestPacket {
    private final UUID id;
    private final String name;

    public RenameSavedChestPacket(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public static void encode(RenameSavedChestPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
        buf.writeUtf(packet.name, 64);
    }

    public static RenameSavedChestPacket decode(FriendlyByteBuf buf) {
        return new RenameSavedChestPacket(buf.readUUID(), buf.readUtf(64));
    }

    public static void handle(RenameSavedChestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) {
                return;
            }
            SavedChestLibrary library = SavedChestLibrary.get(player.serverLevel());
            if (library.rename(packet.id, packet.name)) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncChestLibraryPacket(library.entries()));
            }
        });
        context.setPacketHandled(true);
    }
}
