package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;
import cts.yblk.cts.menu.CanStashConfigMenu;
import cts.yblk.cts.menu.LootPlateMenu;

import java.util.function.Supplier;

/** Puts a JEI ingredient into a template slot without taking it from the player. */
public class PlaceGhostItemPacket {
    private final BlockPos pos;
    private final int slot;
    private final ItemStack stack;
    private final boolean plate;

    public PlaceGhostItemPacket(BlockPos pos, int slot, ItemStack stack, boolean plate) {
        this.pos = pos;
        this.slot = slot;
        this.stack = stack;
        this.plate = plate;
    }

    public static void encode(PlaceGhostItemPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.slot);
        buf.writeItem(packet.stack);
        buf.writeBoolean(packet.plate);
    }

    public static PlaceGhostItemPacket decode(FriendlyByteBuf buf) {
        return new PlaceGhostItemPacket(buf.readBlockPos(), buf.readVarInt(), buf.readItem(), buf.readBoolean());
    }

    public static void handle(PlaceGhostItemPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || packet.slot < 0) {
                return;
            }
            ItemStack placed = packet.stack.copy();
            if (packet.plate) {
                if (player.containerMenu instanceof LootPlateMenu menu
                        && menu.getPlate().getBlockPos().equals(packet.pos)
                        && menu.getPlate().getMode() == LootPlateBlockEntity.Mode.ITEMS) {
                    menu.getPlate().placeTemplate(packet.slot, placed);
                }
            } else if (player.containerMenu instanceof CanStashConfigMenu menu
                    && menu.getStash().getBlockPos().equals(packet.pos)
                    && menu.getStash().getMode() == CanStashBlockEntity.Mode.ITEMS) {
                menu.getStash().placeTemplate(packet.slot, placed);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
