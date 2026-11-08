package cts.yblk.cts.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.registry.ModItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagSlipItem extends Item {
    public static final String TAG = "Tag";
    public static final String PICK = "Pick";
    public static final String ALL = "*";

    public TagSlipItem(Properties properties) {
        super(properties);
    }

    public static boolean isSlip(ItemStack stack) {
        return stack.is(ModItems.TAG_SLIP.get());
    }

    public static String read(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG)) {
            return "";
        }
        return stack.getTag().getString(TAG);
    }

    public static void apply(ItemStack stack, String raw, String pick) {
        if (!isSlip(stack)) {
            return;
        }
        String cleaned = sanitize(raw);
        if (!cleaned.isEmpty()) {
            stack.getOrCreateTag().putString(TAG, cleaned);
        }
        if (pick == null) {
            return;
        }
        CompoundTag nbt = stack.getOrCreateTag();
        if (ALL.equals(pick)) {
            nbt.remove(PICK);
            return;
        }
        ListTag list = new ListTag();
        if (!pick.isBlank()) {
            for (String part : pick.split(",")) {
                String id = part.trim();
                if (ResourceLocation.tryParse(id) != null) {
                    list.add(StringTag.valueOf(id));
                }
            }
        }
        nbt.put(PICK, list);
    }

    public static boolean filters(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(PICK, Tag.TAG_LIST);
    }

    public static String pickToken(ItemStack stack) {
        if (!filters(stack)) {
            return ALL;
        }
        return String.join(",", readPick(stack));
    }

    public static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isEmpty()) {
            return "";
        }
        if (!trimmed.contains(":")) {
            trimmed = "minecraft:" + trimmed;
        }
        return ResourceLocation.tryParse(trimmed) == null ? "" : trimmed;
    }

    public static boolean isItemTag(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location != null && BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, location)).isPresent();
    }

    public static List<Item> members(String id) {
        if (isItemTag(id)) {
            return taggedItems(id);
        }
        return blockItems(id);
    }

    public static List<Item> blockItems(String id) {
        List<Item> items = new ArrayList<>();
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return items;
        }
        BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, location)).ifPresent(set -> set.forEach(holder -> {
            Item item = holder.value().asItem();
            if (item != Items.AIR) {
                items.add(item);
            }
        }));
        return items;
    }

    public static List<Item> selected(ItemStack stack) {
        List<Item> found = members(read(stack));
        if (!filters(stack)) {
            return found;
        }
        Set<String> allow = new HashSet<>(readPick(stack));
        found.removeIf(item -> {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            return key == null || !allow.contains(key.toString());
        });
        return found;
    }

    public static ItemStack resolve(ItemStack template, RandomSource random) {
        if (!isSlip(template)) {
            ItemStack copy = template.copy();
            copy.setCount(1);
            return copy;
        }
        List<Item> found = selected(template);
        if (found.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(found.get(random.nextInt(found.size())));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cts.tag_slip.tooltip").withStyle(ChatFormatting.GRAY));
        String tag = read(stack);
        if (!tag.isEmpty()) {
            tooltip.add(Component.literal("#" + tag).withStyle(ChatFormatting.GRAY));
        }
        if (filters(stack)) {
            tooltip.add(Component.translatable("gui.cts.tag_chosen", readPick(stack).size()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static List<String> readPick(ItemStack stack) {
        List<String> ids = new ArrayList<>();
        if (!stack.hasTag()) {
            return ids;
        }
        ListTag list = stack.getTag().getList(PICK, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ids.add(list.getString(i));
        }
        return ids;
    }

    private static List<Item> taggedItems(String id) {
        List<Item> items = new ArrayList<>();
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return items;
        }
        BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, location)).ifPresent(set -> set.forEach(holder -> items.add(holder.value())));
        return items;
    }
}
