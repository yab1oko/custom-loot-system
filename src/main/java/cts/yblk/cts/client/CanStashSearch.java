package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import cts.yblk.cts.block.BrickCacheBlock;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.block.ShelvingBlock;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModSounds;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.OpenCanStashPacket;
import cts.yblk.cts.network.SearchAnimPacket;

@Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
public class CanStashSearch {
    private static BlockPos target;
    private static int ticks;
    private static int duration = CanStashBlockEntity.SEARCH_TICKS;
    private static SoundInstance searchingSound;
    private static boolean animActive;

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
                || !CanStashBlock.isSearchable(minecraft.level.getBlockState(hit.getBlockPos()))) {
            reset();
            return;
        }
        BlockPos pos = ShelvingBlock.inventoryPos(minecraft.level, hit.getBlockPos());
        if (minecraft.level.getBlockEntity(pos) instanceof CanStashBlockEntity stash && !stash.allowsSearch(minecraft.player)) {
            reset();
            return;
        }
        if (target == null || !target.equals(pos)) {
            if (target != null) {
                endRummage(minecraft);
            }
            target = pos.immutable();
            ticks = 0;
            duration = durationTicks(minecraft, pos);
            startSearchingSound(minecraft, pos);
            beginRummage(minecraft, pos);
        }
        if (animActive && SearchAnimation.isAvailable() && minecraft.options.getCameraType().isFirstPerson()) {
            minecraft.player.yBodyRot = minecraft.player.getYRot();
            minecraft.player.yBodyRotO = minecraft.player.yRotO;
        }
        if (ticks < duration) {
            ticks++;
            BlockState state = minecraft.level.getBlockState(pos);
            boolean customSearchSound = state.is(ModBlocks.CORPSE.get());
            // No player.swing() — it fights PA and shakes the body like repeated clicks.
            // Without PA, a light alternate swing is the only FP feedback.
            if (!SearchAnimation.isAvailable() && ticks % 8 == 1) {
                InteractionHand hand = (ticks / 8) % 2 == 0
                        ? InteractionHand.MAIN_HAND
                        : InteractionHand.OFF_HAND;
                minecraft.player.swing(hand, false);
            }
            if (!customSearchSound && ticks % 6 == 1) {
                SoundType sound = state.is(ModBlocks.BOXES.get())
                        ? SoundType.WOOL
                        : state.getBlock() instanceof BrickCacheBlock
                        ? SoundType.STONE
                        : state.getSoundType(minecraft.level, pos, minecraft.player);
                minecraft.level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        sound.getHitSound(), SoundSource.BLOCKS,
                        (sound.getVolume() + 1.0F) / 8.0F, sound.getPitch() * 0.5F, false);
            }
            if (ticks >= duration) {
                stopSearchingSound();
                endRummage(minecraft);
                ModNetwork.CHANNEL.sendToServer(new OpenCanStashPacket(target));
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type() || ticks <= 0 || target == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Component title = null;
        if (minecraft.level != null) {
            title = minecraft.level.getBlockState(target).getBlock().getName();
            if (minecraft.level.getBlockEntity(target) instanceof CanStashBlockEntity stash) {
                title = stash.getDisplayName();
            }
        }
        ActionProgressHud.render(event.getGuiGraphics(), title, null, ticks, duration, event.getPartialTick());
    }

    public static boolean isSearching() {
        return ticks > 0 && target != null;
    }

    /**
     * Block vanilla use/attack while searching (or aiming a searchable stash) so
     * RMB hold does not spam swings / body bob.
     */
    @SubscribeEvent
    public static void cancelVanillaUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.getAbilities().instabuild) {
            return;
        }
        if (isSearching()) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        if (minecraft.hitResult instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK
                && CanStashBlock.isSearchable(minecraft.level.getBlockState(hit.getBlockPos()))) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    /** 0..1 while holding search on {@code pos}; otherwise -1. */
    public static float searchProgress(BlockPos pos) {
        if (ticks <= 0 || target == null || pos == null || !target.equals(pos) || duration <= 0) {
            return -1.0F;
        }
        return Mth.clamp(ticks / (float) duration, 0.0F, 1.0F);
    }

    private static void beginRummage(Minecraft minecraft, BlockPos pos) {
        if (minecraft.player == null || animActive) {
            return;
        }
        animActive = true;
        SearchAnimation.start(minecraft.player);
        ModNetwork.CHANNEL.sendToServer(SearchAnimPacket.start(minecraft.player, pos));
    }

    private static void endRummage(Minecraft minecraft) {
        if (!animActive || minecraft.player == null) {
            animActive = false;
            return;
        }
        animActive = false;
        SearchAnimation.stop(minecraft.player);
        ModNetwork.CHANNEL.sendToServer(SearchAnimPacket.stop(minecraft.player));
    }

    private static int durationTicks(Minecraft minecraft, BlockPos pos) {
        if (minecraft.level.getBlockEntity(pos) instanceof CanStashBlockEntity stash) {
            return Math.max(1, stash.getEffectiveSearchSeconds() * 20);
        }
        int seconds = CanStashBlockEntity.DEFAULT_SEARCH_SECONDS;
        if (minecraft.level.getBlockState(pos).is(ModBlocks.SAFE.get())) {
            seconds = CanStashBlockEntity.SAFE_SEARCH_SECONDS;
        }
        return Math.max(1, seconds * 20);
    }

    private static void startSearchingSound(Minecraft minecraft, BlockPos pos) {
        stopSearchingSound();
        if (minecraft.level == null || !minecraft.level.getBlockState(pos).is(ModBlocks.CORPSE.get())) {
            return;
        }
        searchingSound = new SimpleSoundInstance(
                ModSounds.SEARCHING.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F,
                RandomSource.create(),
                pos);
        minecraft.getSoundManager().play(searchingSound);
    }

    private static void stopSearchingSound() {
        if (searchingSound != null) {
            Minecraft.getInstance().getSoundManager().stop(searchingSound);
            searchingSound = null;
        }
    }

    private static void reset() {
        Minecraft minecraft = Minecraft.getInstance();
        endRummage(minecraft);
        stopSearchingSound();
        target = null;
        ticks = 0;
    }
}
