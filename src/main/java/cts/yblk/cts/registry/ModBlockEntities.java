package cts.yblk.cts.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import cts.yblk.cts.Cts;
import cts.yblk.cts.blockentity.BarricadeBlockEntity;
import cts.yblk.cts.blockentity.HarvestTreeBlockEntity;
import cts.yblk.cts.blockentity.MetalOreBlockEntity;
import cts.yblk.cts.blockentity.VentilationGrilleBlockEntity;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.ConvertedContainerBlockEntity;
import cts.yblk.cts.blockentity.EntityPlateBlockEntity;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Cts.MODID);

    public static final RegistryObject<BlockEntityType<LootPlateBlockEntity>> LOOT_PLATE =
            BLOCK_ENTITIES.register("loot_plate", () ->
                    BlockEntityType.Builder.of(LootPlateBlockEntity::new, ModBlocks.LOOT_PLATE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> CAN_STASH =
            BLOCK_ENTITIES.register("can_stash", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.CAN_STASH.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> MEDKIT =
            BLOCK_ENTITIES.register("medkit", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.MEDKIT.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> WASTE_DRUM =
            BLOCK_ENTITIES.register("waste_drum", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.WASTE_DRUM.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> BOXES =
            BLOCK_ENTITIES.register("boxes", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.BOXES.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> AMMO_BOX =
            BLOCK_ENTITIES.register("ammo_box", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.AMMO_BOX.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> DIRTY_BACKPACK =
            BLOCK_ENTITIES.register("dirty_backpack", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.DIRTY_BACKPACK.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> CLOTHES =
            BLOCK_ENTITIES.register("clothes", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.CLOTHES.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> TOOLBOX =
            BLOCK_ENTITIES.register("toolbox", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.TOOLBOX.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> CRATE =
            BLOCK_ENTITIES.register("crate", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.CRATE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> WEAPON_CASE =
            BLOCK_ENTITIES.register("weapon_case", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.WEAPON_CASE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> SAFE =
            BLOCK_ENTITIES.register("safe", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.SAFE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> METAL_SCRAP =
            BLOCK_ENTITIES.register("metal_scrap", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.METAL_SCRAP.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> TRASH_CAN =
            BLOCK_ENTITIES.register("trashcan", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.TRASH_CAN.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> DRESSER =
            BLOCK_ENTITIES.register("dresser", () -> {
                Block[] blocks = ModBlocks.DRESSERS.stream().map(RegistryObject::get).toArray(Block[]::new);
                return BlockEntityType.Builder.of(CanStashBlockEntity::new, blocks).build(null);
            });

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> KITCHEN_DRAWER =
            BLOCK_ENTITIES.register("kitchen_drawer", () -> {
                Block[] drawers = ModBlocks.KITCHEN_DRAWERS.stream().map(RegistryObject::get).toArray(Block[]::new);
                Block[] singles = ModBlocks.KITCHEN_DRAWER_SINGLES.stream().map(RegistryObject::get).toArray(Block[]::new);
                Block[] blocks = new Block[drawers.length + singles.length];
                System.arraycopy(drawers, 0, blocks, 0, drawers.length);
                System.arraycopy(singles, 0, blocks, drawers.length, singles.length);
                return BlockEntityType.Builder.of(CanStashBlockEntity::new, blocks).build(null);
            });

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> BRICK_CACHE =
            BLOCK_ENTITIES.register("brick_cache", () -> {
                Block[] blocks = ModBlocks.BRICK_CACHES.stream().map(RegistryObject::get).toArray(Block[]::new);
                return BlockEntityType.Builder.of(CanStashBlockEntity::new, blocks).build(null);
            });

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> LITTER =
            BLOCK_ENTITIES.register("litter", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.LITTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> NEST =
            BLOCK_ENTITIES.register("nest", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.NEST.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> SHELVING =
            BLOCK_ENTITIES.register("shelving", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.SHELVING.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> CASH_REGISTER =
            BLOCK_ENTITIES.register("cash_register", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.CASH_REGISTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> MEDICAL_CRATE =
            BLOCK_ENTITIES.register("medical_crate", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.MEDICAL_CRATE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> LAUNDRY_BASKET =
            BLOCK_ENTITIES.register("laundry_basket", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.LAUNDRY_BASKET.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> TOILET =
            BLOCK_ENTITIES.register("toilet", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.TOILET.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> FRIDGE =
            BLOCK_ENTITIES.register("fridge", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.FRIDGE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> WASHING_MACHINE =
            BLOCK_ENTITIES.register("washing_machine", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.WASHING_MACHINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> MICROWAVE =
            BLOCK_ENTITIES.register("microwave", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.MICROWAVE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> CHEST =
            BLOCK_ENTITIES.register("chest", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.CHEST.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> CORPSE =
            BLOCK_ENTITIES.register("corpse", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.CORPSE.get()).build(null));

    public static final RegistryObject<BlockEntityType<CanStashBlockEntity>> AIRDROP =
            BLOCK_ENTITIES.register("airdrop", () ->
                    BlockEntityType.Builder.of(CanStashBlockEntity::new, ModBlocks.AIRDROP.get()).build(null));

    public static final RegistryObject<BlockEntityType<ConvertedContainerBlockEntity>> CONVERTED_CONTAINER =
            BLOCK_ENTITIES.register("converted_container", () ->
                    BlockEntityType.Builder.of(ConvertedContainerBlockEntity::new, ModBlocks.CONVERTED_CONTAINER.get()).build(null));

    public static final RegistryObject<BlockEntityType<EntityPlateBlockEntity>> ENTITY_PLATE =
            BLOCK_ENTITIES.register("entity_plate", () ->
                    BlockEntityType.Builder.of(EntityPlateBlockEntity::new, ModBlocks.ENTITY_PLATE.get()).build(null));

    public static final RegistryObject<BlockEntityType<BarricadeBlockEntity>> BARRICADE =
            BLOCK_ENTITIES.register("barricade", () ->
                    BlockEntityType.Builder.of(BarricadeBlockEntity::new, ModBlocks.BARRICADE.get()).build(null));

    public static final RegistryObject<BlockEntityType<VentilationGrilleBlockEntity>> VENTILATION_GRILLE =
            BLOCK_ENTITIES.register("ventilation_grille", () ->
                    BlockEntityType.Builder.of(VentilationGrilleBlockEntity::new, ModBlocks.VENTILATION_GRILLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<MetalOreBlockEntity>> METAL_ORE =
            BLOCK_ENTITIES.register("metal_ore", () ->
                    BlockEntityType.Builder.of(MetalOreBlockEntity::new, ModBlocks.METAL_ORE.get()).build(null));

    public static final RegistryObject<BlockEntityType<HarvestTreeBlockEntity>> HARVEST_TREE =
            BLOCK_ENTITIES.register("harvest_tree", () ->
                    BlockEntityType.Builder.of(HarvestTreeBlockEntity::new, ModBlocks.HARVEST_TREE.get()).build(null));
}
