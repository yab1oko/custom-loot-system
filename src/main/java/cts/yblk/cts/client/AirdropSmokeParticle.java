package cts.yblk.cts.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Campfire signal smoke behaviour with a golden tint. */
@OnlyIn(Dist.CLIENT)
public class AirdropSmokeParticle extends TextureSheetParticle {
    private AirdropSmokeParticle(ClientLevel level, double x, double y, double z,
                                 double xd, double yd, double zd) {
        super(level, x, y, z);
        this.scale(3.0F);
        this.setSize(0.25F, 0.25F);
        this.lifetime = this.random.nextInt(50) + 280;
        this.gravity = 3.0E-6F;
        this.xd = xd;
        this.yd = yd + (double) (this.random.nextFloat() / 500.0F);
        this.zd = zd;
        this.setAlpha(0.92F);
        // Soft golden — less fiery orange
        this.setColor(0.95F, 0.78F, 0.32F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.xd += (double) (this.random.nextFloat() / 5000.0F * (float) (this.random.nextBoolean() ? 1 : -1));
            this.zd += (double) (this.random.nextFloat() / 5000.0F * (float) (this.random.nextBoolean() ? 1 : -1));
            this.yd -= (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }
        } else {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            AirdropSmokeParticle particle = new AirdropSmokeParticle(level, x, y, z, xd, yd, zd);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
