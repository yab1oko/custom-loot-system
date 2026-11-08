package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;

import java.util.function.Supplier;

public class UpdateItemEntryPacket {
    private final BlockPos pos;
    private final int slot;
    private final int min;
    private final int max;
    private final int chance;
    private final boolean randomDamage;
    private final boolean randomEnchant;
    private final String tag;
    private final String pick;

    public UpdateItemEntryPacket(BlockPos pos, int slot, int min, int max, int chance, boolean randomDamage, boolean randomEnchant, String tag, String pick) {
        this.pos = pos;
        this.slot = slot;
        this.min = min;
        this.max = max;
        this.chance = chance;
        this.randomDamage = randomDamage;
        this.randomEnchant = randomEnchant;
        this.tag = tag;
        this.pick = pick;
    }

    public static void encode(UpdateItemEntryPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.slot);
        buf.writeVarInt(packet.min);
        buf.writeVarInt(packet.max);
        buf.writeVarInt(packet.chance);
        buf.writeBoolean(packet.randomDamage);
        buf.writeBoolean(packet.randomEnchant);
        buf.writeUtf(packet.tag == null ? "" : packet.tag, 256);
        buf.writeUtf(packet.pick == null ? "" : packet.pick, 32767);
    }

    public static UpdateItemEntryPacket decode(FriendlyByteBuf buf) {
        return new UpdateItemEntryPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readUtf(256), buf.readUtf(32767));
    }

    public static void handle(UpdateItemEntryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer player = contextSupplier.get().getSender();
            if (player == null || !player.getAbilities().instabuild) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof LootPlateBlockEntity plate) {
                plate.setEntry(packet.slot, packet.min, packet.max, packet.chance, packet.randomDamage, packet.randomEnchant, packet.tag, packet.pick);
            } else if (player.level().getBlockEntity(packet.pos) instanceof CanStashBlockEntity stash) {
                stash.setEntry(packet.slot, packet.min, packet.max, packet.chance, packet.randomDamage, packet.randomEnchant, packet.tag, packet.pick);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
