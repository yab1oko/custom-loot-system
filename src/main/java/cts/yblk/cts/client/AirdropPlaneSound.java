package cts.yblk.cts.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import cts.yblk.cts.entity.AirdropPlaneEntity;
import cts.yblk.cts.registry.ModSounds;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine loop that follows the plane and fades with it.
 */
@OnlyIn(Dist.CLIENT)
public class AirdropPlaneSound extends AbstractTickableSoundInstance {
    private static final Set<Integer> STARTED = ConcurrentHashMap.newKeySet();
    private final AirdropPlaneEntity plane;

    public static boolean isPlaying(AirdropPlaneEntity plane) {
        return STARTED.contains(plane.getId());
    }

    public static void mark(AirdropPlaneEntity plane) {
        STARTED.add(plane.getId());
    }

    public AirdropPlaneSound(AirdropPlaneEntity plane) {
        super(ModSounds.AIRDROP.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.plane = plane;
        this.looping = false;
        this.delay = 0;
        this.volume = 0.02F;
        this.pitch = 1.0F;
        this.x = plane.getX();
        this.y = plane.getY();
        this.z = plane.getZ();
        this.relative = false;
    }

    @Override
    public void tick() {
        if (plane.isRemoved()) {
            STARTED.remove(plane.getId());
            stop();
            return;
        }
        this.x = plane.getX();
        this.y = plane.getY();
        this.z = plane.getZ();
        float fade = plane.fade();
        this.volume = 0.05F + 0.9F * fade;
    }
}
