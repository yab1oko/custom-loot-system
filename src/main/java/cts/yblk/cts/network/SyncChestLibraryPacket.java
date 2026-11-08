package cts.yblk.cts.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.client.ChestLibraryClient;
import cts.yblk.cts.library.SavedChestLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncChestLibraryPacket {
    private final List<SavedChestLibrary.Entry> entries;

    public SyncChestLibraryPacket(List<SavedChestLibrary.Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static void encode(SyncChestLibraryPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entries.size());
        for (SavedChestLibrary.Entry entry : packet.entries) {
            buf.writeNbt(entry.writeSync());
        }
    }

    public static SyncChestLibraryPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<SavedChestLibrary.Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag != null) {
                entries.add(SavedChestLibrary.Entry.readSync(tag));
            }
        }
        return new SyncChestLibraryPacket(entries);
    }

    public static void handle(SyncChestLibraryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ChestLibraryClient.applySync(packet.entries)));
        context.setPacketHandled(true);
    }
}
