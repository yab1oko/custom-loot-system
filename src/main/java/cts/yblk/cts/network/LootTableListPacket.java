package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.item.LootTableIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LootTableListPacket {
    private final List<String> ids;

    public LootTableListPacket(List<String> ids) {
        this.ids = ids;
    }

    public static void encode(LootTableListPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.ids.size());
        for (String id : packet.ids) {
            buf.writeUtf(id);
        }
    }

    public static LootTableListPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<String> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readUtf());
        }
        return new LootTableListPacket(ids);
    }

    public static void handle(LootTableListPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> LootTableIndex.replace(packet.ids));
        context.get().setPacketHandled(true);
    }
}
