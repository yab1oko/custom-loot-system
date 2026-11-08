package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.menu.LootBookMenu;

import java.util.function.Supplier;

public class LootBookPagePacket {
    private final int direction;

    public LootBookPagePacket(int direction) {
        this.direction = direction;
    }

    public static void encode(LootBookPagePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.direction);
    }

    public static LootBookPagePacket decode(FriendlyByteBuf buf) {
        return new LootBookPagePacket(buf.readVarInt());
    }

    public static void handle(LootBookPagePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.containerMenu instanceof LootBookMenu menu) {
                menu.movePage(packet.direction);
            }
        });
        context.get().setPacketHandled(true);
    }
}
