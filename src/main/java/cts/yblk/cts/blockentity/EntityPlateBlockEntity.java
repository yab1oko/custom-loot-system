package cts.yblk.cts.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.block.EntityPlateBlock;
import cts.yblk.cts.menu.EntityPlateMenu;
import cts.yblk.cts.registry.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityPlateBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ID_COUNT = 9;
    public static final int MAX_COUNT = 8;
    public static final int MAX_RADIUS = 32;
    public static final int MAX_RESPAWN_SECONDS = 7 * 24 * 60 * 60;
    public static final int RELEASE_DISTANCE = 100;
    public static final String ENTITY_TAG = "CtsEntityPlate";

    private final String[] entityIds = new String[ID_COUNT];
    private final int[] chances = SpawnChance.array(ID_COUNT);
    private boolean randomize = true;
    private int count = 1;
    private int respawnSeconds = 60;
    private int radius = 4;
    private int cooldownTicks;
    private boolean armCooldown;
    private int spawnGraceTicks;
    private final List<UUID> tracked = new ArrayList<>();

    public EntityPlateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENTITY_PLATE.get(), pos, state);
        for (int i = 0; i < entityIds.length; i++) {
            entityIds[i] = "";
        }
    }

    public String getEntityId(int index) {
        return entityIds[index];
    }

    public int getChance(int index) {
        return chances[index];
    }

    public boolean isRandomize() {
        return randomize;
    }

    public int getCount() {
        return count;
    }

    public int getRespawnSeconds() {
        return respawnSeconds;
    }

    public int getRadius() {
        return radius;
    }

    public static boolean canConfigure(Player player) {
        return player.getAbilities().instabuild;
    }

    public boolean isConfigured() {
        for (String id : entityIds) {
            if (resolve(id) != null) {
                return true;
            }
        }
        return false;
    }

    public void applySettings(String[] ids, int[] newChances, boolean randomize, int count, int seconds, int radius) {
        for (int i = 0; i < ID_COUNT; i++) {
            entityIds[i] = i < ids.length && ids[i] != null ? ids[i].trim() : "";
            chances[i] = newChances != null && i < newChances.length ? SpawnChance.clamp(newChances[i]) : SpawnChance.DEFAULT;
        }
        this.randomize = randomize;
        this.count = Math.max(1, Math.min(MAX_COUNT, count));
        this.respawnSeconds = Math.max(0, Math.min(MAX_RESPAWN_SECONDS, seconds));
        this.radius = Math.max(0, Math.min(MAX_RADIUS, radius));
        setChanged();
        updateConfiguredState();
        syncToClient();
    }

    public void onSpawnedMobDeath(UUID id) {
        tracked.remove(id);
        spawnGraceTicks = 0;
        if (cooldownTicks <= 0) {
            armCooldown = true;
        }
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EntityPlateBlockEntity plate) {
        if (!(level instanceof ServerLevel server) || !plate.isConfigured()) {
            return;
        }
        if (plate.spawnGraceTicks > 0) {
            plate.spawnGraceTicks--;
        }
        if (plate.cooldownTicks > 0) {
            plate.cooldownTicks--;
            if (plate.cooldownTicks == 0) {
                plate.setChanged();
            }
        }
        if (plate.hasBlockingEntities(server)) {
            plate.armCooldown = true;
            plate.spawnGraceTicks = 0;
            return;
        }
        if (plate.spawnGraceTicks > 0) {
            return;
        }
        if (plate.armCooldown) {
            plate.armCooldown = false;
            plate.cooldownTicks = plate.respawnSeconds * 20;
            plate.setChanged();
            if (plate.cooldownTicks > 0) {
                return;
            }
        }
        if (plate.cooldownTicks > 0) {
            return;
        }
        plate.spawnWave(server);
    }

    private boolean hasBlockingEntities(ServerLevel level) {
        boolean blocking = false;
        boolean changed = false;
        var iterator = tracked.iterator();
        while (iterator.hasNext()) {
            Entity entity = level.getEntity(iterator.next());
            if (entity == null) {
                if (spawnGraceTicks <= 0) {
                    iterator.remove();
                    changed = true;
                }
                continue;
            }
            if (!stillGuarding(entity)) {
                iterator.remove();
                changed = true;
                continue;
            }
            blocking = true;
        }
        AABB area = new AABB(worldPosition).inflate(RELEASE_DISTANCE);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, area, this::belongsToThisPlate)) {
            if (!stillGuarding(entity) || tracked.contains(entity.getUUID())) {
                continue;
            }
            tracked.add(entity.getUUID());
            changed = true;
            blocking = true;
        }
        if (changed) {
            setChanged();
        }
        return blocking;
    }

    private boolean belongsToThisPlate(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.contains(ENTITY_TAG) && data.getLong(ENTITY_TAG) == worldPosition.asLong();
    }

    private boolean stillGuarding(Entity entity) {
        if (entity == null || entity.isRemoved()) {
            return false;
        }
        if (entity instanceof LivingEntity living && living.getHealth() <= 0.0F) {
            return false;
        }
        if (!entity.isAlive()) {
            return false;
        }
        return entity.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= (double) RELEASE_DISTANCE * RELEASE_DISTANCE;
    }

    private void spawnWave(ServerLevel level) {
        List<Integer> slots = configuredSlots();
        if (slots.isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        boolean spawned = false;
        if (randomize) {
            int spawnedCount = 0;
            int attempts = 0;
            while (spawnedCount < count && attempts < count * 8) {
                attempts++;
                int slot = SpawnChance.pickWeighted(random, ID_COUNT,
                        index -> chances[index] > 0 && resolve(entityIds[index]) != null,
                        index -> chances[index]);
                if (slot < 0) {
                    break;
                }
                if (spawnOne(level, resolve(entityIds[slot]), random)) {
                    spawned = true;
                    spawnedCount++;
                }
            }
        } else {
            for (int slot : slots) {
                if (chances[slot] <= 0) {
                    continue;
                }
                EntityType<?> type = resolve(entityIds[slot]);
                if (type == null) {
                    continue;
                }
                for (int i = 0; i < count; i++) {
                    spawned |= spawnOne(level, type, random);
                }
            }
        }
        if (spawned) {
            level.playSound(null, worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.25F, 0.8F);
            armCooldown = true;
            spawnGraceTicks = 40;
        } else {
            armCooldown = false;
            cooldownTicks = Math.max(20, respawnSeconds * 20);
        }
        setChanged();
    }

    private boolean spawnOne(ServerLevel level, EntityType<?> type, RandomSource random) {
        for (int attempt = 0; attempt < 8; attempt++) {
            Entity entity = type.create(level);
            if (entity == null) {
                return false;
            }
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = radius <= 0 ? 0.0D : Math.sqrt(random.nextDouble()) * radius;
            double x = worldPosition.getX() + 0.5D + Math.cos(angle) * distance;
            double z = worldPosition.getZ() + 0.5D + Math.sin(angle) * distance;
            int y = findSpawnY(level, x, z);
            entity.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), MobSpawnType.SPAWNER, null, null);
                mob.setPersistenceRequired();
            }
            entity.getPersistentData().putLong(ENTITY_TAG, worldPosition.asLong());
            if (level.addFreshEntity(entity)) {
                tracked.add(entity.getUUID());
                return true;
            }
        }
        return false;
    }

    private int findSpawnY(ServerLevel level, double x, double z) {
        int base = worldPosition.getY();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 2; dy >= -4; dy--) {
            cursor.set(x, base + dy, z);
            BlockPos below = cursor.below();
            if (level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)
                    && level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                return cursor.getY();
            }
        }
        return base;
    }

    private List<Integer> configuredSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < entityIds.length; i++) {
            if (resolve(entityIds[i]) != null) {
                slots.add(i);
            }
        }
        return slots;
    }

    @Nullable
    private static EntityType<?> resolve(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        ResourceLocation id = trimmed.contains(":")
                ? ResourceLocation.tryParse(trimmed)
                : ResourceLocation.tryParse("minecraft:" + trimmed);
        if (id == null) {
            return null;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null || !type.canSummon() || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
            return null;
        }
        return type;
    }

    private void updateConfiguredState() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        boolean configured = isConfigured();
        if (state.hasProperty(EntityPlateBlock.CONFIGURED) && state.getValue(EntityPlateBlock.CONFIGURED) != configured) {
            level.setBlock(worldPosition, state.setValue(EntityPlateBlock.CONFIGURED, configured), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EntityPlateMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag ids = new ListTag();
        for (String entityId : entityIds) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", entityId);
            ids.add(entry);
        }
        tag.put("EntityIds", ids);
        tag.putIntArray("Chances", chances);
        tag.putBoolean("Randomize", randomize);
        tag.putInt("Count", count);
        tag.putInt("RespawnSeconds", respawnSeconds);
        tag.putInt("Radius", radius);
        tag.putInt("Cooldown", cooldownTicks);
        tag.putBoolean("ArmCooldown", armCooldown);
        tag.putInt("SpawnGrace", spawnGraceTicks);
        ListTag trackedTag = new ListTag();
        for (UUID id : tracked) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", id);
            trackedTag.add(entry);
        }
        tag.put("Tracked", trackedTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < entityIds.length; i++) {
            entityIds[i] = "";
        }
        ListTag ids = tag.getList("EntityIds", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(ID_COUNT, ids.size()); i++) {
            entityIds[i] = ids.getCompound(i).getString("Id");
        }
        SpawnChance.load(tag, chances);
        randomize = !tag.contains("Randomize") || tag.getBoolean("Randomize");
        count = tag.contains("Count") ? tag.getInt("Count") : 1;
        respawnSeconds = tag.contains("RespawnSeconds") ? tag.getInt("RespawnSeconds") : 60;
        radius = tag.contains("Radius") ? tag.getInt("Radius") : 4;
        cooldownTicks = tag.getInt("Cooldown");
        armCooldown = tag.getBoolean("ArmCooldown");
        spawnGraceTicks = tag.getInt("SpawnGrace");
        tracked.clear();
        ListTag trackedTag = tag.getList("Tracked", Tag.TAG_COMPOUND);
        for (int i = 0; i < trackedTag.size(); i++) {
            if (trackedTag.getCompound(i).hasUUID("Id")) {
                tracked.add(trackedTag.getCompound(i).getUUID("Id"));
            }
        }
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
