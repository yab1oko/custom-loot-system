package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.block.BarricadeBlock;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.block.ShelvingBlock;
import cts.yblk.cts.block.HarvestTreeStructure;
import cts.yblk.cts.block.MetalOreBlock;
import cts.yblk.cts.block.VentilationGrilleBlock;
import cts.yblk.cts.blockentity.BarricadeBlockEntity;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.HarvestTreeBlockEntity;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;
import cts.yblk.cts.blockentity.MetalOreBlockEntity;
import cts.yblk.cts.blockentity.VentilationGrilleBlockEntity;
import cts.yblk.cts.registry.ModBlocks;

@Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
public class TargetPrompt {
    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos hitPos = hit.getBlockPos();
        BlockState hitState = minecraft.level.getBlockState(hitPos);
        if (BarricadeBlock.isBreakableTarget(hitState) && !BarricadeBreak.isBreaking()) {
            Component action = BarricadeBlockEntity.holdsBreakTool(minecraft.player)
                    ? Component.translatable("gui.cts.to_break")
                    : Component.translatable("gui.cts.need_break_tool");
            draw(minecraft, event.getGuiGraphics(), hitState.getBlock().getName(), null, action);
            return;
        }
        if (VentilationGrilleBlock.isOpenableTarget(hitState) && !VentilationGrilleOpen.isOpening()) {
            Component action = VentilationGrilleBlockEntity.holdsScrewdriver(minecraft.player)
                    ? Component.translatable("gui.cts.to_open")
                    : Component.translatable("gui.cts.need_screwdriver");
            draw(minecraft, event.getGuiGraphics(), hitState.getBlock().getName(), null, action);
            return;
        }
        if (MetalOreBlock.isMineableTarget(hitState) && !MetalOreBreak.isBreaking()) {
            Component action = MetalOreBlockEntity.holdsPickaxe(minecraft.player)
                    ? Component.translatable("gui.cts.to_break")
                    : Component.translatable("gui.cts.need_pickaxe");
            draw(minecraft, event.getGuiGraphics(), hitState.getBlock().getName(), null, action);
            return;
        }
        if (HarvestTreeStructure.isChopTarget(minecraft.level, hitPos) && !HarvestTreeBreak.isBreaking()) {
            Component action = HarvestTreeBlockEntity.holdsAxe(minecraft.player)
                    ? Component.translatable("gui.cts.to_break")
                    : Component.translatable("gui.cts.need_axe");
            Component title = HarvestTreeStructure.itemFor(minecraft.level, hitPos).getHoverName();
            draw(minecraft, event.getGuiGraphics(), title, null, action);
            return;
        }
        if (CanStashBlock.isSearchable(hitState)) {
            BlockPos inventory = ShelvingBlock.inventoryPos(minecraft.level, hitPos);
            BlockState state = minecraft.level.getBlockState(inventory);
            CanStashBlockEntity stashEntity = minecraft.level.getBlockEntity(inventory) instanceof CanStashBlockEntity found
                    ? found : null;
            Component title = stashEntity != null ? stashEntity.getDisplayName() : state.getBlock().getName();
            Component status = null;
            boolean fullyEmpty = false;
            if (state.hasProperty(CanStashBlock.LOOTED) && state.getValue(CanStashBlock.LOOTED)
                    && stashEntity != null) {
                fullyEmpty = stashEntity.isContentsEmpty();
                status = Component.translatable(fullyEmpty ? "gui.cts.empty" : "gui.cts.almost_empty");
            }
            if (fullyEmpty) {
                draw(minecraft, event.getGuiGraphics(), title, status, null);
                return;
            }
            if (!CanStashSearch.isSearching()) {
                Component action = Component.translatable("gui.cts.to_search");
                if (stashEntity != null
                        && stashEntity.requiresTool()
                        && !stashEntity.allowsSearch(minecraft.player)) {
                    action = Component.translatable("gui.cts.need_tool_item", stashEntity.toolRequirementName());
                }
                draw(minecraft, event.getGuiGraphics(), title, status, action);
            }
            return;
        }
        if (hitState.is(ModBlocks.LOOT_PLATE.get())
                && minecraft.level.getBlockEntity(hitPos) instanceof LootPlateBlockEntity plate
                && !plate.getDisplayedItem().isEmpty()) {
            draw(minecraft, event.getGuiGraphics(), plate.getDisplayedItem().getHoverName(), null, Component.translatable("gui.cts.to_pick_up"));
        }
    }

    private static void draw(Minecraft minecraft, GuiGraphics graphics, Component title, @Nullable Component status, @Nullable Component action) {
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(width / 2.0F, height / 2.0F - 42.0F, 0.0F);
        pose.scale(0.8F, 0.8F, 1.0F);
        int y = 0;
        graphics.drawCenteredString(minecraft.font, title, 0, y, 0xFFFFFF);
        y += 12;
        if (status != null) {
            graphics.drawCenteredString(minecraft.font, status, 0, y, 0xFFD0D0D0);
            y += 12;
        }
        if (action != null) {
            Component line = Component.empty()
                    .append("[")
                    .append(minecraft.options.keyUse.getTranslatedKeyMessage())
                    .append("] ")
                    .append(action);
            graphics.drawCenteredString(minecraft.font, line, 0, y, 0xFFFFFF);
        }
        pose.popPose();
    }
}
