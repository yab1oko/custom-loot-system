package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.block.MetalOreBlock;
import cts.yblk.cts.blockentity.MetalOreBlockEntity;

import java.util.function.Supplier;

public class BreakMetalOrePacket {
    private final BlockPos pos;

    public BreakMetalOrePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(BreakMetalOrePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static BreakMetalOrePacket decode(FriendlyByteBuf buf) {
        return new BreakMetalOrePacket(buf.readBlockPos());
    }

    public static void handle(BreakMetalOrePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.getAbilities().instabuild) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (!MetalOreBlock.isMineableTarget(player.level().getBlockState(packet.pos))) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof MetalOreBlockEntity ore) {
                ore.tryMine(player);
            }
        });
        context.setPacketHandled(true);
    }
}
