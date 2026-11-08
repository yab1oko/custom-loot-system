package cts.yblk.cts.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import cts.yblk.cts.Cts;
import cts.yblk.cts.blockentity.SpawnChance;
import cts.yblk.cts.registry.ModItems;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class LootTableImporter {
    private LootTableImporter() {
    }

    public static Optional<LootBookData> load(MinecraftServer server, String rawId) {
        ResourceLocation id = parseId(rawId);
        if (id == null) {
            return Optional.empty();
        }
        Optional<JsonObject> json = readJson(server, id);
        if (json.isEmpty()) {
            return Optional.empty();
        }
        LootBookData data = fromJson(json.get(), id.toString());
        return Optional.of(data);
    }

    public static LootBookData fromJson(JsonObject root, String tableId) {
        LootBookData data = new LootBookData();
        data.clearPages();
        data.setName(tableId);
        List<LootBookData.Entry> entries = new ArrayList<>();
        JsonArray pools = root.has("pools") && root.get("pools").isJsonArray() ? root.getAsJsonArray("pools") : new JsonArray();
        for (JsonElement poolElement : pools) {
            if (!poolElement.isJsonObject()) {
                continue;
            }
            collectPool(poolElement.getAsJsonObject(), entries);
        }
        data.fill(entries);
        return data;
    }

    private static void collectPool(JsonObject pool, List<LootBookData.Entry> out) {
        JsonArray entries = pool.has("entries") && pool.get("entries").isJsonArray()
                ? pool.getAsJsonArray("entries")
                : new JsonArray();
        int emptyWeight = 0;
        int itemWeight = 0;
        List<JsonObject> leaves = new ArrayList<>();
        for (JsonElement element : entries) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String type = typeOf(entry);
            int weight = Math.max(0, intOr(entry, "weight", 1));
            if ("empty".equals(type)) {
                emptyWeight += weight;
                continue;
            }
            List<JsonObject> found = new ArrayList<>();
            flatten(entry, found);
            for (JsonObject leaf : found) {
                leaf.addProperty("_weight", Math.max(0, intOr(leaf, "weight", weight)));
                leaves.add(leaf);
                itemWeight += Math.max(0, intOr(leaf, "_weight", 1));
            }
        }
        if (leaves.isEmpty()) {
            return;
        }
        // CTS export: one item + optional empty in a pool → chance = item weight directly if empty makes 100.
        boolean ctsStyle = leaves.size() == 1 && emptyWeight > 0 && itemWeight + emptyWeight == 100;
        for (JsonObject leaf : leaves) {
            int weight = Math.max(0, intOr(leaf, "_weight", 1));
            int chance;
            if (ctsStyle) {
                chance = weight;
            } else if (emptyWeight > 0) {
                int total = itemWeight + emptyWeight;
                chance = total <= 0 ? 0 : (int) Math.round(100.0D * weight / total);
            } else if (leaves.size() == 1) {
                chance = 100;
            } else {
                chance = itemWeight <= 0 ? 0 : (int) Math.round(100.0D * weight / itemWeight);
            }
            LootBookData.Entry entry = leafToEntry(leaf, SpawnChance.clamp(chance));
            if (entry != null) {
                out.add(entry);
            }
        }
    }

    private static void flatten(JsonObject entry, List<JsonObject> out) {
        String type = typeOf(entry);
        if ("item".equals(type) || "tag".equals(type)) {
            out.add(entry);
            return;
        }
        if ("alternatives".equals(type) || "group".equals(type) || "sequence".equals(type)) {
            JsonArray children = entry.has("children") && entry.get("children").isJsonArray()
                    ? entry.getAsJsonArray("children")
                    : new JsonArray();
            for (JsonElement child : children) {
                if (child.isJsonObject()) {
                    flatten(child.getAsJsonObject(), out);
                }
            }
        }
    }

    private static LootBookData.Entry leafToEntry(JsonObject leaf, int chance) {
        if (chance <= 0) {
            return null;
        }
        String type = typeOf(leaf);
        if ("tag".equals(type)) {
            String tag = stringOr(leaf, "name", "");
            if (tag.isEmpty()) {
                return null;
            }
            ItemStack slip = new ItemStack(ModItems.TAG_SLIP.get());
            TagSlipItem.apply(slip, tag, TagSlipItem.ALL);
            return LootBookData.Entry.create(slip, 1, 1, chance, false);
        }
        if (!"item".equals(type)) {
            return null;
        }
        String name = stringOr(leaf, "name", "");
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return null;
        }
        ItemStack stack = new ItemStack(item);
        int min = 1;
        int max = 1;
        boolean randomDamage = false;
        boolean randomEnchant = false;
        CompoundTag nbt = null;
        if (leaf.has("functions") && leaf.get("functions").isJsonArray()) {
            for (JsonElement functionElement : leaf.getAsJsonArray("functions")) {
                if (!functionElement.isJsonObject()) {
                    continue;
                }
                JsonObject function = functionElement.getAsJsonObject();
                String fn = typeOf(function, "function");
                if ("set_count".equals(fn)) {
                    int[] range = readCount(function.get("count"));
                    min = range[0];
                    max = range[1];
                } else if ("set_damage".equals(fn)) {
                    randomDamage = stack.isDamageableItem();
                } else if ("enchant_randomly".equals(fn) || "enchant_with_levels".equals(fn)) {
                    randomEnchant = true;
                } else if ("set_nbt".equals(fn)) {
                    String raw = stringOr(function, "tag", "");
                    if (!raw.isEmpty()) {
                        try {
                            nbt = net.minecraft.nbt.TagParser.parseTag(raw);
                        } catch (Exception ignored) {
                            nbt = null;
                        }
                    }
                }
            }
        }
        if (nbt != null && !nbt.isEmpty()) {
            stack.setTag(nbt);
        }
        if (stack.isDamageableItem()) {
            min = 1;
            max = 1;
        } else {
            randomDamage = false;
            min = SpawnChance.clampCount(min);
            max = SpawnChance.clampCount(max);
        }
        stack.setCount(1);
        return LootBookData.Entry.create(stack, min, max, chance, randomDamage, randomEnchant);
    }

    private static int[] readCount(JsonElement count) {
        if (count == null || count.isJsonNull()) {
            return new int[]{1, 1};
        }
        if (count.isJsonPrimitive()) {
            int value = Math.max(1, count.getAsInt());
            return new int[]{value, value};
        }
        if (count.isJsonObject()) {
            JsonObject object = count.getAsJsonObject();
            int min = Math.max(1, (int) Math.floor(doubleOr(object, "min", 1)));
            int max = Math.max(min, (int) Math.ceil(doubleOr(object, "max", min)));
            return new int[]{min, max};
        }
        return new int[]{1, 1};
    }

    private static Optional<JsonObject> readJson(MinecraftServer server, ResourceLocation id) {
        Path file = LootTableFiles.sourceFile(id);
        if (file != null) {
            try {
                String text = Files.readString(file, StandardCharsets.UTF_8);
                JsonElement element = JsonParser.parseString(text);
                if (element.isJsonObject()) {
                    return Optional.of(element.getAsJsonObject());
                }
            } catch (Exception exception) {
                Cts.LOGGER.warn("Failed to read exported loot table {}", file, exception);
            }
        }
        ResourceLocation path = new ResourceLocation(id.getNamespace(), "loot_tables/" + id.getPath() + ".json");
        try {
            Optional<Resource> resource = server.getResourceManager().getResource(path);
            if (resource.isEmpty()) {
                return Optional.empty();
            }
            try (BufferedReader reader = resource.get().openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element.isJsonObject()) {
                    return Optional.of(element.getAsJsonObject());
                }
            }
        } catch (Exception exception) {
            Cts.LOGGER.warn("Failed to read loot table {}", id, exception);
        }
        return Optional.empty();
    }

    private static ResourceLocation parseId(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!trimmed.contains(":")) {
            trimmed = Cts.MODID + ":" + trimmed;
        }
        return ResourceLocation.tryParse(trimmed);
    }

    private static String typeOf(JsonObject object) {
        return typeOf(object, "type");
    }

    private static String typeOf(JsonObject object, String key) {
        String raw = stringOr(object, key, "");
        int colon = raw.indexOf(':');
        return colon >= 0 ? raw.substring(colon + 1) : raw;
    }

    private static String stringOr(JsonObject object, String key, String fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : fallback;
    }

    private static int intOr(JsonObject object, String key, int fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsInt() : fallback;
    }

    private static double doubleOr(JsonObject object, String key, double fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsDouble() : fallback;
    }
}
