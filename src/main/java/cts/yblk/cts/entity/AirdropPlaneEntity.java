package cts.yblk.cts.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import cts.yblk.cts.registry.ModEntities;
import cts.yblk.cts.registry.ModSounds;

/**
 * Short cargo fly-by. Releases the existing parachute crate, then leaves.
 * Nothing about it is kept after the pass.
 */
public class AirdropPlaneEntity extends Entity {
    private static final double SPEED = 0.85D;
    private static final double APPROACH = 72.0D;
    private static final double DEPART = 80.0D;

    private double targetX;
    private double targetZ;
    private double headingX = 1.0D;
    private double headingZ;
    private String lootTable = "";
    private boolean released;

    public AirdropPlaneEntity(EntityType<? extends AirdropPlaneEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AirdropPlaneEntity create(Level level, double dropX, double dropY, double dropZ, String lootTable) {
        AirdropPlaneEntity plane = ModEntities.AIRDROP_PLANE.get().create(level);
        if (plane == null) {
            return null;
        }
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        plane.headingX = Math.cos(angle);
        plane.headingZ = Math.sin(angle);
        plane.targetX = dropX;
        plane.targetZ = dropZ;
        plane.lootTable = lootTable == null ? "" : lootTable;
        plane.setPos(dropX - plane.headingX * APPROACH, dropY, dropZ - plane.headingZ * APPROACH);
        plane.setYRot((float) (Mth.atan2(plane.headingZ, plane.headingX) * Mth.RAD_TO_DEG));
        plane.setDeltaMovement(plane.headingX * SPEED, 0.0D, plane.headingZ * SPEED);
        return plane;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(headingX * SPEED, 0.0D, headingZ * SPEED);
        setPos(getX() + getDeltaMovement().x, getY(), getZ() + getDeltaMovement().z);
        setYRot((float) (Mth.atan2(headingZ, headingX) * Mth.RAD_TO_DEG));

        if (level().isClientSide) {
            if (tickCount % 12 == 0) {
                level().playLocalSound(getX(), getY(), getZ(), SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL, 0.55F, 0.65F, false);
            }
            return;
        }

        double dx = targetX - getX();
        double dz = targetZ - getZ();
        if (!released && dx * headingX + dz * headingZ <= SPEED) {
            released = true;
            AirdropEntity crate = AirdropEntity.create(level(), targetX, getY(), targetZ, lootTable);
            if (crate != null) {
                level().addFreshEntity(crate);
                level().playSound(null, targetX, getY(), targetZ, ModSounds.AIRDROP.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            }
        }
        if (released) {
            double past = (getX() - targetX) * headingX + (getZ() - targetZ) * headingZ;
            if (past > DEPART) {
                discard();
            }
        } else if (tickCount > 20 * 40) {
            discard();
        }
    }

    public float yaw() {
        return getYRot();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        targetX = tag.getDouble("TargetX");
        targetZ = tag.getDouble("TargetZ");
        headingX = tag.getDouble("HeadingX");
        headingZ = tag.getDouble("HeadingZ");
        lootTable = tag.getString("LootTable");
        released = tag.getBoolean("Released");
        if (headingX == 0.0D && headingZ == 0.0D) {
            headingX = 1.0D;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("TargetX", targetX);
        tag.putDouble("TargetZ", targetZ);
        tag.putDouble("HeadingX", headingX);
        tag.putDouble("HeadingZ", headingZ);
        tag.putString("LootTable", lootTable);
        tag.putBoolean("Released", released);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0D * 256.0D;
    }

    public Vec3 heading() {
        return new Vec3(headingX, 0.0D, headingZ);
    }

    public BlockPos target() {
        return BlockPos.containing(targetX, getY(), targetZ);
    }
}
