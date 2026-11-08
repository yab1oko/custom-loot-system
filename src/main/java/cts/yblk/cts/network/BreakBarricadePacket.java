package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.block.BarricadeBlock;
import cts.yblk.cts.blockentity.BarricadeBlockEntity;

import java.util.function.Supplier;

public class BreakBarricadePacket {
    private final BlockPos pos;

    public BreakBarricadePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(BreakBarricadePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static BreakBarricadePacket decode(FriendlyByteBuf buf) {
        return new BreakBarricadePacket(buf.readBlockPos());
    }

    public static void handle(BreakBarricadePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.getAbilities().instabuild) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (!BarricadeBlock.isBreakableTarget(player.level().getBlockState(packet.pos))) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof BarricadeBlockEntity barricade) {
                barricade.tryBreak(player);
            }
        });
        context.setPacketHandled(true);
    }
}
