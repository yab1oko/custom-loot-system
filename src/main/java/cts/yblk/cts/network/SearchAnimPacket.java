package cts.yblk.cts.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.block.ShelvingBlock;
import cts.yblk.cts.client.SearchAnimation;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

import java.util.function.Supplier;

/**
 * Syncs container-search rummage animation. Client → server (validated) → tracking clients.
 */
public class SearchAnimPacket {
    private final int entityId;
    private final boolean searching;
    private final long posPacked;

    public SearchAnimPacket(int entityId, boolean searching, long posPacked) {
        this.entityId = entityId;
        this.searching = searching;
        this.posPacked = posPacked;
    }

    public static SearchAnimPacket start(Player player, net.minecraft.core.BlockPos pos) {
        return new SearchAnimPacket(player.getId(), true, pos.asLong());
    }

    public static SearchAnimPacket stop(Player player) {
        return new SearchAnimPacket(player.getId(), false, 0L);
    }

    public static void encode(SearchAnimPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeBoolean(packet.searching);
        buf.writeLong(packet.posPacked);
    }

    public static SearchAnimPacket decode(FriendlyByteBuf buf) {
        return new SearchAnimPacket(buf.readVarInt(), buf.readBoolean(), buf.readLong());
    }

    public static void handle(SearchAnimPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                handleServer(packet, sender);
            } else {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleServer(SearchAnimPacket packet, ServerPlayer sender) {
        if (packet.entityId != sender.getId()) {
            return;
        }
        if (packet.searching) {
            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.of(packet.posPacked);
            net.minecraft.core.BlockPos inventory = ShelvingBlock.inventoryPos(sender.level(), pos);
            if (sender.distanceToSqr(Vec3.atCenterOf(inventory)) > 64.0D) {
                return;
            }
            if (!CanStashBlock.isSearchable(sender.level().getBlockState(pos))
                    && !CanStashBlock.isSearchable(sender.level().getBlockState(inventory))) {
                return;
            }
            if (sender.level().getBlockEntity(inventory) instanceof CanStashBlockEntity stash
                    && !stash.allowsSearch(sender)) {
                return;
            }
        }
        ModNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> sender),
                new SearchAnimPacket(sender.getId(), packet.searching, packet.posPacked));
    }

    private static void handleClient(SearchAnimPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(packet.entityId);
        if (!(entity instanceof Player player)) {
            return;
        }
        // Local player already started/stopped from CanStashSearch
        if (player == minecraft.player) {
            return;
        }
        if (packet.searching) {
            SearchAnimation.start(player);
        } else {
            SearchAnimation.stop(player);
        }
    }
}
