package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class RequestLootTablesPacket {
    public static void encode(RequestLootTablesPacket packet, FriendlyByteBuf buf) {
    }

    public static RequestLootTablesPacket decode(FriendlyByteBuf buf) {
        return new RequestLootTablesPacket();
    }

    public static void handle(RequestLootTablesPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || player.getServer() == null) {
                return;
            }
            List<String> ids = new ArrayList<>();
            for (ResourceLocation id : player.getServer().getLootData().getKeys(LootDataType.TABLE)) {
                if (!BuiltInLootTables.EMPTY.equals(id)) {
                    ids.add(id.toString());
                }
            }
            ids.sort(Comparator.naturalOrder());
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LootTableListPacket(ids));
        });
        context.get().setPacketHandled(true);
    }
}
