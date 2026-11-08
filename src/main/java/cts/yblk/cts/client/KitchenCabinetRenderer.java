package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import cts.yblk.cts.block.KitchenDrawerBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

public class KitchenCabinetRenderer implements BlockEntityRenderer<CanStashBlockEntity> {
    private final BlockRenderDispatcher dispatcher;

    public KitchenCabinetRenderer(BlockEntityRendererProvider.Context context) {
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
        boolean single = state.getBlock() instanceof KitchenDrawerBlock drawer && drawer.isSingle();
        String wood = woodKey(state.getBlock(), single);

        poseStack.pushPose();
        PropPartRenderer.applyFacing(poseStack, facing);

        if (single) {
            BakedModel body = PropPartRenderer.model(part("kitchen_drawer_single_" + wood + "_body"));
            BakedModel door = PropPartRenderer.model(part("kitchen_drawer_single_" + wood + "_door"));
            PropPartRenderer.renderModel(dispatcher, body, state, poseStack, buffer, light, overlay);
            renderDoor(poseStack, door, state, buffer, light, overlay, openness,
                    16.0F, 8.0F, 15.0F, 22.5F);
        } else {
            BakedModel body = PropPartRenderer.model(part("kitchen_drawer_" + wood + "_body"));
            BakedModel door1 = PropPartRenderer.model(part("kitchen_drawer_" + wood + "_door_1"));
            BakedModel door2 = PropPartRenderer.model(part("kitchen_drawer_" + wood + "_door_2"));
            PropPartRenderer.renderModel(dispatcher, body, state, poseStack, buffer, light, overlay);
            renderDoor(poseStack, door1, state, buffer, light, overlay, openness,
                    0.0F, 7.0F, 15.0F, -45.0F);
            renderDoor(poseStack, door2, state, buffer, light, overlay, openness,
                    16.0F, 10.0F, 15.0F, 22.5F);
        }

        poseStack.popPose();
    }

    private void renderDoor(PoseStack poseStack, BakedModel door, BlockState state, MultiBufferSource buffer,
                            int light, int overlay, float openness,
                            float hxPx, float hyPx, float hzPx, float openDegrees) {
        float hx = hxPx / 16.0F;
        float hy = hyPx / 16.0F;
        float hz = hzPx / 16.0F;
        poseStack.pushPose();
        poseStack.translate(hx, hy, hz);
        poseStack.mulPose(Axis.YP.rotationDegrees(openDegrees * openness));
        poseStack.translate(-hx, -hy, -hz);
        PropPartRenderer.renderModel(dispatcher, door, state, poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CanStashBlockEntity entity) {
        return true;
    }

    private static ResourceLocation part(String path) {
        return new ResourceLocation(Cts.MODID, "block/" + path);
    }

    private static String woodKey(Block block, boolean single) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath();
        String prefix = single ? "kitchen_drawer_single_" : "kitchen_drawer_";
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        return "oak";
    }
}
