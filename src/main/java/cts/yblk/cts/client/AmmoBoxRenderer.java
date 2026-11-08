package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.Cts;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;

/**
 * Ammo-box lid from {@code tools/blockbench/ammo_box_lid.bbmodel} animation {@code open}.
 * Pivot is the lid group origin; position is applied in parent space, then X rotation.
 */
public class AmmoBoxRenderer implements BlockEntityRenderer<CanStashBlockEntity> {
    private static final ResourceLocation BODY = new ResourceLocation(Cts.MODID, "block/ammo_box_body");
    private static final ResourceLocation LID = new ResourceLocation(Cts.MODID, "block/ammo_box_lid");

    /** Lid group origin in Blockbench pixels. */
    private static final float HINGE_X = 8.0F / 16.0F;
    private static final float HINGE_Y = 7.0F / 16.0F;
    private static final float HINGE_Z = 13.0F / 16.0F;

    private static final float[] TIMES = {0.0F, 0.25F, 0.5F, 0.75F, 1.0F};
    private static final float[] ROT_X = {0.0F, 40.47F, 71.25F, 68.91F, 70.0F};
    private static final float[] POS_Y = {0.0F, -1.47F, -5.25F, -5.91F, -6.0F};
    private static final float[] POS_Z = {0.0F, 1.68F, 2.54F, 2.85F, 2.9F};

    private final BlockRenderDispatcher dispatcher;

    public AmmoBoxRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CanStashBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        BlockState state = entity.getBlockState();
        Direction facing = state.hasProperty(CanStashBlock.FACING)
                ? state.getValue(CanStashBlock.FACING)
                : Direction.NORTH;
        float openness = Mth.clamp(entity.getLidOpenness(partialTick), 0.0F, 1.0F);
        float rotX = sample(TIMES, ROT_X, openness);
        float posY = sample(TIMES, POS_Y, openness) / 16.0F;
        float posZ = sample(TIMES, POS_Z, openness) / 16.0F;

        BakedModel body = PropPartRenderer.model(BODY);
        BakedModel lid = PropPartRenderer.model(LID);

        poseStack.pushPose();
        PropPartRenderer.applyFacing(poseStack, facing);
        PropPartRenderer.renderModel(dispatcher, body, state, poseStack, buffer, light, overlay, false);

        poseStack.pushPose();
        poseStack.translate(HINGE_X, HINGE_Y + posY, HINGE_Z + posZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.translate(-HINGE_X, -HINGE_Y, -HINGE_Z);
        PropPartRenderer.renderModel(dispatcher, lid, state, poseStack, buffer, light, overlay, false);
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CanStashBlockEntity entity) {
        return true;
    }

    /** Uniform Catmull-Rom, same neighbor rule Blockbench uses on these keys. */
    private static float sample(float[] times, float[] values, float time) {
        if (time <= times[0]) {
            return values[0];
        }
        int last = times.length - 1;
        if (time >= times[last]) {
            return values[last];
        }
        int i = 0;
        while (i < last - 1 && time > times[i + 1]) {
            i++;
        }
        float span = times[i + 1] - times[i];
        float u = span <= 0.0F ? 0.0F : (time - times[i]) / span;
        float p0 = values[Math.max(0, i - 1)];
        float p1 = values[i];
        float p2 = values[i + 1];
        float p3 = values[Math.min(last, i + 2)];
        float u2 = u * u;
        float u3 = u2 * u;
        return 0.5F * ((2.0F * p1)
                + (-p0 + p2) * u
                + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * u2
                + (-p0 + 3.0F * p1 - 3.0F * p2 + p3) * u3);
    }
}
