package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.EntityPlateBlockEntity;
import cts.yblk.cts.blockentity.SpawnChance;

import java.util.function.Supplier;

public class UpdateEntityPlatePacket {
    private final BlockPos pos;
    private final String[] entityIds;
    private final int[] chances;
    private final boolean randomize;
    private final int count;
    private final int respawnSeconds;
    private final int radius;

    public UpdateEntityPlatePacket(BlockPos pos, String[] entityIds, int[] chances, boolean randomize, int count, int respawnSeconds, int radius) {
        this.pos = pos;
        this.entityIds = entityIds;
        this.chances = chances;
        this.randomize = randomize;
        this.count = count;
        this.respawnSeconds = respawnSeconds;
        this.radius = radius;
    }

    public static void encode(UpdateEntityPlatePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.randomize);
        buf.writeVarInt(packet.count);
        buf.writeVarInt(packet.respawnSeconds);
        buf.writeVarInt(packet.radius);
        for (int i = 0; i < EntityPlateBlockEntity.ID_COUNT; i++) {
            String id = i < packet.entityIds.length && packet.entityIds[i] != null ? packet.entityIds[i] : "";
            buf.writeUtf(id, 128);
            int chance = packet.chances != null && i < packet.chances.length ? packet.chances[i] : SpawnChance.DEFAULT;
            buf.writeVarInt(chance);
        }
    }

    public static UpdateEntityPlatePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean randomize = buf.readBoolean();
        int count = buf.readVarInt();
        int seconds = buf.readVarInt();
        int radius = buf.readVarInt();
        String[] ids = new String[EntityPlateBlockEntity.ID_COUNT];
        int[] chances = new int[EntityPlateBlockEntity.ID_COUNT];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = buf.readUtf(128);
            chances[i] = buf.readVarInt();
        }
        return new UpdateEntityPlatePacket(pos, ids, chances, randomize, count, seconds, radius);
    }

    public static void handle(UpdateEntityPlatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !EntityPlateBlockEntity.canConfigure(player)) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof EntityPlateBlockEntity plate) {
                plate.applySettings(packet.entityIds, packet.chances, packet.randomize, packet.count, packet.respawnSeconds, packet.radius);
            }
        });
        context.setPacketHandled(true);
    }
}
