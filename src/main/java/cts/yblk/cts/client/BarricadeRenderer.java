package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import cts.yblk.cts.Cts;
import cts.yblk.cts.block.BarricadeBlock;
import cts.yblk.cts.blockentity.BarricadeBlockEntity;

public class BarricadeRenderer implements BlockEntityRenderer<BarricadeBlockEntity> {
    private static final ResourceLocation[] MODELS = {
            new ResourceLocation(Cts.MODID, "block/barricade_1"),
            new ResourceLocation(Cts.MODID, "block/barricade_2"),
            new ResourceLocation(Cts.MODID, "block/barricade_3")
    };

    private final BlockRenderDispatcher dispatcher;

    public BarricadeRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(BarricadeBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (state.getValue(BarricadeBlock.BROKEN)) {
            return;
        }
        int spin = state.getValue(BarricadeBlock.SPIN);
        int variant = Math.max(0, Math.min(2, state.getValue(BarricadeBlock.VARIANT)));
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(MODELS[variant]);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        // wallYRot is CW degrees (0/90/180/270); PoseStack Y+ is CCW → negate.
        poseStack.mulPose(Axis.YP.rotationDegrees(-BarricadeBlock.wallYRot(state)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin * 90.0F));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.cutout()),
                state,
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                RenderType.cutout());
        poseStack.popPose();
    }
}
