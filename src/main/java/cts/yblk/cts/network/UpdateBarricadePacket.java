package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.BarricadeBlockEntity;

import java.util.function.Supplier;

public class UpdateBarricadePacket {
    private final BlockPos pos;
    private final int respawnSeconds;

    public UpdateBarricadePacket(BlockPos pos, int respawnSeconds) {
        this.pos = pos;
        this.respawnSeconds = respawnSeconds;
    }

    public static void encode(UpdateBarricadePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.respawnSeconds);
    }

    public static UpdateBarricadePacket decode(FriendlyByteBuf buf) {
        return new UpdateBarricadePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(UpdateBarricadePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !BarricadeBlockEntity.canConfigure(player)) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof BarricadeBlockEntity barricade) {
                barricade.applySettings(packet.respawnSeconds);
            }
        });
        context.setPacketHandled(true);
    }
}
