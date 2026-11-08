package cts.yblk.cts.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.Minecraft;
import cts.yblk.cts.Cts;

/**
 * Soft-dep Player Animator poses for hold actions (search / chop / mine / screwdriver).
 * <p>
 * First-person mode adapts to Features Real First Person: with RFP on, poses apply
 * through RFP's body pass ({@code FirstPersonMode.DISABLED}); without RFP, PA draws
 * third-person arms in FP ({@code THIRD_PERSON_MODEL}).
 */
@Mod.EventBusSubscriber(modid = Cts.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SearchAnimation {
    private static final String PLAYER_ANIMATOR = "playeranimator";
    private static Boolean loaded;
    private static boolean bodySyncActive;

    public static final ResourceLocation RUMMAGE = new ResourceLocation(Cts.MODID, "rummage");
    public static final ResourceLocation CHOP = new ResourceLocation(Cts.MODID, "axe_chop");
    public static final ResourceLocation MINE = new ResourceLocation(Cts.MODID, "mine");
    public static final ResourceLocation SCREWDRIVER = new ResourceLocation(Cts.MODID, "screwdriver");

    private SearchAnimation() {
    }

    public static boolean isAvailable() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(PLAYER_ANIMATOR);
        }
        return loaded;
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (isAvailable()) {
            event.enqueueWork(SearchAnimationHooks::registerFactory);
        }
    }

    /** Rummage: both empty hands. */
    public static void start(Player player) {
        start(player, RUMMAGE, false);
    }

    public static void start(Player player, ResourceLocation animId, boolean showHeldItem) {
        if (player == null || !isAvailable() || animId == null) {
            return;
        }
        bodySyncActive = true;
        SearchAnimationHooks.start(player, animId, showHeldItem);
    }

    public static void stop(Player player) {
        bodySyncActive = false;
        if (player == null || !isAvailable()) {
            return;
        }
        SearchAnimationHooks.stop(player);
    }

    @Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
    public static final class BodySync {
        private BodySync() {
        }

        @SubscribeEvent
        public static void syncBodyToCamera(TickEvent.RenderTickEvent event) {
            if (event.phase != TickEvent.Phase.START || !bodySyncActive || !isAvailable()) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || !minecraft.options.getCameraType().isFirstPerson()) {
                return;
            }
            minecraft.player.yBodyRot = minecraft.player.getYRot();
            minecraft.player.yBodyRotO = minecraft.player.yRotO;
        }
    }
}
