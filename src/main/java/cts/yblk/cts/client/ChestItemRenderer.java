package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static ChestItemRenderer instance;
    private final ChestModelRenderer chest;

    private ChestItemRenderer(BlockEntityRenderDispatcher dispatcher, net.minecraft.client.model.geom.EntityModelSet models) {
        super(dispatcher, models);
        this.chest = new ChestModelRenderer(models.bakeLayer(ModelLayers.CHEST));
    }

    public static ChestItemRenderer get() {
        if (instance == null) {
            Minecraft minecraft = Minecraft.getInstance();
            instance = new ChestItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        }
        return instance;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        chest.render(poseStack, buffer, light, overlay, Direction.SOUTH);
    }
}
