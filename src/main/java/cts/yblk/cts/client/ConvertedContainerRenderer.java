package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.blockentity.ConvertedContainerBlockEntity;
import cts.yblk.cts.registry.ModTags;

public class ConvertedContainerRenderer implements BlockEntityRenderer<ConvertedContainerBlockEntity> {
    private final ChestModelRenderer chest;
    private final ShulkerModel<?> shulker;
    private final BlockRenderDispatcher dispatcher;

    public ConvertedContainerRenderer(BlockEntityRendererProvider.Context context) {
        this.chest = new ChestModelRenderer(context);
        this.shulker = new ShulkerModel<>(context.bakeLayer(ModelLayers.SHULKER));
        this.dispatcher = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(ConvertedContainerBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState mimic = entity.getMimic();
        boolean open = entity.getBlockState().hasProperty(CanStashBlock.LOOTED)
                && entity.getBlockState().getValue(CanStashBlock.LOOTED);
        float openness = entity.getLidOpenness(partialTick);
        Direction fallback = entity.getBlockState().hasProperty(CanStashBlock.FACING)
                ? entity.getBlockState().getValue(CanStashBlock.FACING)
                : Direction.NORTH;

        if (mimic.getBlock() instanceof ChestBlock || mimic.is(ModTags.CHEST_LIKE)) {
            MimicChestLooks.render(mimic, entity.getBlockPos(), partialTick, poseStack, buffer, light, overlay,
                    openness, ChestModelRenderer.facingOf(mimic, fallback), chest);
            return;
        }
        if (mimic.getBlock() instanceof BarrelBlock) {
            BlockState rendered = mimic.hasProperty(BarrelBlock.OPEN)
                    ? mimic.setValue(BarrelBlock.OPEN, open)
                    : mimic;
            dispatcher.renderSingleBlock(rendered, poseStack, buffer, light, overlay, ModelData.EMPTY, null);
            return;
        }
        if (mimic.getBlock() instanceof ShulkerBoxBlock) {
            renderShulker(mimic, openness, poseStack, buffer, light, overlay);
            return;
        }
        if (mimic.getRenderShape() == RenderShape.MODEL) {
            dispatcher.renderSingleBlock(mimic, poseStack, buffer, light, overlay, ModelData.EMPTY, null);
            return;
        }
        dispatcher.renderSingleBlock(mimic, poseStack, buffer, light, overlay, ModelData.EMPTY, null);
    }

    private void renderShulker(BlockState mimic, float progress, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Direction direction = mimic.hasProperty(ShulkerBoxBlock.FACING)
                ? mimic.getValue(ShulkerBoxBlock.FACING)
                : Direction.UP;
        DyeColor color = ShulkerBoxBlock.getColorFromBlock(mimic.getBlock());
        Material material = color == null
                ? Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION
                : Sheets.SHULKER_TEXTURE_LOCATION.get(color.getId());

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(0.9995F, 0.9995F, 0.9995F);
        poseStack.mulPose(direction.getRotation());
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.0F, -1.0F, 0.0F);
        ModelPart lid = shulker.getLid();
        lid.setPos(0.0F, 24.0F - progress * 0.5F * 16.0F, 0.0F);
        lid.yRot = 270.0F * progress * ((float) Math.PI / 180.0F);
        VertexConsumer consumer = material.buffer(buffer, RenderType::entityCutoutNoCull);
        shulker.renderToBuffer(poseStack, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
