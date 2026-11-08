package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.Cts;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

public class DresserRenderer implements BlockEntityRenderer<CanStashBlockEntity> {
    /** Bottom drawer open travel from dresser_empty (pixels → blocks). */
    private static final float DRAWER1_OPEN_Z = 5.2F / 16.0F;
    /** Top drawer open travel from dresser_empty. */
    private static final float DRAWER2_OPEN_Z = 1.4F / 16.0F;

    private final BlockRenderDispatcher dispatcher;

    public DresserRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CanStashBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        BlockState state = entity.getBlockState();
        Direction facing = state.hasProperty(CanStashBlock.FACING)
                ? state.getValue(CanStashBlock.FACING)
                : Direction.NORTH;
        float openness = entity.getLidOpenness(partialTick);
        String wood = woodKey(state.getBlock());

        BakedModel body = PropPartRenderer.model(part(wood, "body"));
        BakedModel drawer1 = PropPartRenderer.model(part(wood, "drawer_1"));
        BakedModel drawer2 = PropPartRenderer.model(part(wood, "drawer_2"));

        poseStack.pushPose();
        PropPartRenderer.applyFacing(poseStack, facing);
        PropPartRenderer.renderModel(dispatcher, body, state, poseStack, buffer, light, overlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, DRAWER1_OPEN_Z * openness);
        PropPartRenderer.renderModel(dispatcher, drawer1, state, poseStack, buffer, light, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, DRAWER2_OPEN_Z * openness);
        PropPartRenderer.renderModel(dispatcher, drawer2, state, poseStack, buffer, light, overlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CanStashBlockEntity entity) {
        return true;
    }

    private static ResourceLocation part(String wood, String part) {
        return new ResourceLocation(Cts.MODID, "block/dresser_" + wood + "_" + part);
    }

    private static String woodKey(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath();
        if (path.startsWith("dresser_")) {
            return path.substring("dresser_".length());
        }
        return "oak";
    }
}
