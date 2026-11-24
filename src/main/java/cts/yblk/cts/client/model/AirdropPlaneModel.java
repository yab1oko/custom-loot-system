package cts.yblk.cts.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import cts.yblk.cts.Cts;
import cts.yblk.cts.entity.AirdropPlaneEntity;

@OnlyIn(Dist.CLIENT)
public class AirdropPlaneModel extends EntityModel<AirdropPlaneEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(Cts.MODID, "airdrop_plane"), "main");

    private final ModelPart root;

    public AirdropPlaneModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -2.2F, -16.0F, 4.0F, 4.0F, 28.0F)
                        .texOffs(32, 0).addBox(-1.4F, -1.5F, -20.0F, 2.8F, 2.6F, 5.0F)
                        .texOffs(0, 32).addBox(-1.2F, -4.6F, 8.0F, 2.4F, 3.2F, 4.0F)
                        .texOffs(10, 32).addBox(-6.0F, -1.2F, 8.5F, 12.0F, 0.8F, 4.0F)
                        .texOffs(32, 8).addBox(-1.6F, -2.6F, -6.0F, 3.2F, 1.2F, 5.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        body.addOrReplaceChild("wing_left", CubeListBuilder.create()
                        .texOffs(0, 26).addBox(0.0F, 0.0F, -2.0F, 16.0F, 0.7F, 6.0F),
                PartPose.offsetAndRotation(1.5F, -0.4F, -2.0F, 0.0F, 0.0F, 0.08F));
        body.addOrReplaceChild("wing_right", CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-16.0F, 0.0F, -2.0F, 16.0F, 0.7F, 6.0F),
                PartPose.offsetAndRotation(-1.5F, -0.4F, -2.0F, 0.0F, 0.0F, -0.08F));
        body.addOrReplaceChild("engine_left", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1.2F, -1.2F, -2.5F, 2.4F, 2.4F, 5.0F),
                PartPose.offset(5.5F, 0.8F, -1.0F));
        body.addOrReplaceChild("engine_right", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1.2F, -1.2F, -2.5F, 2.4F, 2.4F, 5.0F),
                PartPose.offset(-5.5F, 0.8F, -1.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AirdropPlaneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
