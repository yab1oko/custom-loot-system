package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.block.EntityPlateBlock;
import cts.yblk.cts.blockentity.EntityPlateBlockEntity;
import cts.yblk.cts.registry.ModItems;

public class EntityPlateRenderer implements BlockEntityRenderer<EntityPlateBlockEntity> {
    public EntityPlateRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EntityPlateBlockEntity plate, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = plate.getBlockState();
        if (!state.getValue(EntityPlateBlock.CONFIGURED) || !holdingEntityPlate()) {
            return;
        }
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state.setValue(EntityPlateBlock.CONFIGURED, false),
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
    }

    private static boolean holdingEntityPlate() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }
        return isPlate(minecraft.player.getMainHandItem()) || isPlate(minecraft.player.getOffhandItem());
    }

    private static boolean isPlate(ItemStack stack) {
        return stack.is(ModItems.ENTITY_PLATE.get());
    }
}
