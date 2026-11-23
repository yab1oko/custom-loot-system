package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;

import java.util.function.Supplier;

public class UpdateLootPlatePacket {
    private final BlockPos pos;
    private final LootPlateBlockEntity.Mode mode;
    private final String lootTable;
    private final int respawnSeconds;
    private final boolean perPlayer;

    public UpdateLootPlatePacket(BlockPos pos, LootPlateBlockEntity.Mode mode, String lootTable, int respawnSeconds, boolean perPlayer) {
        this.pos = pos;
        this.mode = mode;
        this.lootTable = lootTable;
        this.respawnSeconds = respawnSeconds;
        this.perPlayer = perPlayer;
    }

    public static void encode(UpdateLootPlatePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.mode);
        buf.writeUtf(packet.lootTable, 256);
        buf.writeVarInt(packet.respawnSeconds);
        buf.writeBoolean(packet.perPlayer);
    }

    public static UpdateLootPlatePacket decode(FriendlyByteBuf buf) {
        return new UpdateLootPlatePacket(buf.readBlockPos(), buf.readEnum(LootPlateBlockEntity.Mode.class), buf.readUtf(256), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(UpdateLootPlatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !LootPlateBlockEntity.canConfigure(player)) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof LootPlateBlockEntity plate) {
                plate.applySettings(packet.mode, packet.lootTable, packet.respawnSeconds, packet.perPlayer);
            }
        });
        context.setPacketHandled(true);
    }
}
