package cts.yblk.cts.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import cts.yblk.cts.registry.ModBlocks;

/** Break crumbs use the airdrop crate texture (parts otherwise resolve to missing/air). */
public final class AirdropBreakParticles {
    public static final IClientBlockExtensions EXTENSION = new IClientBlockExtensions() {
        @Override
        public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine engine) {
            if (!(level instanceof ClientLevel client)) {
                return true;
            }
            BlockState crumb = ModBlocks.AIRDROP.get().defaultBlockState();
            AABB bounds = state.getShape(level, pos).bounds();
            double sizeX = Math.max(bounds.maxX - bounds.minX, 0.25D);
            double sizeY = Math.max(bounds.maxY - bounds.minY, 0.25D);
            double sizeZ = Math.max(bounds.maxZ - bounds.minZ, 0.25D);
            int countX = Math.min(4, Math.max(2, Mth.ceil(sizeX / 0.25D)));
            int countY = Math.min(4, Math.max(2, Mth.ceil(sizeY / 0.25D)));
            int countZ = Math.min(4, Math.max(2, Mth.ceil(sizeZ / 0.25D)));
            for (int x = 0; x < countX; x++) {
                for (int y = 0; y < countY; y++) {
                    for (int z = 0; z < countZ; z++) {
                        double fractionX = (x + 0.5D) / countX;
                        double fractionY = (y + 0.5D) / countY;
                        double fractionZ = (z + 0.5D) / countZ;
                        engine.add(new TerrainParticle(client,
                                pos.getX() + fractionX * sizeX + bounds.minX,
                                pos.getY() + fractionY * sizeY + bounds.minY,
                                pos.getZ() + fractionZ * sizeZ + bounds.minZ,
                                fractionX - 0.5D, fractionY - 0.5D, fractionZ - 0.5D,
                                crumb, pos).updateSprite(crumb, pos));
                    }
                }
            }
            return true;
        }
    };

    private AirdropBreakParticles() {
    }
}
