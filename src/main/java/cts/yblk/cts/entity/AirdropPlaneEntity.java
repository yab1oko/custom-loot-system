package cts.yblk.cts.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import cts.yblk.cts.registry.ModEntities;

/**
 * Cargo fly-by. The server moves it in a straight line. The client only renders and plays the engine.
 */
public class AirdropPlaneEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_FADE =
            SynchedEntityData.defineId(AirdropPlaneEntity.class, EntityDataSerializers.FLOAT);

    private static final double SPEED = 0.33D;
    /** Path length matches the 9 second engine clip at this speed. */
    private static final double APPROACH = 36.0D;
    private static final double DEPART = 23.0D;

    private double targetX;
    private double targetZ;
    private double headingX = 1.0D;
    private double headingZ;
    private String lootTable = "";
    private boolean released;
    private double clientX;
    private double clientY;
    private double clientZ;
    private int clientSteps;
    private float shownFade;

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
        plane.faceHeading();
        return plane;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_FADE, 0.0F);
    }

    public float fade() {
        return level().isClientSide ? shownFade : this.entityData.get(DATA_FADE);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        this.clientX = x;
        this.clientY = y;
        this.clientZ = z;
        this.clientSteps = Math.max(steps, 5);
        this.setYRot(yRot);
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            this.xo = getX();
            this.yo = getY();
            this.zo = getZ();
            this.yRotO = getYRot();
            if (clientSteps > 0) {
                setPos(getX() + (clientX - getX()) / clientSteps,
                        getY() + (clientY - getY()) / clientSteps,
                        getZ() + (clientZ - getZ()) / clientSteps);
                clientSteps--;
            }
            float target = this.entityData.get(DATA_FADE);
            shownFade += (target - shownFade) * 0.2F;
            return;
        }
        super.tick();
        setDeltaMovement(headingX * SPEED, 0.0D, headingZ * SPEED);
        setPos(getX() + getDeltaMovement().x, getY(), getZ() + getDeltaMovement().z);
        faceHeading();

        double traveled = (getX() - (targetX - headingX * APPROACH)) * headingX
                + (getZ() - (targetZ - headingZ * APPROACH)) * headingZ;
        float fadeIn = Mth.clamp((float) (traveled / 16.0D), 0.0F, 1.0F);
        float fadeOut = 1.0F;
        double dx = targetX - getX();
        double dz = targetZ - getZ();
        if (!released && dx * headingX + dz * headingZ <= SPEED) {
            released = true;
            AirdropEntity crate = AirdropEntity.create(level(), targetX, getY(), targetZ, lootTable);
            if (crate != null) {
                level().addFreshEntity(crate);
            }
        }
        if (released) {
            double past = (getX() - targetX) * headingX + (getZ() - targetZ) * headingZ;
            fadeOut = Mth.clamp(1.0F - (float) (past / 24.0D), 0.0F, 1.0F);
            if (past > DEPART) {
                discard();
                return;
            }
        } else if (tickCount > 20 * 80) {
            discard();
            return;
        }
        this.entityData.set(DATA_FADE, fadeIn * fadeOut);
    }

    private void faceHeading() {
        float yaw = (float) (Mth.atan2(-headingX, headingZ) * Mth.RAD_TO_DEG);
        setYRot(yaw);
        setYHeadRot(yaw);
        yRotO = yaw;
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
