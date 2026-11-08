package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.menu.LootBookMenu;

import java.util.function.Supplier;

public class LootBookNamePacket {
    private final String name;

    public LootBookNamePacket(String name) {
        this.name = name;
    }

    public static void encode(LootBookNamePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.name == null ? "" : packet.name, 256);
    }

    public static LootBookNamePacket decode(FriendlyByteBuf buf) {
        return new LootBookNamePacket(buf.readUtf(256));
    }

    public static void handle(LootBookNamePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.containerMenu instanceof LootBookMenu menu) {
                menu.setName(packet.name);
            }
        });
        context.get().setPacketHandled(true);
    }
}
