package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import cts.yblk.cts.library.SavedChestLibrary;

import java.util.function.Supplier;

public class RequestChestLibraryPacket {
    public RequestChestLibraryPacket() {
    }

    public static void encode(RequestChestLibraryPacket packet, FriendlyByteBuf buf) {
    }

    public static RequestChestLibraryPacket decode(FriendlyByteBuf buf) {
        return new RequestChestLibraryPacket();
    }

    public static void handle(RequestChestLibraryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) {
                return;
            }
            SavedChestLibrary library = SavedChestLibrary.get(player.serverLevel());
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncChestLibraryPacket(library.entries()));
        });
        context.setPacketHandled(true);
    }
}
