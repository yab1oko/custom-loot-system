package cts.yblk.cts.library;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import cts.yblk.cts.Cts;
import cts.yblk.cts.block.CopiedBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.ConvertedContainerBlockEntity;
import cts.yblk.cts.registry.ModBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Per-world library of saved CTS stash configurations (props and converted containers).
 */
public final class SavedChestLibrary extends SavedData {
    private static final String DATA_NAME = Cts.MODID + "_saved_chests";

    private final List<Entry> entries = new ArrayList<>();

    public static SavedChestLibrary get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(SavedChestLibrary::load, SavedChestLibrary::new, DATA_NAME);
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public Optional<Entry> find(UUID id) {
        for (Entry entry : entries) {
            if (entry.id.equals(id)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public Entry addFrom(CanStashBlockEntity entity, String name) {
        BlockState state = entity.getBlockState();
        Block block = state.getBlock();
        ItemStack stack = CopiedBlock.item(block, state, entity.getLevel(), entity.getBlockPos());
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        CompoundTag beTag = BlockItem.getBlockEntityData(stack);
        CompoundTag stash = beTag == null ? new CompoundTag() : beTag.copy();
        CompoundTag stateTag = stack.getTagElement("BlockStateTag");
        CompoundTag mimic = new CompoundTag();
        if (entity instanceof ConvertedContainerBlockEntity converted) {
            mimic = NbtUtils.writeBlockState(converted.getMimic());
            stash.remove("Mimic");
        }
        Entry entry = new Entry(UUID.randomUUID(), sanitizeName(name), blockId,
                mimic, stash, stateTag == null ? new CompoundTag() : stateTag.copy());
        entries.add(entry);
        setDirty();
        return entry;
    }

    public boolean rename(UUID id, String name) {
        Optional<Entry> found = find(id);
        if (found.isEmpty()) {
            return false;
        }
        found.get().name = sanitizeName(name);
        setDirty();
        return true;
    }

    public boolean delete(UUID id) {
        boolean removed = entries.removeIf(entry -> entry.id.equals(id));
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public ItemStack createStack(Entry entry) {
        Block block = BuiltInRegistries.BLOCK.getOptional(entry.blockId).orElse(ModBlocks.CONVERTED_CONTAINER.get());
        if (block == Blocks.AIR) {
            block = ModBlocks.CONVERTED_CONTAINER.get();
        }
        ItemStack stack = new ItemStack(block);
        if (!entry.stateTag.isEmpty()) {
            stack.addTagElement("BlockStateTag", entry.stateTag.copy());
        }
        CompoundTag tag = entry.stash.copy();
        if (block == ModBlocks.CONVERTED_CONTAINER.get() && !tag.contains("Mimic") && !entry.mimic.isEmpty()) {
            tag.put("Mimic", entry.mimic.copy());
        }
        if (!tag.isEmpty()) {
            stack.addTagElement("BlockEntityTag", tag);
        }
        if (!entry.name.isBlank()) {
            stack.setHoverName(net.minecraft.network.chat.Component.literal(entry.name));
        }
        return stack;
    }

    public static SavedChestLibrary load(CompoundTag tag) {
        SavedChestLibrary library = new SavedChestLibrary();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            library.entries.add(Entry.read(list.getCompound(i)));
        }
        return library;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            list.add(entry.write());
        }
        tag.put("Entries", list);
        return tag;
    }

    private static String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return "Chest";
        }
        String trimmed = name.trim();
        return trimmed.length() > 64 ? trimmed.substring(0, 64) : trimmed;
    }

    public static final class Entry {
        private final UUID id;
        private String name;
        private final ResourceLocation blockId;
        private final CompoundTag mimic;
        private final CompoundTag stash;
        private final CompoundTag stateTag;

        public Entry(UUID id, String name, ResourceLocation blockId, CompoundTag mimic, CompoundTag stash, CompoundTag stateTag) {
            this.id = id;
            this.name = name;
            this.blockId = blockId == null ? ModBlocks.CONVERTED_CONTAINER.getId() : blockId;
            this.mimic = mimic == null ? new CompoundTag() : mimic;
            this.stash = stash == null ? new CompoundTag() : stash;
            this.stateTag = stateTag == null ? new CompoundTag() : stateTag;
        }

        public UUID id() {
            return id;
        }

        public String name() {
            return name;
        }

        public ResourceLocation blockId() {
            return blockId;
        }

        public CompoundTag mimic() {
            return mimic;
        }

        public CompoundTag stash() {
            return stash;
        }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", id);
            tag.putString("Name", name);
            tag.putString("Block", blockId.toString());
            if (!mimic.isEmpty()) {
                tag.put("Mimic", mimic.copy());
            }
            tag.put("Stash", stash.copy());
            if (!stateTag.isEmpty()) {
                tag.put("BlockStateTag", stateTag.copy());
            }
            return tag;
        }

        public CompoundTag writeSync() {
            return write();
        }

        public static Entry read(CompoundTag tag) {
            return readSync(tag);
        }

        public static Entry readSync(CompoundTag tag) {
            UUID id = tag.hasUUID("Id") ? tag.getUUID("Id") : UUID.randomUUID();
            ResourceLocation blockId = tag.contains("Block", Tag.TAG_STRING)
                    ? ResourceLocation.tryParse(tag.getString("Block"))
                    : ModBlocks.CONVERTED_CONTAINER.getId();
            if (blockId == null) {
                blockId = ModBlocks.CONVERTED_CONTAINER.getId();
            }
            CompoundTag mimic = tag.contains("Mimic", Tag.TAG_COMPOUND)
                    ? tag.getCompound("Mimic").copy()
                    : new CompoundTag();
            CompoundTag stash = tag.contains("Stash", Tag.TAG_COMPOUND)
                    ? tag.getCompound("Stash").copy()
                    : new CompoundTag();
            CompoundTag stateTag = tag.contains("BlockStateTag", Tag.TAG_COMPOUND)
                    ? tag.getCompound("BlockStateTag").copy()
                    : new CompoundTag();
            return new Entry(id, sanitizeName(tag.getString("Name")), blockId, mimic, stash, stateTag);
        }
    }
}
