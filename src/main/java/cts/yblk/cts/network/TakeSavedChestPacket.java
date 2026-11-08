package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.library.SavedChestLibrary;

import java.util.UUID;
import java.util.function.Supplier;

public class TakeSavedChestPacket {
    private final UUID id;

    public TakeSavedChestPacket(UUID id) {
        this.id = id;
    }

    public static void encode(TakeSavedChestPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
    }

    public static TakeSavedChestPacket decode(FriendlyByteBuf buf) {
        return new TakeSavedChestPacket(buf.readUUID());
    }

    public static void handle(TakeSavedChestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) {
                return;
            }
            SavedChestLibrary library = SavedChestLibrary.get(player.serverLevel());
            library.find(packet.id).ifPresent(entry -> {
                ItemStack stack = library.createStack(entry);
                if (!player.addItem(stack)) {
                    player.drop(stack, false);
                }
                player.displayClientMessage(Component.translatable("gui.cts.chest_library.taken", entry.name()), true);
            });
        });
        context.setPacketHandled(true);
    }
}
