package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import cts.yblk.cts.block.HarvestTreeStructure;
import cts.yblk.cts.blockentity.HarvestTreeBlockEntity;
import cts.yblk.cts.network.BreakHarvestTreePacket;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.registry.ModBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
public class HarvestTreeBreak {
    private static BlockPos target;
    private static BlockPos crackMaster;
    private static List<BlockPos> crackBlocks = List.of();
    private static int ticks;
    private static int duration = HarvestTreeBlockEntity.DEFAULT_BREAK_SECONDS * 20;
    private static int crackStage = -1;
    private static boolean animActive;

    private HarvestTreeBreak() {
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
                || !HarvestTreeStructure.isChopTarget(minecraft.level, hit.getBlockPos())
                || !HarvestTreeBlockEntity.holdsAxe(minecraft.player)) {
            reset();
            return;
        }
        BlockPos pos = hit.getBlockPos().immutable();
        BlockPos master = HarvestTreeStructure.findMaster(minecraft.level, pos);
        if (master == null) {
            reset();
            return;
        }
        target = pos;
        if (crackMaster == null || !crackMaster.equals(master)) {
            clearCrack(minecraft);
            endAnim(minecraft);
            crackMaster = master.immutable();
            crackBlocks = collectCrackBlocks(minecraft, crackMaster);
            ticks = 0;
            duration = HarvestTreeBlockEntity.DEFAULT_BREAK_SECONDS * 20;
            if (minecraft.level.getBlockEntity(crackMaster) instanceof HarvestTreeBlockEntity tree) {
                duration = Math.max(1, tree.getBreakSeconds() * 20);
            }
            beginAnim(minecraft);
        }
        if (ticks < duration) {
            ticks++;
            updateCrack(minecraft);
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
                ModNetwork.CHANNEL.sendToServer(new BreakHarvestTreePacket(target));
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
        Component title = crackMaster != null
                ? HarvestTreeStructure.itemFor(minecraft.level, crackMaster).getHoverName()
                : ModBlocks.HARVEST_TREE.get().getName();
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
        SearchAnimation.start(minecraft.player, SearchAnimation.CHOP, true);
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

    private static List<BlockPos> collectCrackBlocks(Minecraft minecraft, BlockPos master) {
        List<BlockPos> list = new ArrayList<>();
        list.add(master);
        list.addAll(HarvestTreeStructure.collectParts(minecraft.level, master));
        return Collections.unmodifiableList(list);
    }

    /** Unique fake breaker id so vanilla can show cracks on many blocks at once. */
    private static int crackId(BlockPos pos) {
        return 0x6E000000 | (pos.hashCode() & 0x00FFFFFF);
    }

    private static void updateCrack(Minecraft minecraft) {
        int stage = Math.min(9, (int) (ticks / (float) duration * 10.0F) - 1);
        if (stage == crackStage || minecraft.level == null) {
            return;
        }
        crackStage = stage;
        for (BlockPos pos : crackBlocks) {
            minecraft.level.destroyBlockProgress(crackId(pos), pos, stage);
        }
    }

    private static void clearCrack(Minecraft minecraft) {
        if (minecraft.level != null && crackStage >= 0) {
            for (BlockPos pos : crackBlocks) {
                minecraft.level.destroyBlockProgress(crackId(pos), pos, -1);
            }
        }
        crackBlocks = List.of();
        crackMaster = null;
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
