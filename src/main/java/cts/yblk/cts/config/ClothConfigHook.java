package cts.yblk.cts.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cts.yblk.cts.Cts;

/**
 * Registers the in-game Config button.
 * <p>
 * Cloth Config's Forge jar does not remap correctly in ForgeGradle userdev
 * ({@code NoSuchMethodError: Component.m_237115_}). Use Cloth only in production;
 * otherwise fall back to {@link CtsConfigScreen}.
 */
@Mod.EventBusSubscriber(modid = Cts.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClothConfigHook {
    private static final String CLOTH = "cloth_config";
    private static final Logger LOGGER = LogManager.getLogger();

    private ClothConfigHook() {
    }

    public static boolean isClothAvailable() {
        return ModList.get().isLoaded(CLOTH);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> createScreen(parent))));
    }

    private static Screen createScreen(Screen parent) {
        if (FMLEnvironment.production && isClothAvailable()) {
            try {
                return ClothConfigScreens.create(parent);
            } catch (LinkageError | RuntimeException e) {
                LOGGER.warn("Cloth Config failed; using built-in CTS config screen", e);
            }
        }
        return new CtsConfigScreen(parent);
    }
}
