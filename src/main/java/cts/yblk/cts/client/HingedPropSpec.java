package cts.yblk.cts.client;

import net.minecraft.resources.ResourceLocation;
import cts.yblk.cts.Cts;

/**
 * Body + hinged lid/door baked parts for {@link HingedPropRenderer}.
 *
 * @param hingeX       hinge in block coords (model pixels / 16)
 * @param axis         {@code 'x'} or {@code 'y'}
 * @param openDegrees  full-open angle (multiplied by lidOpenness)
 * @param yawOffset    extra Y rotation with facing (degrees)
 * @param translucent  lid (and glass) use entityTranslucent; body stays cutout
 */
public record HingedPropSpec(
        ResourceLocation body,
        ResourceLocation lid,
        float hingeX,
        float hingeY,
        float hingeZ,
        char axis,
        float openDegrees,
        float yawOffset,
        boolean translucent
) {
    public static HingedPropSpec of(String name, float hx, float hy, float hz, char axis, float openDegrees) {
        return of(name, hx, hy, hz, axis, openDegrees, 0.0F, false);
    }

    public static HingedPropSpec of(String name, float hx, float hy, float hz, char axis, float openDegrees,
                                    float yawOffset, boolean translucent) {
        return new HingedPropSpec(
                new ResourceLocation(Cts.MODID, "block/" + name + "_body"),
                new ResourceLocation(Cts.MODID, "block/" + name + "_lid"),
                hx / 16.0F,
                hy / 16.0F,
                hz / 16.0F,
                axis,
                openDegrees,
                yawOffset,
                translucent);
    }
}
