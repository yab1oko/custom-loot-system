package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

public class HingedPropRenderer implements BlockEntityRenderer<CanStashBlockEntity> {
    private final HingedPropSpec spec;
    private final BlockRenderDispatcher dispatcher;

    public HingedPropRenderer(BlockEntityRendererProvider.Context context, HingedPropSpec spec) {
        this.dispatcher = context.getBlockRenderDispatcher();
        this.spec = spec;
    }

    @Override
    public void render(CanStashBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        BlockState state = entity.getBlockState();
        Direction facing = state.hasProperty(CanStashBlock.FACING)
                ? state.getValue(CanStashBlock.FACING)
                : Direction.NORTH;
        float openness = entity.getLidOpenness(partialTick);
        BakedModel body = PropPartRenderer.model(spec.body());
        BakedModel lid = PropPartRenderer.model(spec.lid());

        poseStack.pushPose();
        PropPartRenderer.applyFacing(poseStack, facing, spec.yawOffset());
        // Opaque/cutout body first; translucent door/glass after (and with cull) to cut flicker.
        PropPartRenderer.renderModel(dispatcher, body, state, poseStack, buffer, light, overlay, false);

        poseStack.pushPose();
        poseStack.translate(spec.hingeX(), spec.hingeY(), spec.hingeZ());
        float angle = spec.openDegrees() * openness;
        if (spec.axis() == 'y') {
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(angle));
        }
        poseStack.translate(-spec.hingeX(), -spec.hingeY(), -spec.hingeZ());
        PropPartRenderer.renderModel(dispatcher, lid, state, poseStack, buffer, light, overlay, spec.translucent());
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CanStashBlockEntity entity) {
        return true;
    }
}
