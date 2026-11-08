package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

public class ChestPropRenderer implements BlockEntityRenderer<CanStashBlockEntity> {
    private final ChestModelRenderer chest;

    public ChestPropRenderer(BlockEntityRendererProvider.Context context) {
        this.chest = new ChestModelRenderer(context);
    }

    @Override
    public void render(CanStashBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = entity.getBlockState();
        Direction facing = state.hasProperty(CanStashBlock.FACING) ? state.getValue(CanStashBlock.FACING) : Direction.NORTH;
        chest.render(poseStack, buffer, light, overlay, facing, entity.getLidOpenness(partialTick), Sheets.CHEST_LOCATION);
    }
}
