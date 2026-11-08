package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.block.LootPlateBlock;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;

public class LootPlateRenderer implements BlockEntityRenderer<LootPlateBlockEntity> {
    public LootPlateRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LootPlateBlockEntity plate, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = plate.getBlockState();
        if (state.getValue(LootPlateBlock.CONFIGURED) && PlateVisibility.localPlayerHoldingPlate()) {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    state.setValue(LootPlateBlock.CONFIGURED, false),
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
        }

        ItemStack stack = plate.getDisplayedItem();
        if (stack.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        int seed = (int) plate.getBlockPos().asLong();
        BakedModel model = itemRenderer.getModel(stack, plate.getLevel(), null, seed);
        Direction facing = state.getValue(LootPlateBlock.FACING);
        boolean flat = !model.isGui3d();
        boolean blockItem = stack.getItem() instanceof BlockItem;

        poseStack.pushPose();
        poseItem(poseStack, facing, plate.getSpin(), flat, blockItem);
        if (flat) {
            poseStack.scale(0.45F, 0.45F, 0.45F);
            itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, packedLight, packedOverlay, poseStack, buffer, plate.getLevel(), seed);
        } else {
            // Tools: GROUND lays custom models on their side. Blocks: FIXED avoids GROUND's float.
            // Walls: FIXED like item frames.
            ItemDisplayContext context = facing == Direction.UP && !blockItem
                    ? ItemDisplayContext.GROUND
                    : ItemDisplayContext.FIXED;
            poseStack.scale(0.75F, 0.75F, 0.75F);
            itemRenderer.renderStatic(stack, context, packedLight, packedOverlay, poseStack, buffer, plate.getLevel(), seed);
        }
        poseStack.popPose();
    }

    private static void poseItem(PoseStack poseStack, Direction facing, float spin, boolean flat, boolean blockItem) {
        if (facing == Direction.UP) {
            double y = flat ? 0.04D : (blockItem ? 1.0D / 16.0D : 0.18D);
            poseStack.translate(0.5D, y, 0.5D);
            if (flat) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            }
            return;
        }

        // Same yaw convention as ItemFrameRenderer so FIXED/GUI face outward.
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        // +Z toward the plate face (was -Z → opposite side of the block).
        poseStack.translate(0.0D, 0.0D, 0.5D - (1.0D / 16.0D));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
    }
}
