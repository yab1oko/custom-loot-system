package cts.yblk.cts.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import cts.yblk.cts.Cts;
import cts.yblk.cts.entity.AirdropEntity;

@OnlyIn(Dist.CLIENT)
public class AirdropFlyingModel extends HierarchicalModel<AirdropEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Cts.MODID, "airdrop_flying"), "main");

    private final ModelPart root;
    private final ModelPart bone;

    public AirdropFlyingModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 68).addBox(-16.0F, 0.6F, -15.4F, 32.0F, 32.0F, 32.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-17.0F, -0.4F, -16.4F, 34.0F, 34.0F, 34.0F, new CubeDeformation(0.0F))
                        .texOffs(78, 200).addBox(-18.0F, -38.4F, -26.4F, 36.0F, 2.0F, 54.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -8.6F, -0.6F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(102, 224).addBox(-18.0F, -2.0F, -3.0F, 36.0F, 2.0F, 30.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -25.0F, -50.0F, 0.4363F, 0.0F, 0.0F));

        bone.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(0, 216).addBox(-1.0F, -38.0F, -1.0F, 2.0F, 38.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-15.0F, 0.6F, 16.6F, -2.6619F, 0.0161F, 3.1106F));

        bone.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                        .texOffs(0, 216).addBox(-1.0F, -38.0F, -1.0F, 2.0F, 38.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(15.0F, 0.6F, 16.6F, -2.6619F, -0.0161F, -3.1106F));

        bone.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                        .texOffs(0, 216).addBox(-1.0F, -38.0F, -1.0F, 2.0F, 38.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-15.0F, 0.6F, -15.4F, 0.4794F, 0.0242F, -0.0465F));

        bone.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                        .texOffs(0, 216).addBox(-1.0F, -38.0F, -1.0F, 2.0F, 38.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(15.0F, 0.6F, -15.4F, 0.4794F, -0.0242F, 0.0465F));

        bone.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                        .texOffs(102, 224).addBox(-18.0F, -2.0F, -3.0F, 36.0F, 2.0F, 30.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -27.0F, 52.0F, 2.7053F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(AirdropEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.flyAnimationState, AirdropFlyingAnimation.FLY, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
