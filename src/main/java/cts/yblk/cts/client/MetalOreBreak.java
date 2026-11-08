package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
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
import cts.yblk.cts.block.MetalOreBlock;
import cts.yblk.cts.blockentity.MetalOreBlockEntity;
import cts.yblk.cts.network.BreakMetalOrePacket;
import cts.yblk.cts.network.ModNetwork;

@Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
public class MetalOreBreak {
    private static BlockPos target;
    private static int ticks;
    private static int duration = MetalOreBlockEntity.DEFAULT_BREAK_SECONDS * 20;
    private static int crackStage = -1;
    private static boolean animActive;

    private MetalOreBreak() {
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
                || minecraft.player.getAbilities().instabuild
                || !minecraft.options.keyUse.isDown()
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK
                || !MetalOreBlock.isMineableTarget(minecraft.level.getBlockState(hit.getBlockPos()))
                || !MetalOreBlockEntity.holdsPickaxe(minecraft.player)) {
            reset();
            return;
        }
        BlockPos pos = hit.getBlockPos().immutable();
        if (target == null || !target.equals(pos)) {
            clearCrack(minecraft);
            endAnim(minecraft);
            target = pos;
            ticks = 0;
            duration = MetalOreBlockEntity.DEFAULT_BREAK_SECONDS * 20;
            if (minecraft.level.getBlockEntity(pos) instanceof MetalOreBlockEntity ore) {
                duration = Math.max(1, ore.getBreakSeconds() * 20);
            }
            beginAnim(minecraft);
        }
        if (ticks < duration) {
            ticks++;
            updateCrack(minecraft, pos);
            if (ticks % 6 == 1) {
                if (!SearchAnimation.isAvailable()) {
                    minecraft.player.swing(InteractionHand.MAIN_HAND);
                }
                BlockState state = minecraft.level.getBlockState(pos);
                SoundType sound = state.getSoundType(minecraft.level, pos, minecraft.player);
                minecraft.level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        sound.getHitSound(), SoundSource.BLOCKS,
                        (sound.getVolume() + 1.0F) / 8.0F, sound.getPitch() * 0.5F, false);
            }
            if (ticks >= duration) {
                ModNetwork.CHANNEL.sendToServer(new BreakMetalOrePacket(target));
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
                Component.translatable("gui.cts.to_break"), ticks, duration, event.getPartialTick());
    }

    @SubscribeEvent
    public static void cancelVanillaUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }
        if (isBreaking()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    public static boolean isBreaking() {
        return ticks > 0 && target != null;
    }

    private static void beginAnim(Minecraft minecraft) {
        if (minecraft.player == null || animActive) {
            return;
        }
        animActive = true;
        SearchAnimation.start(minecraft.player, SearchAnimation.MINE, true);
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

    private static void updateCrack(Minecraft minecraft, BlockPos pos) {
        int stage = Math.min(9, (int) (ticks / (float) duration * 10.0F) - 1);
        if (stage == crackStage) {
            return;
        }
        crackStage = stage;
        minecraft.level.destroyBlockProgress(minecraft.player.getId(), pos, stage);
    }

    private static void clearCrack(Minecraft minecraft) {
        if (target != null && minecraft.level != null && minecraft.player != null && crackStage >= 0) {
            minecraft.level.destroyBlockProgress(minecraft.player.getId(), target, -1);
        }
        crackStage = -1;
    }

    private static void reset() {
        Minecraft minecraft = Minecraft.getInstance();
        clearCrack(minecraft);
        endAnim(minecraft);
        target = null;
        ticks = 0;
    }
}
