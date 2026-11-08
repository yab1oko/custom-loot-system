package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import cts.yblk.cts.library.SavedChestLibrary;

import java.util.UUID;
import java.util.function.Supplier;

public class DeleteSavedChestPacket {
    private final UUID id;

    public DeleteSavedChestPacket(UUID id) {
        this.id = id;
    }

    public static void encode(DeleteSavedChestPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
    }

    public static DeleteSavedChestPacket decode(FriendlyByteBuf buf) {
        return new DeleteSavedChestPacket(buf.readUUID());
    }

    public static void handle(DeleteSavedChestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) {
                return;
            }
            SavedChestLibrary library = SavedChestLibrary.get(player.serverLevel());
            if (library.delete(packet.id)) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncChestLibraryPacket(library.entries()));
            }
        });
        context.setPacketHandled(true);
    }
}
