package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

public class AirdropBlockRenderer implements BlockEntityRenderer<CanStashBlockEntity> {
    private final BlockRenderDispatcher dispatcher;

    public AirdropBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = net.minecraft.client.Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(CanStashBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        Direction facing = state.hasProperty(CanStashBlock.FACING) ? state.getValue(CanStashBlock.FACING) : Direction.NORTH;
        BakedModel model = dispatcher.getBlockModel(state);

        poseStack.pushPose();
        // BB model is authored in north-local space (footprint -X / +Z from master).
        // NORTH must keep 0° so geometry lines up with AirdropStructure offsets.
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        dispatcher.getModelRenderer().tesselateBlock(
                entity.getLevel(), model, state, entity.getBlockPos(), poseStack,
                buffer.getBuffer(net.minecraft.client.renderer.ItemBlockRenderTypes.getRenderType(state, false)),
                false, RandomSource.create(), state.getSeed(entity.getBlockPos()), OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY, null);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CanStashBlockEntity entity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
