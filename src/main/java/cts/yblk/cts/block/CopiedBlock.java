package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class CopiedBlock {
    private CopiedBlock() {
    }

    public static ItemStack item(Block block, BlockState state, BlockGetter level, BlockPos pos) {
        ItemStack stack = new ItemStack(block);
        CompoundTag stateTag = new CompoundTag();
        for (Property<?> property : state.getProperties()) {
            if (property == CanStashBlock.FACING || property == CanStashBlock.LOOTED
                    || property == ShelvingBlock.VARIANT || property == CorpseBlock.VARIANT) {
                continue;
            }
            write(stateTag, property, state);
        }
        if (!stateTag.isEmpty()) {
            stack.addTagElement("BlockStateTag", stateTag);
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            entity.saveToItem(stack);
        }
        return stack;
    }

    private static <T extends Comparable<T>> void write(CompoundTag tag, Property<T> property, BlockState state) {
        tag.putString(property.getName(), property.getName(state.getValue(property)));
    }
}
