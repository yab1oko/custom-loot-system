package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class ChestModelRenderer {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public ChestModelRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(ModelLayers.CHEST));
    }

    public ChestModelRenderer(ModelPart root) {
        this.bottom = root.getChild("bottom");
        this.lid = root.getChild("lid");
        this.lock = root.getChild("lock");
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, Direction facing) {
        render(poseStack, buffer, light, overlay, facing, false, Sheets.CHEST_LOCATION);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, Direction facing, boolean open) {
        render(poseStack, buffer, light, overlay, facing, open ? 1.0F : 0.0F, Sheets.CHEST_LOCATION);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, Direction facing, boolean open, Material material) {
        render(poseStack, buffer, light, overlay, facing, open ? 1.0F : 0.0F, material);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, Direction facing, float openness, Material material) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        float clamped = Math.max(0.0F, Math.min(1.0F, openness));
        lid.xRot = -(clamped * ((float) Math.PI / 2.0F));
        lock.xRot = lid.xRot;
        VertexConsumer consumer = material.buffer(buffer, RenderType::entityCutout);
        bottom.render(poseStack, consumer, light, overlay);
        lid.render(poseStack, consumer, light, overlay);
        lock.render(poseStack, consumer, light, overlay);
        poseStack.popPose();
    }

    public static Material materialFor(BlockState mimic) {
        if (mimic.is(Blocks.TRAPPED_CHEST) || mimic.getBlock() instanceof TrappedChestBlock) {
            return Sheets.CHEST_TRAP_LOCATION;
        }
        if (mimic.is(Blocks.ENDER_CHEST)) {
            return Sheets.ENDER_CHEST_LOCATION;
        }
        return Sheets.CHEST_LOCATION;
    }

    public static Direction facingOf(BlockState state, Direction fallback) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return fallback;
    }

    public static boolean isSingleChest(BlockState state) {
        return !state.hasProperty(BlockStateProperties.CHEST_TYPE)
                || state.getValue(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE;
    }
}
