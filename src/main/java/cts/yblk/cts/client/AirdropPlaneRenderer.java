package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import cts.yblk.cts.Cts;
import cts.yblk.cts.client.model.AirdropPlaneModel;
import cts.yblk.cts.entity.AirdropPlaneEntity;

public class AirdropPlaneRenderer extends EntityRenderer<AirdropPlaneEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Cts.MODID, "textures/entity/airdrop_plane.png");
    private final AirdropPlaneModel model;

    public AirdropPlaneRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new AirdropPlaneModel(context.bakeLayer(AirdropPlaneModel.LAYER));
        this.shadowRadius = 1.2F;
    }

    @Override
    public void render(AirdropPlaneEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!AirdropPlaneSound.isPlaying(entity)) {
            AirdropPlaneSound.mark(entity);
            Minecraft.getInstance().getSoundManager().play(new AirdropPlaneSound(entity));
        }
        float fade = Mth.clamp(entity.fade(), 0.0F, 1.0F);
        if (fade <= 0.02F) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.35D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(1.15F, 1.15F, 1.15F);
        this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, fade);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AirdropPlaneEntity entity) {
        return TEXTURE;
    }
}
