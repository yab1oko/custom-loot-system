package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.block.VentilationGrilleBlock;
import cts.yblk.cts.blockentity.VentilationGrilleBlockEntity;

import java.util.function.Supplier;

public class BreakVentilationGrillePacket {
    private final BlockPos pos;

    public BreakVentilationGrillePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(BreakVentilationGrillePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static BreakVentilationGrillePacket decode(FriendlyByteBuf buf) {
        return new BreakVentilationGrillePacket(buf.readBlockPos());
    }

    public static void handle(BreakVentilationGrillePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.getAbilities().instabuild) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (!VentilationGrilleBlock.isOpenableTarget(player.level().getBlockState(packet.pos))) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof VentilationGrilleBlockEntity grille) {
                grille.tryOpen(player);
            }
        });
        context.setPacketHandled(true);
    }
}
