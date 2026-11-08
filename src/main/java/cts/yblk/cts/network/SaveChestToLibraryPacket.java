package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.library.SavedChestLibrary;

import java.util.function.Supplier;

public class SaveChestToLibraryPacket {
    private final BlockPos pos;
    private final String name;

    public SaveChestToLibraryPacket(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    public static void encode(SaveChestToLibraryPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.name, 64);
    }

    public static SaveChestToLibraryPacket decode(FriendlyByteBuf buf) {
        return new SaveChestToLibraryPacket(buf.readBlockPos(), buf.readUtf(64));
    }

    public static void handle(SaveChestToLibraryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (!(player.level().getBlockEntity(packet.pos) instanceof CanStashBlockEntity entity)) {
                player.displayClientMessage(Component.translatable("gui.cts.chest_library.save_fail"), true);
                return;
            }
            SavedChestLibrary library = SavedChestLibrary.get(player.serverLevel());
            SavedChestLibrary.Entry entry = library.addFrom(entity, packet.name);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncChestLibraryPacket(library.entries()));
            player.displayClientMessage(Component.translatable("gui.cts.chest_library.saved", entry.name()), true);
        });
        context.setPacketHandled(true);
    }
}
