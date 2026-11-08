package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.block.ConvertedContainerBlock;
import cts.yblk.cts.blockentity.ConvertedContainerBlockEntity;
import cts.yblk.cts.item.ConversionWandItem;
import cts.yblk.cts.registry.ModBlocks;

import java.util.function.Supplier;

public class ConvertContainerPacket {
    private final BlockPos pos;
    private final boolean revert;

    public ConvertContainerPacket(BlockPos pos, boolean revert) {
        this.pos = pos;
        this.revert = revert;
    }

    public static void encode(ConvertContainerPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.revert);
    }

    public static ConvertContainerPacket decode(FriendlyByteBuf buf) {
        return new ConvertContainerPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(ConvertContainerPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) {
                return;
            }
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > 64.0D) {
                return;
            }
            Level level = player.level();
            BlockState state = level.getBlockState(packet.pos);
            if (packet.revert) {
                revert(level, packet.pos, state, player);
            } else {
                convert(level, packet.pos, state, player);
            }
        });
        context.setPacketHandled(true);
    }

    private static void convert(Level level, BlockPos pos, BlockState state, ServerPlayer player) {
        if (!ConversionWandItem.canConvert(level, pos, state)) {
            player.displayClientMessage(Component.translatable("gui.cts.convert_invalid"), true);
            return;
        }
        BlockEntity old = level.getBlockEntity(pos);
        dropContainerContents(level, pos, old);
        Direction facing = ConvertedContainerBlock.facingFrom(state);
        BlockState converted = ModBlocks.CONVERTED_CONTAINER.get().defaultBlockState()
                .setValue(CanStashBlock.FACING, facing)
                .setValue(CanStashBlock.LOOTED, false);
        level.setBlock(pos, converted, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ConvertedContainerBlockEntity entity) {
            entity.setMimic(state);
        }
        player.displayClientMessage(Component.translatable("gui.cts.convert_done"), true);
    }

    private static void revert(Level level, BlockPos pos, BlockState state, ServerPlayer player) {
        if (!(level.getBlockEntity(pos) instanceof ConvertedContainerBlockEntity entity)) {
            player.displayClientMessage(Component.translatable("gui.cts.convert_invalid"), true);
            return;
        }
        BlockState mimic = entity.getMimic();
        level.setBlock(pos, mimic, Block.UPDATE_ALL);
        player.displayClientMessage(Component.translatable("gui.cts.convert_reverted"), true);
    }

    private static void dropContainerContents(Level level, BlockPos pos, BlockEntity entity) {
        if (!(entity instanceof Container container)) {
            return;
        }
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                double x = pos.getX() + 0.5D;
                double y = pos.getY() + 0.5D;
                double z = pos.getZ() + 0.5D;
                ItemEntity item = new ItemEntity(level, x, y, z, stack.copy());
                item.setDefaultPickUpDelay();
                level.addFreshEntity(item);
                container.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
