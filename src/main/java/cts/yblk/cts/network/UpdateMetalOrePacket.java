package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.MetalOreBlockEntity;

import java.util.function.Supplier;

public class UpdateMetalOrePacket {
    private final BlockPos pos;
    private final int respawnSeconds;

    public UpdateMetalOrePacket(BlockPos pos, int respawnSeconds) {
        this.pos = pos;
        this.respawnSeconds = respawnSeconds;
    }

    public static void encode(UpdateMetalOrePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.respawnSeconds);
    }

    public static UpdateMetalOrePacket decode(FriendlyByteBuf buf) {
        return new UpdateMetalOrePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(UpdateMetalOrePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !MetalOreBlockEntity.canConfigure(player)) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof MetalOreBlockEntity ore) {
                ore.applySettings(packet.respawnSeconds);
            }
        });
        context.setPacketHandled(true);
    }
}
