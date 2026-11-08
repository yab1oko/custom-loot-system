package cts.yblk.cts.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import cts.yblk.cts.Cts;

public class ModTags {
    public static final TagKey<Block> PROP_CONTAINER = TagKey.create(Registries.BLOCK, new ResourceLocation(Cts.MODID, "prop_container"));
    public static final TagKey<Item> PROP_CONTAINER_ITEM = TagKey.create(Registries.ITEM, new ResourceLocation(Cts.MODID, "prop_container"));
    public static final TagKey<Block> CONVERTIBLE = TagKey.create(Registries.BLOCK, new ResourceLocation(Cts.MODID, "convertible"));
    /** Modded chests that should use the CTS chest lid animation when converted. */
    public static final TagKey<Block> CHEST_LIKE = TagKey.create(Registries.BLOCK, new ResourceLocation(Cts.MODID, "chest_like"));
    public static final TagKey<Block> BREAKABLE = TagKey.create(Registries.BLOCK, new ResourceLocation(Cts.MODID, "breakable"));
    public static final TagKey<Item> SCREWDRIVERS = TagKey.create(Registries.ITEM, new ResourceLocation(Cts.MODID, "screwdrivers"));
    public static final TagKey<Item> AXES = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "tools/axes"));
    public static final TagKey<Item> PICKAXES = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "tools/pickaxes"));

    private ModTags() {
    }
}
