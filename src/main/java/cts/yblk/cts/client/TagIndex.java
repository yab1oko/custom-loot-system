package cts.yblk.cts.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TagIndex {
    private static List<String> ids;

    private TagIndex() {
    }

    public static List<String> ids() {
        if (ids == null || ids.isEmpty()) {
            List<String> found = new ArrayList<>();
            for (TagKey<Item> key : BuiltInRegistries.ITEM.getTagNames().toList()) {
                found.add(key.location().toString());
            }
            for (TagKey<Block> key : BuiltInRegistries.BLOCK.getTagNames().toList()) {
                String id = key.location().toString();
                if (!found.contains(id)) {
                    found.add(id);
                }
            }
            if (found.isEmpty()) {
                return List.of();
            }
            Collections.sort(found);
            ids = List.copyOf(found);
        }
        return ids;
    }
}
