package cts.yblk.cts.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import cts.yblk.cts.Cts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class LootTableFiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private LootTableFiles() {
    }

    public static Path directory() {
        return FMLPaths.GAMEDIR.get().resolve(Cts.MODID).resolve("loottables");
    }

    /** Pre-rebrand folder; still read so old exports keep working. */
    private static Path legacyDirectory() {
        return FMLPaths.GAMEDIR.get().resolve("rpgloot").resolve("loottables");
    }

    /** Path-only id under the mod namespace (legacy helpers). */
    public static String sanitize(String raw) {
        ResourceLocation id = parseId(raw, true);
        if (id == null || !Cts.MODID.equals(id.getNamespace())) {
            return "";
        }
        return id.getPath();
    }

    /** Full loot table id (`namespace:path`). Bare names become `cts:...`. */
    public static String sanitizeId(String raw) {
        ResourceLocation id = parseId(raw, false);
        return id == null ? "" : id.toString();
    }

    public static ResourceLocation parseId(String raw, boolean forceModNamespace) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim().toLowerCase(Locale.ROOT).replace('\\', '/').replace(' ', '_');
        while (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        while (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.isEmpty() || cleaned.contains("..")) {
            return null;
        }
        if (forceModNamespace) {
            int colon = cleaned.indexOf(':');
            if (colon >= 0) {
                cleaned = cleaned.substring(colon + 1);
            }
            cleaned = Cts.MODID + ":" + cleaned;
        } else if (!cleaned.contains(":")) {
            cleaned = Cts.MODID + ":" + cleaned;
        }
        ResourceLocation id = ResourceLocation.tryParse(cleaned);
        if (id == null || id.getPath().isEmpty() || id.getPath().contains("..")) {
            return null;
        }
        return id;
    }

    public static Path write(LootBookData data) throws IOException {
        ResourceLocation id = parseId(data.name(), false);
        if (id == null) {
            id = new ResourceLocation(Cts.MODID, "loot_table");
        }
        Path file = fileFor(directory(), id);
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(toJson(data)), StandardCharsets.UTF_8);
        return file;
    }

    public static Path sourceFile(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        for (Path root : List.of(directory(), legacyDirectory())) {
            Path file = resolveUnder(root, id);
            if (file != null) {
                return file;
            }
            // Legacy flat cts path: loottables/foo.json for cts:foo
            if (Cts.MODID.equals(id.getNamespace())) {
                Path flat = root.resolve(id.getPath() + ".json").normalize();
                Path sourceRoot = root.toAbsolutePath().normalize();
                if (flat.startsWith(sourceRoot) && Files.isRegularFile(flat)) {
                    return flat;
                }
            }
        }
        return null;
    }

    public static List<String> listNames() {
        List<String> names = new ArrayList<>();
        for (Path source : List.of(directory(), legacyDirectory())) {
            names.addAll(listNamesIn(source));
        }
        return names.stream().distinct().sorted().toList();
    }

    private static List<String> listNamesIn(Path source) {
        if (!Files.isDirectory(source)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(source)) {
            return files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .map(path -> {
                        String relative = source.relativize(path).toString().replace('\\', '/');
                        String withoutExt = relative.substring(0, relative.length() - ".json".length());
                        if (withoutExt.contains(":")) {
                            return "";
                        }
                        // namespace/path.json → namespace:path ; path.json → cts:path
                        int slash = withoutExt.indexOf('/');
                        if (slash > 0) {
                            String ns = withoutExt.substring(0, slash);
                            String tablePath = withoutExt.substring(slash + 1);
                            ResourceLocation id = ResourceLocation.tryParse(ns + ":" + tablePath);
                            return id == null ? "" : id.toString();
                        }
                        String sanitized = sanitize(withoutExt);
                        return sanitized.isEmpty() ? "" : Cts.MODID + ":" + sanitized;
                    })
                    .filter(name -> !name.isEmpty())
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            return List.of();
        }
    }

    /** @param lootTablesDirectory historically `datapacks/cts/data/cts/loot_tables` */
    public static int copyInto(Path lootTablesDirectory) throws IOException {
        Path dataRoot = lootTablesDirectory.getParent().getParent();
        int copied = 0;
        for (String name : listNames()) {
            if (copyOneToData(dataRoot, name)) {
                copied++;
            }
        }
        return copied;
    }

    public static boolean copyOne(Path lootTablesDirectory, String name) throws IOException {
        return copyOneToData(lootTablesDirectory.getParent().getParent(), name);
    }

    private static boolean copyOneToData(Path dataRoot, String name) throws IOException {
        ResourceLocation id = parseId(name, false);
        if (id == null) {
            return false;
        }
        Path source = sourceFile(id);
        if (source == null) {
            return false;
        }
        Path target = dataRoot.resolve(id.getNamespace()).resolve("loot_tables").resolve(id.getPath() + ".json");
        Files.createDirectories(target.getParent());
        Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    private static Path fileFor(Path root, ResourceLocation id) {
        return root.resolve(id.getNamespace()).resolve(id.getPath() + ".json");
    }

    private static Path resolveUnder(Path root, ResourceLocation id) {
        Path sourceRoot = root.toAbsolutePath().normalize();
        if (!Files.isDirectory(sourceRoot)) {
            return null;
        }
        Path source = fileFor(sourceRoot, id).normalize();
        if (source.startsWith(sourceRoot) && Files.isRegularFile(source)) {
            return source;
        }
        return null;
    }

    private static JsonObject toJson(LootBookData data) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:chest");
        JsonArray pools = new JsonArray();
        for (LootBookData.Entry[] page : data.pages()) {
            for (LootBookData.Entry entry : page) {
                if (TagSlipItem.isSlip(entry.stack())) {
                    for (JsonObject extra : tagPools(entry)) {
                        pools.add(extra);
                    }
                    continue;
                }
                JsonObject pool = pool(entry);
                if (pool != null) {
                    pools.add(pool);
                }
            }
        }
        root.add("pools", pools);
        return root;
    }

    private static JsonObject pool(LootBookData.Entry entry) {
        ItemStack stack = entry.stack();
        if (stack.isEmpty() || entry.chance() <= 0) {
            return null;
        }
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        JsonArray entries = new JsonArray();
        JsonObject item = new JsonObject();
        item.addProperty("type", "minecraft:item");
        item.addProperty("name", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        item.addProperty("weight", entry.chance());
        JsonArray functions = functions(entry);
        if (!functions.isEmpty()) {
            item.add("functions", functions);
        }
        entries.add(item);
        if (entry.chance() < 100) {
            JsonObject empty = new JsonObject();
            empty.addProperty("type", "minecraft:empty");
            empty.addProperty("weight", 100 - entry.chance());
            entries.add(empty);
        }
        pool.add("entries", entries);
        return pool;
    }

    private static List<JsonObject> tagPools(LootBookData.Entry entry) {
        List<JsonObject> pools = new ArrayList<>();
        if (entry.chance() <= 0) {
            return pools;
        }
        for (Item item : TagSlipItem.selected(entry.stack())) {
            JsonObject pool = new JsonObject();
            pool.addProperty("rolls", 1);
            JsonArray entries = new JsonArray();
            JsonObject chosen = new JsonObject();
            chosen.addProperty("type", "minecraft:item");
            chosen.addProperty("name", BuiltInRegistries.ITEM.getKey(item).toString());
            chosen.addProperty("weight", entry.chance());
            ItemStack sample = new ItemStack(item);
            JsonArray functions = sample.isDamageableItem()
                    ? damageFunctions(sample)
                    : countFunctions(entry);
            if (entry.randomEnchant()) {
                JsonObject enchant = new JsonObject();
                enchant.addProperty("function", "minecraft:enchant_randomly");
                functions.add(enchant);
            }
            if (!functions.isEmpty()) {
                chosen.add("functions", functions);
            }
            entries.add(chosen);
            if (entry.chance() < 100) {
                JsonObject empty = new JsonObject();
                empty.addProperty("type", "minecraft:empty");
                empty.addProperty("weight", 100 - entry.chance());
                entries.add(empty);
            }
            pool.add("entries", entries);
            pools.add(pool);
        }
        return pools;
    }

    private static JsonArray damageFunctions(ItemStack stack) {
        JsonArray functions = new JsonArray();
        if (stack.getMaxDamage() <= 0) {
            return functions;
        }
        JsonObject damage = new JsonObject();
        damage.addProperty("function", "minecraft:set_damage");
        JsonObject range = new JsonObject();
        range.addProperty("type", "minecraft:uniform");
        range.addProperty("min", 1.0D / stack.getMaxDamage());
        range.addProperty("max", 1.0D);
        damage.add("damage", range);
        functions.add(damage);
        return functions;
    }

    private static JsonArray countFunctions(LootBookData.Entry entry) {
        JsonArray functions = new JsonArray();
        JsonObject count = new JsonObject();
        count.addProperty("function", "minecraft:set_count");
        if (entry.min() == entry.max()) {
            count.addProperty("count", entry.min());
        } else {
            JsonObject range = new JsonObject();
            range.addProperty("type", "minecraft:uniform");
            range.addProperty("min", entry.min());
            range.addProperty("max", entry.max());
            count.add("count", range);
        }
        functions.add(count);
        return functions;
    }

    private static JsonArray functions(LootBookData.Entry entry) {
        JsonArray functions = new JsonArray();
        ItemStack stack = entry.stack();
        CompoundTag saved = stack.save(new CompoundTag());
        CompoundTag tag = saved.contains("tag", Tag.TAG_COMPOUND) ? saved.getCompound("tag").copy() : null;
        boolean randomDamage = stack.isDamageableItem() && entry.randomDamage() && stack.getMaxDamage() > 0;
        if (randomDamage && tag != null) {
            tag.remove("Damage");
        }
        if (tag != null && !tag.isEmpty()) {
            JsonObject nbt = new JsonObject();
            nbt.addProperty("function", "minecraft:set_nbt");
            nbt.addProperty("tag", tag.toString());
            functions.add(nbt);
        }
        if (randomDamage) {
            JsonObject damage = new JsonObject();
            damage.addProperty("function", "minecraft:set_damage");
            JsonObject range = new JsonObject();
            range.addProperty("type", "minecraft:uniform");
            range.addProperty("min", 1.0D / stack.getMaxDamage());
            range.addProperty("max", 1.0D);
            damage.add("damage", range);
            functions.add(damage);
        } else if (!stack.isDamageableItem()) {
            JsonObject count = new JsonObject();
            count.addProperty("function", "minecraft:set_count");
            if (entry.min() == entry.max()) {
                count.addProperty("count", entry.min());
            } else {
                JsonObject range = new JsonObject();
                range.addProperty("type", "minecraft:uniform");
                range.addProperty("min", entry.min());
                range.addProperty("max", entry.max());
                count.add("count", range);
            }
            functions.add(count);
        }
        if (entry.randomEnchant()) {
            JsonObject enchant = new JsonObject();
            enchant.addProperty("function", "minecraft:enchant_randomly");
            functions.add(enchant);
        }
        return functions;
    }
}
