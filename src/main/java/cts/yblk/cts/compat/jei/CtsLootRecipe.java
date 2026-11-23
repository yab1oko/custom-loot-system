package cts.yblk.cts.compat.jei;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.library.SavedChestLibrary;

final class CtsLootRecipe {
    final String name;
    final boolean lootTable;
    final String tableId;
    final ItemStack[] stacks = new ItemStack[CanStashBlockEntity.SLOT_COUNT];
    final int[] chance = new int[CanStashBlockEntity.SLOT_COUNT];
    final int[] min = new int[CanStashBlockEntity.SLOT_COUNT];
    final int[] max = new int[CanStashBlockEntity.SLOT_COUNT];
    final boolean[] damage = new boolean[CanStashBlockEntity.SLOT_COUNT];
    final boolean[] enchant = new boolean[CanStashBlockEntity.SLOT_COUNT];

    private CtsLootRecipe(String name, boolean lootTable, String tableId) {
        this.name = name;
        this.lootTable = lootTable;
        this.tableId = tableId;
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = ItemStack.EMPTY;
            chance[i] = 100;
        }
    }

    static CtsLootRecipe from(SavedChestLibrary.Entry entry) {
        CompoundTag stash = entry.stash();
        boolean table = "LOOT_TABLE".equals(stash.getString("Mode"));
        CtsLootRecipe recipe = new CtsLootRecipe(entry.name(), table, stash.getString("LootTable"));
        if (!table && stash.contains("Template")) {
            ItemStackHandler template = new ItemStackHandler(CanStashBlockEntity.SLOT_COUNT);
            template.deserializeNBT(stash.getCompound("Template"));
            int[] chances = stash.getIntArray("Chances");
            int[] countMin = stash.getIntArray("CountMin");
            int[] countMax = stash.getIntArray("CountMax");
            int[] randomDamage = stash.getIntArray("RandomDamage");
            int[] randomEnchant = stash.getIntArray("RandomEnchant");
            for (int slot = 0; slot < recipe.stacks.length; slot++) {
                recipe.stacks[slot] = template.getStackInSlot(slot);
                recipe.chance[slot] = slot < chances.length ? chances[slot] : 100;
                int low = slot < countMin.length ? countMin[slot] : recipe.stacks[slot].getCount();
                int high = slot < countMax.length ? countMax[slot] : low;
                recipe.min[slot] = low;
                recipe.max[slot] = high;
                recipe.damage[slot] = slot < randomDamage.length && randomDamage[slot] != 0;
                recipe.enchant[slot] = slot < randomEnchant.length && randomEnchant[slot] != 0;
            }
        }
        return recipe;
    }
}
