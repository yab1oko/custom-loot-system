package cts.yblk.cts.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.block.ConvertedContainerBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
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
        ResourceLocation lootTable = lootTableOf(old);
        ItemStack[] preserved = lootTable == null ? snapshotItems(old) : null;

        Direction facing = ConvertedContainerBlock.facingFrom(state);
        BlockState converted = ModBlocks.CONVERTED_CONTAINER.get().defaultBlockState()
                .setValue(CanStashBlock.FACING, facing)
                .setValue(CanStashBlock.LOOTED, false);
        level.setBlock(pos, converted, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(pos) instanceof ConvertedContainerBlockEntity entity)) {
            return;
        }
        entity.setMimic(state);

        boolean openConfig = false;
        if (lootTable != null) {
            entity.applySettings(CanStashBlockEntity.Mode.LOOT_TABLE, lootTable.toString(),
                    entity.getRespawnSeconds(), entity.getSearchSeconds(), false);
        } else if (preserved != null && hasAny(preserved)) {
            entity.setMode(CanStashBlockEntity.Mode.ITEMS);
            int limit = Math.min(preserved.length, CanStashBlockEntity.SLOT_COUNT);
            for (int i = 0; i < limit; i++) {
                ItemStack stack = preserved[i];
                if (stack != null && !stack.isEmpty()) {
                    entity.placeTemplate(i, stack);
                }
            }
            openConfig = true;
        }

        player.displayClientMessage(Component.translatable("gui.cts.convert_done"), true);
        if (openConfig) {
            NetworkHooks.openScreen(player, entity, pos);
        }
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

    private static ResourceLocation lootTableOf(BlockEntity entity) {
        if (!(entity instanceof RandomizableContainerBlockEntity)) {
            return null;
        }
        CompoundTag tag = entity.saveWithoutMetadata();
        if (tag.contains("LootTable", Tag.TAG_STRING)) {
            return ResourceLocation.tryParse(tag.getString("LootTable"));
        }
        return null;
    }

    private static ItemStack[] snapshotItems(BlockEntity entity) {
        if (!(entity instanceof Container container)) {
            return null;
        }
        int size = Math.min(container.getContainerSize(), CanStashBlockEntity.SLOT_COUNT);
        ItemStack[] stacks = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            ItemStack stack = container.getItem(i);
            stacks[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        }
        return stacks;
    }

    private static boolean hasAny(ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
