package cts.yblk.cts;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import cts.yblk.cts.config.CtsConfig;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModCreativeTabs;
import cts.yblk.cts.registry.ModEntities;
import cts.yblk.cts.registry.ModItems;
import cts.yblk.cts.registry.ModMenus;
import cts.yblk.cts.registry.ModParticles;
import cts.yblk.cts.registry.ModSounds;

@Mod(Cts.MODID)
public class Cts {
    public static final String MODID = "cts";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Cts() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CtsConfig.SPEC);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
