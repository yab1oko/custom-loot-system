package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.Cts;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

public class CashRegisterRenderer implements BlockEntityRenderer<CanStashBlockEntity> {
    /** Drawer travel from cash_register_empty (pixels → blocks). */
    private static final float DRAWER_OPEN_Z = 3.0F / 16.0F;

    private static final ResourceLocation BODY = new ResourceLocation(Cts.MODID, "block/cash_register_body");
    private static final ResourceLocation DRAWER = new ResourceLocation(Cts.MODID, "block/cash_register_drawer");

    private final BlockRenderDispatcher dispatcher;

    public CashRegisterRenderer(BlockEntityRendererProvider.Context context) {
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

        BakedModel body = PropPartRenderer.model(BODY);
        BakedModel drawer = PropPartRenderer.model(DRAWER);

        poseStack.pushPose();
        PropPartRenderer.applyFacing(poseStack, facing);
        PropPartRenderer.renderModel(dispatcher, body, state, poseStack, buffer, light, overlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, DRAWER_OPEN_Z * openness);
        PropPartRenderer.renderModel(dispatcher, drawer, state, poseStack, buffer, light, overlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CanStashBlockEntity entity) {
        return true;
    }
}
