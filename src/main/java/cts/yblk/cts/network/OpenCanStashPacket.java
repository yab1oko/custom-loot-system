package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.block.ShelvingBlock;

import java.util.function.Supplier;

public class OpenCanStashPacket {
    private final BlockPos pos;

    public OpenCanStashPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(OpenCanStashPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static OpenCanStashPacket decode(FriendlyByteBuf buf) {
        return new OpenCanStashPacket(buf.readBlockPos());
    }

    public static void handle(OpenCanStashPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.getAbilities().instabuild) {
                return;
            }
            BlockPos inventory = ShelvingBlock.inventoryPos(player.level(), packet.pos);
            if (player.distanceToSqr(Vec3.atCenterOf(inventory)) > 64.0D) {
                return;
            }
            if (!CanStashBlock.isSearchable(player.level().getBlockState(packet.pos))
                    && !CanStashBlock.isSearchable(player.level().getBlockState(inventory))) {
                return;
            }
            if (player.level().getBlockEntity(inventory) instanceof CanStashBlockEntity stash) {
                stash.openChest(player);
            }
        });
        context.setPacketHandled(true);
    }
}
