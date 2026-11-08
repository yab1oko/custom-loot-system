package cts.yblk.cts.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import cts.yblk.cts.block.AirdropStructure;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.config.CtsConfig;
import cts.yblk.cts.registry.ModEntities;
import cts.yblk.cts.util.AirdropSmoke;
import cts.yblk.cts.util.LootTableIds;

public class AirdropEntity extends Entity {
    private static final EntityDataAccessor<String> LOOT_TABLE =
            SynchedEntityData.defineId(AirdropEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DRIFT_ANGLE =
            SynchedEntityData.defineId(AirdropEntity.class, EntityDataSerializers.FLOAT);
    /** Radians per tick; sign chooses which way the arc bends. */
    private static final EntityDataAccessor<Float> CURVE_RATE =
            SynchedEntityData.defineId(AirdropEntity.class, EntityDataSerializers.FLOAT);
    private static final double FALL_SPEED = -0.10D;
    private static final double DRIFT_SPEED = 0.032D;
    private static final float CURVE_RATE_MIN = 0.006F;
    private static final float CURVE_RATE_MAX = 0.014F;
    private static final int DEFAULT_DROP_HEIGHT = 48;

    public final AnimationState flyAnimationState = new AnimationState();
    private boolean landed;

    public AirdropEntity(EntityType<? extends AirdropEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AirdropEntity create(Level level, double x, double y, double z, String lootTable) {
        AirdropEntity entity = ModEntities.AIRDROP.get().create(level);
        if (entity == null) {
            return null;
        }
        entity.setPos(x, y, z);
        entity.setLootTable(lootTable);
        entity.entityData.set(DRIFT_ANGLE, level.random.nextFloat() * Mth.TWO_PI);
        float rate = CURVE_RATE_MIN + level.random.nextFloat() * (CURVE_RATE_MAX - CURVE_RATE_MIN);
        if (level.random.nextBoolean()) {
            rate = -rate;
        }
        entity.entityData.set(CURVE_RATE, rate);
        entity.setDeltaMovement(0.0D, FALL_SPEED, 0.0D);
        return entity;
    }

    public static int dropHeight() {
        return DEFAULT_DROP_HEIGHT;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LOOT_TABLE, CtsConfig.defaultAirdropLootTable());
        this.entityData.define(DRIFT_ANGLE, 0.0F);
        this.entityData.define(CURVE_RATE, 0.01F);
    }

    public void setLootTable(String id) {
        String normalized = LootTableIds.normalize(id);
        this.entityData.set(LOOT_TABLE, normalized.isEmpty() ? CtsConfig.defaultAirdropLootTable() : normalized);
    }

    public String getLootTable() {
        return this.entityData.get(LOOT_TABLE);
    }

    /** Smooth arc: heading turns steadily one way — no oscillating sway. */
    private Vec3 curvedFallDelta() {
        float angle = this.entityData.get(DRIFT_ANGLE) + tickCount * this.entityData.get(CURVE_RATE);
        return new Vec3(
                Math.cos(angle) * DRIFT_SPEED,
                FALL_SPEED,
                Math.sin(angle) * DRIFT_SPEED);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            flyAnimationState.startIfStopped(this.tickCount);
        }
        if (landed) {
            return;
        }

        Vec3 movement = curvedFallDelta();
        setDeltaMovement(movement);
        setPos(getX() + movement.x, getY() + movement.y, getZ() + movement.z);

        if (level().isClientSide) {
            AirdropSmoke.clientColumn(level(), getX(), getY() + 0.4D, getZ(), random);
            AirdropSmoke.clientColumn(level(), getX(), getY() + 0.4D, getZ(), random);
            return;
        }

        BlockPos feet = BlockPos.containing(getX(), getY() - 0.05D, getZ());
        boolean nearGround = !level().getBlockState(feet).getCollisionShape(level(), feet).isEmpty()
                || onGround()
                || verticalCollision;
        if (nearGround) {
            land();
        }
    }

    private void land() {
        if (landed || level().isClientSide) {
            return;
        }
        landed = true;
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int bx = Mth.floor(getX());
        int bz = Mth.floor(getZ());
        BlockPos master = AirdropStructure.pickMaster(level(), bx, bz, facing, random);
        if (!AirdropStructure.canPlace(level(), master, facing)) {
            discard();
            return;
        }
        AirdropStructure.place(level(), master, facing);
        if (level().getBlockEntity(master) instanceof CanStashBlockEntity stash) {
            stash.setupAirdrop(getLootTable());
        }
        level().playSound(null, master, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setLootTable(tag.getString("LootTable"));
        landed = tag.getBoolean("Landed");
        if (tag.contains("DriftAngle")) {
            this.entityData.set(DRIFT_ANGLE, tag.getFloat("DriftAngle"));
        }
        if (tag.contains("CurveRate")) {
            this.entityData.set(CURVE_RATE, tag.getFloat("CurveRate"));
        } else if (tag.contains("SwayPhase")) {
            // Legacy: convert old sway seed into a mild curve rate
            float legacy = tag.getFloat("SwayPhase");
            float rate = CURVE_RATE_MIN + (legacy % 1.0F) * (CURVE_RATE_MAX - CURVE_RATE_MIN);
            this.entityData.set(CURVE_RATE, ((int) legacy & 1) == 0 ? rate : -rate);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("LootTable", getLootTable());
        tag.putBoolean("Landed", landed);
        tag.putFloat("DriftAngle", this.entityData.get(DRIFT_ANGLE));
        tag.putFloat("CurveRate", this.entityData.get(CURVE_RATE));
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
        return distance < 128.0D * 128.0D;
    }
}
