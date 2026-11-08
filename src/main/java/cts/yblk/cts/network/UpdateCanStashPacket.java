package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

import java.util.function.Supplier;

public class UpdateCanStashPacket {
    private final BlockPos pos;
    private final CanStashBlockEntity.Mode mode;
    private final String lootTable;
    private final int respawnSeconds;
    private final int searchSeconds;
    private final boolean requiresTool;

    public UpdateCanStashPacket(BlockPos pos, CanStashBlockEntity.Mode mode, String lootTable, int respawnSeconds, int searchSeconds, boolean requiresTool) {
        this.pos = pos;
        this.mode = mode;
        this.lootTable = lootTable;
        this.respawnSeconds = respawnSeconds;
        this.searchSeconds = searchSeconds;
        this.requiresTool = requiresTool;
    }

    public static void encode(UpdateCanStashPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.mode);
        buf.writeUtf(packet.lootTable, 256);
        buf.writeVarInt(packet.respawnSeconds);
        buf.writeVarInt(packet.searchSeconds);
        buf.writeBoolean(packet.requiresTool);
    }

    public static UpdateCanStashPacket decode(FriendlyByteBuf buf) {
        return new UpdateCanStashPacket(buf.readBlockPos(), buf.readEnum(CanStashBlockEntity.Mode.class), buf.readUtf(256), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(UpdateCanStashPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !CanStashBlockEntity.canConfigure(player)) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof CanStashBlockEntity stash) {
                stash.applySettings(packet.mode, packet.lootTable, packet.respawnSeconds, packet.searchSeconds, packet.requiresTool);
            }
        });
        context.setPacketHandled(true);
    }
}
