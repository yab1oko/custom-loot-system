package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Shared helpers for BER props that compose baked body + movable parts.
 * <p>
 * Must use entity {@link RenderType}s (not terrain layers) — terrain layers from a BER
 * break the lightmap. Soft glass uses {@link RenderType#entityTranslucentCull} (depth write
 * cuts sorting flicker vs plain translucent).
 */
final class PropPartRenderer {
    private PropPartRenderer() {
    }

    /** Matches existing CanStash blockstate Y rotations (north model = 0°). */
    static float facingYaw(Direction facing) {
        return facing.toYRot() + 180.0F;
    }

    static void applyFacing(PoseStack poseStack, Direction facing) {
        applyFacing(poseStack, facing, 0.0F);
    }

    static void applyFacing(PoseStack poseStack, Direction facing, float yawOffset) {
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingYaw(facing) + yawOffset));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }

    static void renderModel(BlockRenderDispatcher dispatcher, BakedModel model, BlockState state,
                            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderModel(dispatcher, model, state, poseStack, buffer, light, overlay, false);
    }

    static void renderModel(BlockRenderDispatcher dispatcher, BakedModel model, BlockState state,
                            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
                            boolean translucent) {
        RenderType type = translucent
                ? RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS)
                : RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer consumer = buffer.getBuffer(type);
        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                consumer,
                state,
                model,
                1.0F, 1.0F, 1.0F,
                light,
                overlay,
                ModelData.EMPTY,
                type);
    }

    static BakedModel model(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(location);
    }
}
