package cts.yblk.cts.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityIdIndex {
    private static List<String> ids;

    private EntityIdIndex() {
    }

    public static List<String> ids() {
        if (ids == null) {
            List<String> found = new ArrayList<>();
            for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES) {
                ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
                if (key != null && type.canSummon()) {
                    found.add(key.toString());
                }
            }
            Collections.sort(found);
            ids = List.copyOf(found);
        }
        return ids;
    }
}
