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
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -3.0F, -16.0F, 8.0F, 6.0F, 28.0F)
                        .texOffs(0, 34).addBox(-18.0F, -1.0F, -4.0F, 36.0F, 1.0F, 8.0F)
                        .texOffs(0, 44).addBox(-2.0F, -8.0F, 8.0F, 4.0F, 6.0F, 4.0F)
                        .texOffs(16, 44).addBox(-6.0F, -2.0F, 10.0F, 12.0F, 1.0F, 6.0F)
                        .texOffs(44, 0).addBox(-3.0F, -2.0F, -20.0F, 6.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
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
