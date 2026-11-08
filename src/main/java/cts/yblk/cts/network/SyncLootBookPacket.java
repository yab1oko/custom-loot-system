package cts.yblk.cts.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.client.LootBookScreen;
import cts.yblk.cts.menu.LootBookMenu;

import java.util.function.Supplier;

public class SyncLootBookPacket {
    private final CompoundTag tag;

    public SyncLootBookPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(SyncLootBookPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.tag);
    }

    public static SyncLootBookPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new SyncLootBookPacket(tag == null ? new CompoundTag() : tag);
    }

    public static void handle(SyncLootBookPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet)));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(SyncLootBookPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.containerMenu instanceof LootBookMenu menu)) {
            return;
        }
        menu.applySync(packet.tag);
        if (Minecraft.getInstance().screen instanceof LootBookScreen screen) {
            screen.onBookSynced();
        }
    }
}
