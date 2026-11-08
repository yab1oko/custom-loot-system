package cts.yblk.cts.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import cts.yblk.cts.Cts;

import java.util.List;
import cts.yblk.cts.block.AmmoBoxBlock;
import cts.yblk.cts.block.AirdropBlock;
import cts.yblk.cts.block.BarricadeBlock;
import cts.yblk.cts.block.HarvestTreeBlock;
import cts.yblk.cts.block.HarvestTreeBranchBlock;
import cts.yblk.cts.block.HarvestTreeLeavesBlock;
import cts.yblk.cts.block.HarvestTreeTrunkBlock;
import cts.yblk.cts.block.MetalOreBlock;
import cts.yblk.cts.block.VentilationGrilleBlock;
import cts.yblk.cts.block.AirdropPartBlock;
import cts.yblk.cts.block.BoxesBlock;
import cts.yblk.cts.block.BrickCacheBlock;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.block.CashRegisterBlock;
import cts.yblk.cts.block.ChestPropBlock;
import cts.yblk.cts.block.ClothesBlock;
import cts.yblk.cts.block.ConvertedContainerBlock;
import cts.yblk.cts.block.CorpseBlock;
import cts.yblk.cts.block.CrateBlock;
import cts.yblk.cts.block.DresserBlock;
import cts.yblk.cts.block.DirtyBackpackBlock;
import cts.yblk.cts.block.EntityPlateBlock;
import cts.yblk.cts.block.FridgeBlock;
import cts.yblk.cts.block.FridgePartBlock;
import cts.yblk.cts.block.KitchenDrawerBlock;
import cts.yblk.cts.block.LaundryBasketBlock;
import cts.yblk.cts.block.LootPlateBlock;
import cts.yblk.cts.block.MedkitBlock;
import cts.yblk.cts.block.MedicalCrateBlock;
import cts.yblk.cts.block.MetalScrapBlock;
import cts.yblk.cts.block.MicrowaveBlock;
import cts.yblk.cts.block.NestBlock;
import cts.yblk.cts.block.SafeBlock;
import cts.yblk.cts.block.ShelvingBlock;
import cts.yblk.cts.block.ShelvingPartBlock;
import cts.yblk.cts.block.ToiletBlock;
import cts.yblk.cts.block.ToolboxBlock;
import cts.yblk.cts.block.LitterBlock;
import cts.yblk.cts.block.TrashCanBlock;
import cts.yblk.cts.block.WashingMachineBlock;
import cts.yblk.cts.block.WasteDrumBlock;
import cts.yblk.cts.block.WeaponCaseBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Cts.MODID);

    public static final RegistryObject<Block> LOOT_PLATE = BLOCKS.register("loot_plate",
            () -> new LootPlateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .noCollission()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> CAN_STASH = BLOCKS.register("can_stash",
            () -> new CanStashBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> MEDKIT = BLOCKS.register("medkit",
            () -> new MedkitBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> WASTE_DRUM = BLOCKS.register("waste_drum",
            () -> new WasteDrumBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> BOXES = BLOCKS.register("boxes",
            () -> new BoxesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> AMMO_BOX = BLOCKS.register("ammo_box",
            () -> new AmmoBoxBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> DIRTY_BACKPACK = BLOCKS.register("dirty_backpack",
            () -> new DirtyBackpackBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> CLOTHES = BLOCKS.register("clothes",
            () -> new ClothesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> TOOLBOX = BLOCKS.register("toolbox",
            () -> new ToolboxBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> CRATE = BLOCKS.register("crate",
            () -> new CrateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> WEAPON_CASE = BLOCKS.register("weapon_case",
            () -> new WeaponCaseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> SAFE = BLOCKS.register("safe",
            () -> new SafeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> METAL_SCRAP = BLOCKS.register("metal_scrap",
            () -> new MetalScrapBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> TRASH_CAN = BLOCKS.register("trashcan",
            () -> new TrashCanBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> LITTER = BLOCKS.register("litter",
            () -> new LitterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> NEST = BLOCKS.register("nest",
            () -> new NestBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> SHELVING = BLOCKS.register("shelving",
            () -> new ShelvingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> SHELVING_PART = BLOCKS.register("shelving_part",
            () -> new ShelvingPartBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> CASH_REGISTER = BLOCKS.register("cash_register",
            () -> new CashRegisterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> MEDICAL_CRATE = BLOCKS.register("medical_crate",
            () -> new MedicalCrateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> LAUNDRY_BASKET = BLOCKS.register("laundry_basket",
            () -> new LaundryBasketBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> TOILET = BLOCKS.register("toilet",
            () -> new ToiletBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> FRIDGE = BLOCKS.register("fridge",
            () -> new FridgeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> FRIDGE_PART = BLOCKS.register("fridge_part",
            () -> new FridgePartBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> WASHING_MACHINE = BLOCKS.register("washing_machine",
            () -> new WashingMachineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> MICROWAVE = BLOCKS.register("microwave",
            () -> new MicrowaveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> CHEST = BLOCKS.register("chest",
            () -> new ChestPropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> CORPSE = BLOCKS.register("corpse",
            () -> new CorpseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> AIRDROP = BLOCKS.register("airdrop",
            () -> new AirdropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> AIRDROP_PART = BLOCKS.register("airdrop_part",
            () -> new AirdropPartBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> CONVERTED_CONTAINER = BLOCKS.register("converted_container",
            () -> new ConvertedContainerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> DRESSER_OAK = dresser("dresser_oak");
    public static final RegistryObject<Block> DRESSER_SPRUCE = dresser("dresser_spruce");
    public static final RegistryObject<Block> DRESSER_BIRCH = dresser("dresser_birch");
    public static final RegistryObject<Block> DRESSER_JUNGLE = dresser("dresser_jungle");
    public static final RegistryObject<Block> DRESSER_ACACIA = dresser("dresser_acacia");
    public static final RegistryObject<Block> DRESSER_DARK_OAK = dresser("dresser_dark_oak");
    public static final RegistryObject<Block> DRESSER_MANGROVE = dresser("dresser_mangrove");
    public static final RegistryObject<Block> DRESSER_CHERRY = dresser("dresser_cherry");
    public static final RegistryObject<Block> DRESSER_PALE_OAK = dresser("dresser_pale_oak");
    public static final List<RegistryObject<Block>> DRESSERS = List.of(
            DRESSER_OAK, DRESSER_SPRUCE, DRESSER_BIRCH, DRESSER_JUNGLE, DRESSER_ACACIA,
            DRESSER_DARK_OAK, DRESSER_MANGROVE, DRESSER_CHERRY, DRESSER_PALE_OAK);

    private static RegistryObject<Block> dresser(String name) {
        return BLOCKS.register(name, () -> new DresserBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion()
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.BLOCK)));
    }

    public static final RegistryObject<Block> KITCHEN_DRAWER_OAK = kitchenDrawer("kitchen_drawer_oak", false);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SPRUCE = kitchenDrawer("kitchen_drawer_spruce", false);
    public static final RegistryObject<Block> KITCHEN_DRAWER_BIRCH = kitchenDrawer("kitchen_drawer_birch", false);
    public static final RegistryObject<Block> KITCHEN_DRAWER_JUNGLE = kitchenDrawer("kitchen_drawer_jungle", false);
    public static final RegistryObject<Block> KITCHEN_DRAWER_ACACIA = kitchenDrawer("kitchen_drawer_acacia", false);
    public static final RegistryObject<Block> KITCHEN_DRAWER_DARK_OAK = kitchenDrawer("kitchen_drawer_dark_oak", false);
    public static final RegistryObject<Block> KITCHEN_DRAWER_MANGROVE = kitchenDrawer("kitchen_drawer_mangrove", false);
    public static final RegistryObject<Block> KITCHEN_DRAWER_CHERRY = kitchenDrawer("kitchen_drawer_cherry", false);
    public static final RegistryObject<Block> KITCHEN_DRAWER_PALE_OAK = kitchenDrawer("kitchen_drawer_pale_oak", false);
    public static final List<RegistryObject<Block>> KITCHEN_DRAWERS = List.of(
            KITCHEN_DRAWER_OAK, KITCHEN_DRAWER_SPRUCE, KITCHEN_DRAWER_BIRCH, KITCHEN_DRAWER_JUNGLE, KITCHEN_DRAWER_ACACIA,
            KITCHEN_DRAWER_DARK_OAK, KITCHEN_DRAWER_MANGROVE, KITCHEN_DRAWER_CHERRY, KITCHEN_DRAWER_PALE_OAK);

    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_OAK = kitchenDrawer("kitchen_drawer_single_oak", true);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_SPRUCE = kitchenDrawer("kitchen_drawer_single_spruce", true);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_BIRCH = kitchenDrawer("kitchen_drawer_single_birch", true);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_JUNGLE = kitchenDrawer("kitchen_drawer_single_jungle", true);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_ACACIA = kitchenDrawer("kitchen_drawer_single_acacia", true);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_DARK_OAK = kitchenDrawer("kitchen_drawer_single_dark_oak", true);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_MANGROVE = kitchenDrawer("kitchen_drawer_single_mangrove", true);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_CHERRY = kitchenDrawer("kitchen_drawer_single_cherry", true);
    public static final RegistryObject<Block> KITCHEN_DRAWER_SINGLE_PALE_OAK = kitchenDrawer("kitchen_drawer_single_pale_oak", true);
    public static final List<RegistryObject<Block>> KITCHEN_DRAWER_SINGLES = List.of(
            KITCHEN_DRAWER_SINGLE_OAK, KITCHEN_DRAWER_SINGLE_SPRUCE, KITCHEN_DRAWER_SINGLE_BIRCH, KITCHEN_DRAWER_SINGLE_JUNGLE,
            KITCHEN_DRAWER_SINGLE_ACACIA, KITCHEN_DRAWER_SINGLE_DARK_OAK, KITCHEN_DRAWER_SINGLE_MANGROVE,
            KITCHEN_DRAWER_SINGLE_CHERRY, KITCHEN_DRAWER_SINGLE_PALE_OAK);

    private static RegistryObject<Block> kitchenDrawer(String name, boolean single) {
        return BLOCKS.register(name, () -> new KitchenDrawerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion()
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.BLOCK), single));
    }

    public static final RegistryObject<Block> BRICK_CACHE = BLOCKS.register("brick_cache",
            () -> new BrickCacheBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> STONE_BRICK_CACHE = BLOCKS.register("stone_brick_cache",
            () -> new BrickCacheBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.BLOCK)));

    public static final List<RegistryObject<Block>> BRICK_CACHES = List.of(BRICK_CACHE, STONE_BRICK_CACHE);

    public static final RegistryObject<Block> ENTITY_PLATE = BLOCKS.register("entity_plate",
            () -> new EntityPlateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .noCollission()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> BARRICADE = BLOCKS.register("barricade",
            () -> new BarricadeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> VENTILATION_GRILLE = BLOCKS.register("ventilation_grille",
            () -> new VentilationGrilleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> METAL_ORE = BLOCKS.register("metal_ore",
            () -> new MetalOreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> HARVEST_TREE = BLOCKS.register("harvest_tree",
            () -> new HarvestTreeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> HARVEST_TREE_TRUNK = BLOCKS.register("harvest_tree_trunk",
            () -> new HarvestTreeTrunkBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> HARVEST_TREE_BRANCH = BLOCKS.register("harvest_tree_branch",
            () -> new HarvestTreeBranchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> HARVEST_TREE_LEAVES = BLOCKS.register("harvest_tree_leaves",
            () -> new HarvestTreeLeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.BLOCK)));
}
