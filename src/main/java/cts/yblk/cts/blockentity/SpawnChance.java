package cts.yblk.cts.blockentity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import cts.yblk.cts.item.TagSlipItem;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

public final class SpawnChance {
    public static final int DEFAULT = 100;

    private SpawnChance() {
    }

    public static int clamp(int chance) {
        return Math.max(0, Math.min(100, chance));
    }

    public static boolean roll(RandomSource random, int chance) {
        int value = clamp(chance);
        return value >= 100 || (value > 0 && random.nextInt(100) < value);
    }

    public static int clampCount(int count) {
        return Math.max(1, Math.min(64, count));
    }

    public static int rollCount(RandomSource random, int min, int max) {
        int low = clampCount(Math.min(min, max));
        int high = clampCount(Math.max(min, max));
        return low == high ? low : low + random.nextInt(high - low + 1);
    }

    public static int pickWeighted(RandomSource random, int size, IntPredicate active, IntUnaryOperator weight) {
        int total = 0;
        for (int i = 0; i < size; i++) {
            if (active.test(i)) {
                total += Math.max(0, weight.applyAsInt(i));
            }
        }
        if (total <= 0) {
            return -1;
        }
        int roll = random.nextInt(total);
        int cursor = 0;
        for (int i = 0; i < size; i++) {
            if (!active.test(i)) {
                continue;
            }
            cursor += Math.max(0, weight.applyAsInt(i));
            if (roll < cursor) {
                return i;
            }
        }
        return -1;
    }

    public static List<Integer> pickGroup(RandomSource random, int size, IntPredicate active, IntUnaryOperator weight) {
        int total = 0;
        for (int i = 0; i < size; i++) {
            if (active.test(i)) {
                total += Math.max(0, weight.applyAsInt(i));
            }
        }
        List<Integer> chosen = new ArrayList<>();
        if (total <= 0) {
            return chosen;
        }
        for (int i = 0; i < size; i++) {
            int entryWeight = active.test(i) ? Math.max(0, weight.applyAsInt(i)) : 0;
            if (entryWeight > 0 && random.nextInt(total) < entryWeight) {
                chosen.add(i);
            }
        }
        if (chosen.isEmpty()) {
            int forced = pickWeighted(random, size, active, weight);
            if (forced >= 0) {
                chosen.add(forced);
            }
        }
        return chosen;
    }

    public static ItemStack spawnStack(ItemStack template, RandomSource random, int min, int max, boolean randomDamage) {
        return spawnStack(template, random, min, max, randomDamage, false);
    }

    public static ItemStack spawnStack(ItemStack template, RandomSource random, int min, int max, boolean randomDamage, boolean randomEnchant) {
        boolean fromTag = TagSlipItem.isSlip(template);
        ItemStack copy = TagSlipItem.resolve(template, random);
        if (copy.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (copy.isDamageableItem()) {
            copy.setCount(1);
            if (randomDamage || fromTag) {
                int maxDamage = copy.getMaxDamage();
                if (maxDamage > 0) {
                    copy.setDamageValue(random.nextInt(maxDamage));
                }
            }
        } else {
            copy.setCount(rollCount(random, min, max));
        }
        if (randomEnchant) {
            applyRandomEnchantment(copy, random);
        }
        return copy;
    }

    public static boolean canRandomEnchant(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (TagSlipItem.isSlip(stack)) {
            return true;
        }
        return !compatibleEnchantments(stack).isEmpty();
    }

    public static void applyRandomEnchantment(ItemStack stack, RandomSource random) {
        if (stack.isEmpty()) {
            return;
        }
        List<Enchantment> options = compatibleEnchantments(stack);
        if (options.isEmpty()) {
            return;
        }
        Enchantment enchantment = options.get(random.nextInt(options.size()));
        int level = Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
        stack.enchant(enchantment, level);
    }

    private static List<Enchantment> compatibleEnchantments(ItemStack stack) {
        List<Enchantment> options = new ArrayList<>();
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment.isDiscoverable() && enchantment.canEnchant(stack)) {
                options.add(enchantment);
            }
        }
        return options;
    }

    public static List<Integer> splitCount(RandomSource random, int total, int maxPiles) {
        int limit = Math.min(Math.max(1, total), Math.max(1, maxPiles));
        int piles = limit == 1 ? 1 : 1 + random.nextInt(limit);
        int[] counts = new int[piles];
        Arrays.fill(counts, 1);
        int extra = total - piles;
        for (int i = 0; i < extra; i++) {
            counts[random.nextInt(piles)]++;
        }
        List<Integer> result = new ArrayList<>(piles);
        for (int count : counts) {
            result.add(count);
        }
        return result;
    }

    public static int[] array(int size) {
        int[] chances = new int[size];
        Arrays.fill(chances, DEFAULT);
        return chances;
    }

    public static int[] counts(int size) {
        int[] values = new int[size];
        Arrays.fill(values, 1);
        return values;
    }

    public static void loadFlags(CompoundTag tag, boolean[] flags) {
        loadFlags(tag, "RandomDamage", flags);
    }

    public static void loadFlags(CompoundTag tag, String key, boolean[] flags) {
        Arrays.fill(flags, false);
        if (!tag.contains(key, Tag.TAG_INT_ARRAY)) {
            return;
        }
        int[] saved = tag.getIntArray(key);
        for (int i = 0; i < Math.min(flags.length, saved.length); i++) {
            flags[i] = saved[i] != 0;
        }
    }

    public static int[] flagArray(boolean[] flags) {
        int[] values = new int[flags.length];
        for (int i = 0; i < flags.length; i++) {
            values[i] = flags[i] ? 1 : 0;
        }
        return values;
    }

    public static void load(CompoundTag tag, int[] chances) {
        Arrays.fill(chances, DEFAULT);
        if (!tag.contains("Chances", Tag.TAG_INT_ARRAY)) {
            return;
        }
        int[] saved = tag.getIntArray("Chances");
        for (int i = 0; i < Math.min(chances.length, saved.length); i++) {
            chances[i] = clamp(saved[i]);
        }
    }

    public static void loadCounts(CompoundTag tag, int[] countMin, int[] countMax, ItemStackHandler template) {
        Arrays.fill(countMin, 1);
        Arrays.fill(countMax, 1);
        boolean saved = tag.contains("CountMin", Tag.TAG_INT_ARRAY) && tag.contains("CountMax", Tag.TAG_INT_ARRAY);
        if (saved) {
            int[] min = tag.getIntArray("CountMin");
            int[] max = tag.getIntArray("CountMax");
            for (int i = 0; i < countMin.length; i++) {
                if (i < min.length) {
                    countMin[i] = clampCount(min[i]);
                }
                if (i < max.length) {
                    countMax[i] = clampCount(max[i]);
                }
                if (countMin[i] > countMax[i]) {
                    int swap = countMin[i];
                    countMin[i] = countMax[i];
                    countMax[i] = swap;
                }
            }
        }
        for (int i = 0; i < countMin.length && i < template.getSlots(); i++) {
            ItemStack stack = template.getStackInSlot(i);
            if (stack.isEmpty()) {
                countMin[i] = 1;
                countMax[i] = 1;
                continue;
            }
            if (!saved) {
                int count = clampCount(stack.getCount());
                countMin[i] = count;
                countMax[i] = count;
            }
            stack.setCount(1);
        }
    }
}
