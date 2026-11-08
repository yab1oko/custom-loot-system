package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.block.VentilationGrilleBlock;
import cts.yblk.cts.blockentity.VentilationGrilleBlockEntity;
import cts.yblk.cts.network.BreakVentilationGrillePacket;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.registry.ModSounds;

@Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
public class VentilationGrilleOpen {
    private static BlockPos target;
    private static int ticks;
    private static int duration = VentilationGrilleBlockEntity.DEFAULT_OPEN_SECONDS * 20;
    private static SoundInstance screwdriverSound;
    private static boolean animActive;

    private VentilationGrilleOpen() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.level == null
                || minecraft.screen != null
                || !minecraft.options.keyUse.isDown()
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK
                || !VentilationGrilleBlock.isOpenableTarget(minecraft.level.getBlockState(hit.getBlockPos()))
                || !VentilationGrilleBlockEntity.holdsScrewdriver(minecraft.player)) {
            reset();
            return;
        }
        BlockPos pos = hit.getBlockPos().immutable();
        if (target == null || !target.equals(pos)) {
            endAnim(minecraft);
            target = pos;
            ticks = 0;
            duration = VentilationGrilleBlockEntity.DEFAULT_OPEN_SECONDS * 20;
            if (minecraft.level.getBlockEntity(pos) instanceof VentilationGrilleBlockEntity grille) {
                duration = Math.max(1, grille.getOpenSeconds() * 20);
            }
            playScrewdriverSound(minecraft, pos);
            beginAnim(minecraft);
        }
        if (ticks < duration) {
            ticks++;
            if (!SearchAnimation.isAvailable() && ticks % 8 == 1) {
                minecraft.player.swing(InteractionHand.MAIN_HAND);
            }
            if (ticks >= duration) {
                ModNetwork.CHANNEL.sendToServer(new BreakVentilationGrillePacket(target));
                reset();
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type() || ticks <= 0 || target == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Component title = minecraft.level != null
                ? minecraft.level.getBlockState(target).getBlock().getName()
                : Component.empty();
        ActionProgressHud.render(event.getGuiGraphics(), title,
                Component.translatable("gui.cts.to_open"), ticks, duration, event.getPartialTick());
    }

    @SubscribeEvent
    public static void cancelVanillaUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }
        if (isOpening()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    public static boolean isOpening() {
        return ticks > 0 && target != null;
    }

    private static void beginAnim(Minecraft minecraft) {
        if (minecraft.player == null || animActive) {
            return;
        }
        animActive = true;
        SearchAnimation.start(minecraft.player, SearchAnimation.SCREWDRIVER, true);
    }

    private static void endAnim(Minecraft minecraft) {
        if (!animActive) {
            return;
        }
        animActive = false;
        if (minecraft.player != null) {
            SearchAnimation.stop(minecraft.player);
        }
    }

    private static void playScrewdriverSound(Minecraft minecraft, BlockPos pos) {
        stopScrewdriverSound();
        screwdriverSound = new SimpleSoundInstance(
                ModSounds.SCREWDRIVER.get(),
                SoundSource.BLOCKS,
                0.45F,
                1.0F,
                SoundInstance.createUnseededRandom(),
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D);
        minecraft.getSoundManager().play(screwdriverSound);
    }

    private static void stopScrewdriverSound() {
        if (screwdriverSound == null) {
            return;
        }
        Minecraft.getInstance().getSoundManager().stop(screwdriverSound);
        screwdriverSound = null;
    }

    private static void reset() {
        stopScrewdriverSound();
        endAnim(Minecraft.getInstance());
        target = null;
        ticks = 0;
    }
}
