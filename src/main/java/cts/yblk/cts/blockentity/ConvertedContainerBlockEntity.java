package cts.yblk.cts.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ConvertedContainerBlockEntity extends CanStashBlockEntity {
    private BlockState mimic = Blocks.BARREL.defaultBlockState();

    public ConvertedContainerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public BlockState getMimic() {
        return mimic;
    }

    public void setMimic(BlockState mimic) {
        this.mimic = mimic == null ? Blocks.BARREL.defaultBlockState() : mimic;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return mimic.getBlock().getName();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Mimic", NbtUtils.writeBlockState(mimic));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Mimic", 10)) {
            var lookup = level != null
                    ? level.holderLookup(Registries.BLOCK)
                    : BuiltInRegistries.BLOCK.asLookup();
            mimic = NbtUtils.readBlockState(lookup, tag.getCompound("Mimic"));
        }
    }
}
