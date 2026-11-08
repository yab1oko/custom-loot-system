package cts.yblk.cts.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import cts.yblk.cts.Cts;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModMenus;
import cts.yblk.cts.registry.ModParticles;

@Mod.EventBusSubscriber(modid = Cts.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.LOOT_PLATE.get(), LootPlateScreen::new);
            MenuScreens.register(ModMenus.CAN_STASH.get(), CanStashScreen::new);
            MenuScreens.register(ModMenus.CAN_STASH_CONFIG.get(), CanStashConfigScreen::new);
            MenuScreens.register(ModMenus.ENTITY_PLATE.get(), EntityPlateScreen::new);
            MenuScreens.register(ModMenus.LOOT_BOOK.get(), LootBookScreen::new);
            MenuScreens.register(ModMenus.BARRICADE_CONFIG.get(), BarricadeConfigScreen::new);
            MenuScreens.register(ModMenus.VENTILATION_GRILLE_CONFIG.get(), VentilationGrilleConfigScreen::new);
            MenuScreens.register(ModMenus.METAL_ORE_CONFIG.get(), MetalOreConfigScreen::new);
            MenuScreens.register(ModMenus.HARVEST_TREE_CONFIG.get(), HarvestTreeConfigScreen::new);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CAN_STASH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MEDKIT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WASTE_DRUM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BOXES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AMMO_BOX.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIRTY_BACKPACK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CLOTHES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TOOLBOX.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRATE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WEAPON_CASE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SAFE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.METAL_SCRAP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRASH_CAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LITTER.get(), RenderType.cutout());
            ModBlocks.DRESSERS.forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
            ModBlocks.KITCHEN_DRAWERS.forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
            ModBlocks.KITCHEN_DRAWER_SINGLES.forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.NEST.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHELVING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CASH_REGISTER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MEDICAL_CRATE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LAUNDRY_BASKET.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TOILET.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FRIDGE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WASHING_MACHINE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MICROWAVE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHEST.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CORPSE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AIRDROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AIRDROP_PART.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CONVERTED_CONTAINER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BARRICADE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENTILATION_GRILLE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HARVEST_TREE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HARVEST_TREE_TRUNK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HARVEST_TREE_BRANCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HARVEST_TREE_LEAVES.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) ->
                        level != null && pos != null
                                ? BiomeColors.getAverageFoliageColor(level, pos)
                                : FoliageColor.getDefaultColor(),
                ModBlocks.HARVEST_TREE_LEAVES.get());
    }

    private static final String[] HINGED_PROPS = {
            "weapon_case", "toolbox", "medkit", "toilet", "ammo_box",
            "safe", "microwave", "fridge", "washing_machine", "medical_crate",
            "crate", "waste_drum"
    };

    private static final String[] WOODS = {
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak"
    };

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(Cts.MODID, "block/barricade_1"));
        event.register(new ResourceLocation(Cts.MODID, "block/barricade_2"));
        event.register(new ResourceLocation(Cts.MODID, "block/barricade_3"));
        for (String name : HINGED_PROPS) {
            event.register(new ResourceLocation(Cts.MODID, "block/" + name + "_body"));
            event.register(new ResourceLocation(Cts.MODID, "block/" + name + "_lid"));
        }
        event.register(new ResourceLocation(Cts.MODID, "block/cash_register_body"));
        event.register(new ResourceLocation(Cts.MODID, "block/cash_register_drawer"));
        for (String wood : WOODS) {
            event.register(new ResourceLocation(Cts.MODID, "block/dresser_" + wood + "_body"));
            event.register(new ResourceLocation(Cts.MODID, "block/dresser_" + wood + "_drawer_1"));
            event.register(new ResourceLocation(Cts.MODID, "block/dresser_" + wood + "_drawer_2"));
            event.register(new ResourceLocation(Cts.MODID, "block/kitchen_drawer_" + wood + "_body"));
            event.register(new ResourceLocation(Cts.MODID, "block/kitchen_drawer_" + wood + "_door_1"));
            event.register(new ResourceLocation(Cts.MODID, "block/kitchen_drawer_" + wood + "_door_2"));
            event.register(new ResourceLocation(Cts.MODID, "block/kitchen_drawer_single_" + wood + "_body"));
            event.register(new ResourceLocation(Cts.MODID, "block/kitchen_drawer_single_" + wood + "_door"));
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LOOT_PLATE.get(), LootPlateRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ENTITY_PLATE.get(), EntityPlateRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHEST.get(), ChestPropRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CONVERTED_CONTAINER.get(), ConvertedContainerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AIRDROP.get(), AirdropBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BARRICADE.get(), BarricadeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DRESSER.get(), DresserRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CASH_REGISTER.get(), CashRegisterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KITCHEN_DRAWER.get(), KitchenCabinetRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.AMMO_BOX.get(), AmmoBoxRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WEAPON_CASE.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("weapon_case", 8, 5, 2, 'x', -22.5F)));
        // Model authored opposite hitbox — rotate BER 180° so latches match collision.
        event.registerBlockEntityRenderer(ModBlockEntities.TOOLBOX.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("toolbox", 8, 6, 11.5F, 'x', 22.5F, 180.0F, false)));
        event.registerBlockEntityRenderer(ModBlockEntities.MEDKIT.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("medkit", 7, 2, 4, 'x', -22.5F)));
        event.registerBlockEntityRenderer(ModBlockEntities.TOILET.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("toilet", 7, 9, 4, 'x', -90.0F)));
        event.registerBlockEntityRenderer(ModBlockEntities.SAFE.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("safe", 13, 7, 14, 'y', 22.5F)));
        event.registerBlockEntityRenderer(ModBlockEntities.MICROWAVE.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("microwave", 15, 1, 11, 'y', 22.5F, 0.0F, true)));
        event.registerBlockEntityRenderer(ModBlockEntities.FRIDGE.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("fridge", 15, 22, 14, 'y', 22.5F)));
        event.registerBlockEntityRenderer(ModBlockEntities.WASHING_MACHINE.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("washing_machine", 14, 8, 15, 'y', 22.5F, 0.0F, true)));
        event.registerBlockEntityRenderer(ModBlockEntities.MEDICAL_CRATE.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("medical_crate", 9.6F, 8, 9.9F, 'y', 22.5F)));
        event.registerBlockEntityRenderer(ModBlockEntities.CRATE.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("crate", 8.2F, 14.1F, 9.5F, 'y', -22.5F)));
        event.registerBlockEntityRenderer(ModBlockEntities.WASTE_DRUM.get(),
                ctx -> new HingedPropRenderer(ctx, HingedPropSpec.of("waste_drum", 8, 26, 8, 'y', 22.5F)));

        event.registerEntityRenderer(cts.yblk.cts.registry.ModEntities.AIRDROP.get(), AirdropEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(cts.yblk.cts.client.model.AirdropFlyingModel.LAYER_LOCATION,
                cts.yblk.cts.client.model.AirdropFlyingModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.AIRDROP_SMOKE.get(), AirdropSmokeParticle.Provider::new);
    }
}
