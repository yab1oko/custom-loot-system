package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;

import java.util.function.Supplier;

/** Creative "Refill now". The server rolls; the client only asks. */
public class RefillContainerPacket {
    private final BlockPos pos;

    public RefillContainerPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RefillContainerPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static RefillContainerPacket decode(FriendlyByteBuf buf) {
        return new RefillContainerPacket(buf.readBlockPos());
    }

    public static void handle(RefillContainerPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            BlockEntity entity = player.level().getBlockEntity(packet.pos);
            if (entity instanceof CanStashBlockEntity stash) {
                stash.forceRefill(player);
            } else if (entity instanceof LootPlateBlockEntity plate) {
                plate.forceRefill(player);
            }
        });
        context.setPacketHandled(true);
    }
}
