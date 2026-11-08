package cts.yblk.cts.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import cts.yblk.cts.item.ChestPropItem;
import cts.yblk.cts.item.ConversionWandItem;
import cts.yblk.cts.item.HarvestTreeItem;
import cts.yblk.cts.item.LootBookItem;
import cts.yblk.cts.item.TagSlipItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import cts.yblk.cts.Cts;

import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Cts.MODID);

    public static final RegistryObject<Item> LOOT_PLATE = ITEMS.register("loot_plate",
            () -> new BlockItem(ModBlocks.LOOT_PLATE.get(), new Item.Properties()));

    public static final RegistryObject<Item> CAN_STASH = ITEMS.register("can_stash",
            () -> new BlockItem(ModBlocks.CAN_STASH.get(), new Item.Properties()));

    public static final RegistryObject<Item> MEDKIT = ITEMS.register("medkit",
            () -> new BlockItem(ModBlocks.MEDKIT.get(), new Item.Properties()));

    public static final RegistryObject<Item> WASTE_DRUM = ITEMS.register("waste_drum",
            () -> new BlockItem(ModBlocks.WASTE_DRUM.get(), new Item.Properties()));

    public static final RegistryObject<Item> BOXES = ITEMS.register("boxes",
            () -> new BlockItem(ModBlocks.BOXES.get(), new Item.Properties()));

    public static final RegistryObject<Item> AMMO_BOX = ITEMS.register("ammo_box",
            () -> new BlockItem(ModBlocks.AMMO_BOX.get(), new Item.Properties()));

    public static final RegistryObject<Item> DIRTY_BACKPACK = ITEMS.register("dirty_backpack",
            () -> new BlockItem(ModBlocks.DIRTY_BACKPACK.get(), new Item.Properties()));

    public static final RegistryObject<Item> CLOTHES = ITEMS.register("clothes",
            () -> new BlockItem(ModBlocks.CLOTHES.get(), new Item.Properties()));

    public static final RegistryObject<Item> TOOLBOX = ITEMS.register("toolbox",
            () -> new BlockItem(ModBlocks.TOOLBOX.get(), new Item.Properties()));

    public static final RegistryObject<Item> CRATE = ITEMS.register("crate",
            () -> new BlockItem(ModBlocks.CRATE.get(), new Item.Properties()));

    public static final RegistryObject<Item> WEAPON_CASE = ITEMS.register("weapon_case",
            () -> new BlockItem(ModBlocks.WEAPON_CASE.get(), new Item.Properties()));

    public static final RegistryObject<Item> SAFE = ITEMS.register("safe",
            () -> new BlockItem(ModBlocks.SAFE.get(), new Item.Properties()));

    public static final RegistryObject<Item> METAL_SCRAP = ITEMS.register("metal_scrap",
            () -> new BlockItem(ModBlocks.METAL_SCRAP.get(), new Item.Properties()));

    public static final RegistryObject<Item> TRASH_CAN = ITEMS.register("trashcan",
            () -> new BlockItem(ModBlocks.TRASH_CAN.get(), new Item.Properties()));

    public static final RegistryObject<Item> LITTER = ITEMS.register("litter",
            () -> new BlockItem(ModBlocks.LITTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> NEST = ITEMS.register("nest",
            () -> new BlockItem(ModBlocks.NEST.get(), new Item.Properties()));

    public static final RegistryObject<Item> SHELVING = ITEMS.register("shelving",
            () -> new BlockItem(ModBlocks.SHELVING.get(), new Item.Properties()));

    public static final RegistryObject<Item> CASH_REGISTER = ITEMS.register("cash_register",
            () -> new BlockItem(ModBlocks.CASH_REGISTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> MEDICAL_CRATE = ITEMS.register("medical_crate",
            () -> new BlockItem(ModBlocks.MEDICAL_CRATE.get(), new Item.Properties()));

    public static final RegistryObject<Item> LAUNDRY_BASKET = ITEMS.register("laundry_basket",
            () -> new BlockItem(ModBlocks.LAUNDRY_BASKET.get(), new Item.Properties()));

    public static final RegistryObject<Item> TOILET = ITEMS.register("toilet",
            () -> new BlockItem(ModBlocks.TOILET.get(), new Item.Properties()));

    public static final RegistryObject<Item> FRIDGE = ITEMS.register("fridge",
            () -> new BlockItem(ModBlocks.FRIDGE.get(), new Item.Properties()));

    public static final RegistryObject<Item> WASHING_MACHINE = ITEMS.register("washing_machine",
            () -> new BlockItem(ModBlocks.WASHING_MACHINE.get(), new Item.Properties()));

    public static final RegistryObject<Item> MICROWAVE = ITEMS.register("microwave",
            () -> new BlockItem(ModBlocks.MICROWAVE.get(), new Item.Properties()));

    public static final RegistryObject<Item> CHEST = ITEMS.register("chest",
            () -> new ChestPropItem(ModBlocks.CHEST.get(), new Item.Properties()));

    public static final RegistryObject<Item> CORPSE = ITEMS.register("corpse",
            () -> new BlockItem(ModBlocks.CORPSE.get(), new Item.Properties()));

    public static final RegistryObject<Item> AIRDROP = ITEMS.register("airdrop",
            () -> new BlockItem(ModBlocks.AIRDROP.get(), new Item.Properties()));

    public static final RegistryObject<Item> CONVERSION_WAND = ITEMS.register("conversion_wand",
            () -> new ConversionWandItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRESSER_OAK = dresser(ModBlocks.DRESSER_OAK);
    public static final RegistryObject<Item> DRESSER_SPRUCE = dresser(ModBlocks.DRESSER_SPRUCE);
    public static final RegistryObject<Item> DRESSER_BIRCH = dresser(ModBlocks.DRESSER_BIRCH);
    public static final RegistryObject<Item> DRESSER_JUNGLE = dresser(ModBlocks.DRESSER_JUNGLE);
    public static final RegistryObject<Item> DRESSER_ACACIA = dresser(ModBlocks.DRESSER_ACACIA);
    public static final RegistryObject<Item> DRESSER_DARK_OAK = dresser(ModBlocks.DRESSER_DARK_OAK);
    public static final RegistryObject<Item> DRESSER_MANGROVE = dresser(ModBlocks.DRESSER_MANGROVE);
    public static final RegistryObject<Item> DRESSER_CHERRY = dresser(ModBlocks.DRESSER_CHERRY);
    public static final RegistryObject<Item> DRESSER_PALE_OAK = dresser(ModBlocks.DRESSER_PALE_OAK);
    public static final List<RegistryObject<Item>> DRESSERS = List.of(
            DRESSER_OAK, DRESSER_SPRUCE, DRESSER_BIRCH, DRESSER_JUNGLE, DRESSER_ACACIA,
            DRESSER_DARK_OAK, DRESSER_MANGROVE, DRESSER_CHERRY, DRESSER_PALE_OAK);

    private static RegistryObject<Item> dresser(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static final RegistryObject<Item> KITCHEN_DRAWER_OAK = blockItem(ModBlocks.KITCHEN_DRAWER_OAK);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SPRUCE = blockItem(ModBlocks.KITCHEN_DRAWER_SPRUCE);
    public static final RegistryObject<Item> KITCHEN_DRAWER_BIRCH = blockItem(ModBlocks.KITCHEN_DRAWER_BIRCH);
    public static final RegistryObject<Item> KITCHEN_DRAWER_JUNGLE = blockItem(ModBlocks.KITCHEN_DRAWER_JUNGLE);
    public static final RegistryObject<Item> KITCHEN_DRAWER_ACACIA = blockItem(ModBlocks.KITCHEN_DRAWER_ACACIA);
    public static final RegistryObject<Item> KITCHEN_DRAWER_DARK_OAK = blockItem(ModBlocks.KITCHEN_DRAWER_DARK_OAK);
    public static final RegistryObject<Item> KITCHEN_DRAWER_MANGROVE = blockItem(ModBlocks.KITCHEN_DRAWER_MANGROVE);
    public static final RegistryObject<Item> KITCHEN_DRAWER_CHERRY = blockItem(ModBlocks.KITCHEN_DRAWER_CHERRY);
    public static final RegistryObject<Item> KITCHEN_DRAWER_PALE_OAK = blockItem(ModBlocks.KITCHEN_DRAWER_PALE_OAK);
    public static final List<RegistryObject<Item>> KITCHEN_DRAWERS = List.of(
            KITCHEN_DRAWER_OAK, KITCHEN_DRAWER_SPRUCE, KITCHEN_DRAWER_BIRCH, KITCHEN_DRAWER_JUNGLE, KITCHEN_DRAWER_ACACIA,
            KITCHEN_DRAWER_DARK_OAK, KITCHEN_DRAWER_MANGROVE, KITCHEN_DRAWER_CHERRY, KITCHEN_DRAWER_PALE_OAK);

    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_OAK = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_OAK);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_SPRUCE = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_SPRUCE);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_BIRCH = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_BIRCH);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_JUNGLE = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_JUNGLE);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_ACACIA = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_ACACIA);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_DARK_OAK = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_DARK_OAK);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_MANGROVE = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_MANGROVE);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_CHERRY = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_CHERRY);
    public static final RegistryObject<Item> KITCHEN_DRAWER_SINGLE_PALE_OAK = blockItem(ModBlocks.KITCHEN_DRAWER_SINGLE_PALE_OAK);
    public static final List<RegistryObject<Item>> KITCHEN_DRAWER_SINGLES = List.of(
            KITCHEN_DRAWER_SINGLE_OAK, KITCHEN_DRAWER_SINGLE_SPRUCE, KITCHEN_DRAWER_SINGLE_BIRCH, KITCHEN_DRAWER_SINGLE_JUNGLE,
            KITCHEN_DRAWER_SINGLE_ACACIA, KITCHEN_DRAWER_SINGLE_DARK_OAK, KITCHEN_DRAWER_SINGLE_MANGROVE,
            KITCHEN_DRAWER_SINGLE_CHERRY, KITCHEN_DRAWER_SINGLE_PALE_OAK);

    public static final RegistryObject<Item> BRICK_CACHE = blockItem(ModBlocks.BRICK_CACHE);
    public static final RegistryObject<Item> STONE_BRICK_CACHE = blockItem(ModBlocks.STONE_BRICK_CACHE);
    public static final List<RegistryObject<Item>> BRICK_CACHES = List.of(BRICK_CACHE, STONE_BRICK_CACHE);

    private static RegistryObject<Item> blockItem(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static final RegistryObject<Item> ENTITY_PLATE = ITEMS.register("entity_plate",
            () -> new BlockItem(ModBlocks.ENTITY_PLATE.get(), new Item.Properties()));

    public static final RegistryObject<Item> BARRICADE = ITEMS.register("barricade",
            () -> new BlockItem(ModBlocks.BARRICADE.get(), new Item.Properties()));

    public static final RegistryObject<Item> VENTILATION_GRILLE = ITEMS.register("ventilation_grille",
            () -> new BlockItem(ModBlocks.VENTILATION_GRILLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> METAL_ORE = ITEMS.register("metal_ore",
            () -> new BlockItem(ModBlocks.METAL_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARVEST_TREE = ITEMS.register("harvest_tree",
            () -> new HarvestTreeItem(ModBlocks.HARVEST_TREE.get(),
                    cts.yblk.cts.block.HarvestTreeStructure.Size.MEDIUM, new Item.Properties()));

    public static final RegistryObject<Item> HARVEST_TREE_LARGE = ITEMS.register("harvest_tree_large",
            () -> new HarvestTreeItem(ModBlocks.HARVEST_TREE.get(),
                    cts.yblk.cts.block.HarvestTreeStructure.Size.LARGE, new Item.Properties()));

    public static final RegistryObject<Item> LOOT_BOOK = ITEMS.register("loot_book",
            () -> new LootBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TAG_SLIP = ITEMS.register("tag_slip",
            () -> new TagSlipItem(new Item.Properties()));
}
