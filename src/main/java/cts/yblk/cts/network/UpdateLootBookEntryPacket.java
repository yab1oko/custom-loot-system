package cts.yblk.cts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.menu.LootBookMenu;

import java.util.function.Supplier;

public class UpdateLootBookEntryPacket {
    private final int slot;
    private final int min;
    private final int max;
    private final int chance;
    private final boolean randomDamage;
    private final boolean randomEnchant;
    private final String tag;
    private final String pick;

    public UpdateLootBookEntryPacket(int slot, int min, int max, int chance, boolean randomDamage, boolean randomEnchant, String tag, String pick) {
        this.slot = slot;
        this.min = min;
        this.max = max;
        this.chance = chance;
        this.randomDamage = randomDamage;
        this.randomEnchant = randomEnchant;
        this.tag = tag;
        this.pick = pick;
    }

    public static void encode(UpdateLootBookEntryPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.slot);
        buf.writeVarInt(packet.min);
        buf.writeVarInt(packet.max);
        buf.writeVarInt(packet.chance);
        buf.writeBoolean(packet.randomDamage);
        buf.writeBoolean(packet.randomEnchant);
        buf.writeUtf(packet.tag == null ? "" : packet.tag, 256);
        buf.writeUtf(packet.pick == null ? "" : packet.pick, 32767);
    }

    public static UpdateLootBookEntryPacket decode(FriendlyByteBuf buf) {
        return new UpdateLootBookEntryPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readUtf(256), buf.readUtf(32767));
    }

    public static void handle(UpdateLootBookEntryPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.containerMenu instanceof LootBookMenu menu) {
                menu.setEntry(packet.slot, packet.min, packet.max, packet.chance, packet.randomDamage, packet.randomEnchant, packet.tag, packet.pick);
            }
        });
        context.get().setPacketHandled(true);
    }
}
