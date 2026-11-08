package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import cts.yblk.cts.Cts;
import cts.yblk.cts.client.model.AirdropFlyingModel;
import cts.yblk.cts.entity.AirdropEntity;

public class AirdropEntityRenderer extends EntityRenderer<AirdropEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Cts.MODID, "textures/entity/airdrop_flying.png");
    private final AirdropFlyingModel model;

    public AirdropEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new AirdropFlyingModel(context.bakeLayer(AirdropFlyingModel.LAYER_LOCATION));
        this.shadowRadius = 1.4F;
    }

    @Override
    public void render(AirdropEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        // Hierarchical / Blockbench entity models are upside-down relative to world Y.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.5F, 0.0F);

        float age = entity.tickCount + partialTick;
        this.model.setupAnim(entity, 0.0F, 0.0F, age, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AirdropEntity entity) {
        return TEXTURE;
    }
}
