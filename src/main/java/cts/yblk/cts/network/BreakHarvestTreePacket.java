package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.block.HarvestTreeStructure;
import cts.yblk.cts.blockentity.HarvestTreeBlockEntity;

import java.util.function.Supplier;

public class BreakHarvestTreePacket {
    private final BlockPos pos;

    public BreakHarvestTreePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(BreakHarvestTreePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static BreakHarvestTreePacket decode(FriendlyByteBuf buf) {
        return new BreakHarvestTreePacket(buf.readBlockPos());
    }

    public static void handle(BreakHarvestTreePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.getAbilities().instabuild) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (!HarvestTreeStructure.isChopTarget(player.level(), packet.pos)) {
                return;
            }
            BlockPos master = HarvestTreeStructure.findMaster(player.level(), packet.pos);
            if (master != null && player.level().getBlockEntity(master) instanceof HarvestTreeBlockEntity tree) {
                tree.tryChop(player);
            }
        });
        context.setPacketHandled(true);
    }
}
