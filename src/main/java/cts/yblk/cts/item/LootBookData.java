package cts.yblk.cts.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import cts.yblk.cts.blockentity.SpawnChance;

import java.util.ArrayList;
import java.util.List;

public final class LootBookData {
    public static final int COLUMNS = 9;
    public static final int ROWS = 6;
    public static final int PAGE_SIZE = COLUMNS * ROWS;

    private final List<Entry[]> pages = new ArrayList<>();
    private int page;
    private String name = "cts:loot_table";

    public LootBookData() {
        pages.add(blank());
    }

    public int page() {
        return page;
    }

    public int pageCount() {
        return pages.size();
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        String cleaned = LootTableFiles.sanitizeId(name);
        this.name = cleaned.isEmpty() ? "cts:loot_table" : cleaned;
    }

    public Entry entry(int slot) {
        return pages.get(page)[slot];
    }

    public List<Entry[]> pages() {
        return pages;
    }

    public void clearPages() {
        pages.clear();
        pages.add(blank());
        page = 0;
    }

    public void fill(List<Entry> entries) {
        clearPages();
        if (entries == null || entries.isEmpty()) {
            return;
        }
        pages.clear();
        Entry[] current = blank();
        int slot = 0;
        for (Entry entry : entries) {
            if (entry == null || entry.stack.isEmpty()) {
                continue;
            }
            if (slot >= PAGE_SIZE) {
                pages.add(current);
                current = blank();
                slot = 0;
            }
            current[slot].copyFrom(entry);
            slot++;
        }
        pages.add(current);
        page = 0;
    }

    public void replace(LootBookData other) {
        CompoundTag tag = new CompoundTag();
        other.write(tag);
        read(tag);
    }

    public void read(CompoundTag tag) {
        pages.clear();
        name = tag.contains("TableName", Tag.TAG_STRING) ? tag.getString("TableName") : "cts:loot_table";
        if (LootTableFiles.sanitizeId(name).isEmpty()) {
            name = "cts:loot_table";
        } else {
            name = LootTableFiles.sanitizeId(name);
        }
        ListTag savedPages = tag.getList("Pages", Tag.TAG_COMPOUND);
        List<Entry> overflow = new ArrayList<>();
        if (savedPages.isEmpty()) {
            pages.add(blank());
        } else {
            for (int i = 0; i < savedPages.size(); i++) {
                pages.add(readPage(savedPages.getCompound(i), overflow));
            }
        }
        placeOverflow(overflow);
        page = Math.max(0, Math.min(pages.size() - 1, tag.getInt("Page")));
    }

    public void write(CompoundTag tag) {
        tag.putString("TableName", name);
        tag.putInt("Page", page);
        ListTag savedPages = new ListTag();
        for (Entry[] entries : pages) {
            CompoundTag pageTag = new CompoundTag();
            ListTag slots = new ListTag();
            for (int slot = 0; slot < PAGE_SIZE; slot++) {
                Entry entry = entries[slot];
                if (entry.stack.isEmpty()) {
                    continue;
                }
                CompoundTag slotTag = new CompoundTag();
                slotTag.putInt("Slot", slot);
                slotTag.put("Item", entry.stack.save(new CompoundTag()));
                slotTag.putInt("Min", entry.min);
                slotTag.putInt("Max", entry.max);
                slotTag.putInt("Chance", entry.chance);
                slotTag.putBoolean("RandomDamage", entry.randomDamage);
                slotTag.putBoolean("RandomEnchant", entry.randomEnchant);
                slots.add(slotTag);
            }
            pageTag.put("Slots", slots);
            savedPages.add(pageTag);
        }
        tag.put("Pages", savedPages);
    }

    public void setPage(int index) {
        if (index >= pages.size()) {
            pages.add(blank());
        }
        page = Math.max(0, Math.min(pages.size() - 1, index));
    }

    public boolean openNext() {
        if (page + 1 < pages.size()) {
            page++;
            return true;
        }
        if (!pageHasItems(page)) {
            return false;
        }
        pages.add(blank());
        page++;
        return true;
    }

    public boolean openPrevious() {
        if (page <= 0) {
            return false;
        }
        page--;
        return true;
    }

    public void place(int slot, ItemStack stack) {
        Entry entry = entry(slot);
        if (stack.isEmpty()) {
            entry.clear();
            return;
        }
        boolean damageable = stack.isDamageableItem();
        int count = damageable ? 1 : SpawnChance.clampCount(stack.getCount());
        ItemStack visual = stack.copy();
        visual.setCount(1);
        entry.stack = visual;
        entry.min = count;
        entry.max = count;
        entry.chance = SpawnChance.DEFAULT;
        entry.randomDamage = false;
        entry.randomEnchant = false;
    }

    public void setEntry(int slot, int min, int max, int chance, boolean randomDamage, boolean randomEnchant, String tag, String pick) {
        Entry entry = entry(slot);
        if (entry.stack.isEmpty()) {
            return;
        }
        if (entry.stack.isDamageableItem()) {
            entry.randomDamage = randomDamage;
            entry.min = 1;
            entry.max = 1;
        } else {
            entry.randomDamage = false;
            entry.min = SpawnChance.clampCount(Math.min(min, max));
            entry.max = SpawnChance.clampCount(Math.max(min, max));
        }
        entry.randomEnchant = randomEnchant && SpawnChance.canRandomEnchant(entry.stack);
        entry.chance = SpawnChance.clamp(chance);
        entry.stack.setCount(1);
        TagSlipItem.apply(entry.stack, tag, pick);
    }

    private boolean pageHasItems(int index) {
        for (Entry entry : pages.get(index)) {
            if (!entry.stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void placeOverflow(List<Entry> overflow) {
        for (Entry extra : overflow) {
            boolean placed = false;
            for (Entry[] entries : pages) {
                for (int slot = 0; slot < entries.length; slot++) {
                    if (entries[slot].stack.isEmpty()) {
                        entries[slot].copyFrom(extra);
                        placed = true;
                        break;
                    }
                }
                if (placed) {
                    break;
                }
            }
            if (!placed) {
                Entry[] extraPage = blank();
                extraPage[0].copyFrom(extra);
                pages.add(extraPage);
            }
        }
    }

    private static Entry[] readPage(CompoundTag tag, List<Entry> overflow) {
        Entry[] entries = blank();
        ListTag slots = tag.getList("Slots", Tag.TAG_COMPOUND);
        for (int i = 0; i < slots.size(); i++) {
            CompoundTag slotTag = slots.getCompound(i);
            int slot = slotTag.getInt("Slot");
            if (slot < 0) {
                continue;
            }
            Entry loaded = Entry.from(slotTag);
            if (loaded.stack.isEmpty()) {
                continue;
            }
            if (slot >= PAGE_SIZE) {
                overflow.add(loaded);
            } else {
                entries[slot].copyFrom(loaded);
            }
        }
        return entries;
    }

    private static Entry[] blank() {
        Entry[] entries = new Entry[PAGE_SIZE];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new Entry();
        }
        return entries;
    }

    public static final class Entry {
        private ItemStack stack = ItemStack.EMPTY;
        private int min = 1;
        private int max = 1;
        private int chance = SpawnChance.DEFAULT;
        private boolean randomDamage;
        private boolean randomEnchant;

        public ItemStack stack() {
            return stack;
        }

        public int min() {
            return min;
        }

        public int max() {
            return max;
        }

        public int chance() {
            return chance;
        }

        public boolean randomDamage() {
            return randomDamage;
        }

        public boolean randomEnchant() {
            return randomEnchant;
        }

        public static Entry create(ItemStack stack, int min, int max, int chance, boolean randomDamage) {
            return create(stack, min, max, chance, randomDamage, false);
        }

        public static Entry create(ItemStack stack, int min, int max, int chance, boolean randomDamage, boolean randomEnchant) {
            Entry entry = new Entry();
            entry.stack = stack.copy();
            entry.stack.setCount(1);
            entry.min = SpawnChance.clampCount(min);
            entry.max = SpawnChance.clampCount(max);
            entry.chance = SpawnChance.clamp(chance);
            entry.randomDamage = randomDamage && entry.stack.isDamageableItem();
            entry.randomEnchant = randomEnchant && SpawnChance.canRandomEnchant(entry.stack);
            if (entry.stack.isDamageableItem()) {
                entry.min = 1;
                entry.max = 1;
            } else {
                entry.randomDamage = false;
            }
            return entry;
        }

        private void clear() {
            stack = ItemStack.EMPTY;
            min = 1;
            max = 1;
            chance = SpawnChance.DEFAULT;
            randomDamage = false;
            randomEnchant = false;
        }

        private void copyFrom(Entry other) {
            stack = other.stack.copy();
            min = other.min;
            max = other.max;
            chance = other.chance;
            randomDamage = other.randomDamage;
            randomEnchant = other.randomEnchant;
        }

        private static Entry from(CompoundTag slotTag) {
            Entry entry = new Entry();
            entry.stack = ItemStack.of(slotTag.getCompound("Item"));
            entry.stack.setCount(1);
            entry.min = SpawnChance.clampCount(slotTag.getInt("Min"));
            entry.max = SpawnChance.clampCount(slotTag.getInt("Max"));
            entry.chance = SpawnChance.clamp(slotTag.contains("Chance") ? slotTag.getInt("Chance") : SpawnChance.DEFAULT);
            entry.randomDamage = slotTag.getBoolean("RandomDamage");
            entry.randomEnchant = slotTag.getBoolean("RandomEnchant");
            if (entry.stack.isEmpty()) {
                entry.clear();
            } else if (entry.stack.isDamageableItem()) {
                entry.min = 1;
                entry.max = 1;
            } else {
                entry.randomDamage = false;
            }
            if (!SpawnChance.canRandomEnchant(entry.stack)) {
                entry.randomEnchant = false;
            }
            return entry;
        }
    }
}
