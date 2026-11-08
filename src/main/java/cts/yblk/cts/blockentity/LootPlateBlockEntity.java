package cts.yblk.cts.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import cts.yblk.cts.item.TagSlipItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.block.LootPlateBlock;
import cts.yblk.cts.config.CtsConfig;
import cts.yblk.cts.menu.LootPlateMenu;
import cts.yblk.cts.registry.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;

public class LootPlateBlockEntity extends BlockEntity implements MenuProvider {
    public enum Mode {
        ITEMS,
        LOOT_TABLE
    }

    public static final int SLOT_COUNT = 27;
    public static final int MAX_RESPAWN_SECONDS = 7 * 24 * 60 * 60;
    /** Fallback / legacy constant; prefer {@link CtsConfig#defaultLootRespawnSeconds()}. */
    public static final int DEFAULT_RESPAWN_SECONDS = CtsConfig.FALLBACK_LOOT_RESPAWN_SECONDS;

    private final int[] chances = SpawnChance.array(SLOT_COUNT);
    private final int[] countMin = SpawnChance.counts(SLOT_COUNT);
    private final int[] countMax = SpawnChance.counts(SLOT_COUNT);
    private final boolean[] randomDamage = new boolean[SLOT_COUNT];
    private final boolean[] randomEnchant = new boolean[SLOT_COUNT];

    private final ItemStackHandler template = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            if (getStackInSlot(slot).isEmpty()) {
                chances[slot] = SpawnChance.DEFAULT;
                countMin[slot] = 1;
                countMax[slot] = 1;
                randomDamage[slot] = false;
                randomEnchant[slot] = false;
            }
            if (level != null && !level.isClientSide) {
                spawnIfNeeded();
                updateConfiguredState();
            }
            setChanged();
            syncToClient();
        }
    };

    private ItemStack displayedItem = ItemStack.EMPTY;
    private String lootTableId = "";
    private Mode mode = Mode.ITEMS;
    private int respawnSeconds = DEFAULT_RESPAWN_SECONDS;
    private int cooldownTicks;
    private float spin;

    public LootPlateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOOT_PLATE.get(), pos, state);
        this.respawnSeconds = CtsConfig.defaultLootRespawnSeconds();
    }

    public ItemStackHandler getTemplate() {
        return template;
    }

    public int getChance(int slot) {
        return chances[slot];
    }

    public int getCountMin(int slot) {
        return countMin[slot];
    }

    public int getCountMax(int slot) {
        return countMax[slot];
    }

    public boolean isRandomDamage(int slot) {
        return randomDamage[slot];
    }

    public boolean isRandomEnchant(int slot) {
        return randomEnchant[slot];
    }

    public void placeTemplate(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        if (stack.isEmpty()) {
            chances[slot] = SpawnChance.DEFAULT;
            countMin[slot] = 1;
            countMax[slot] = 1;
            randomDamage[slot] = false;
            randomEnchant[slot] = false;
            template.setStackInSlot(slot, ItemStack.EMPTY);
            return;
        }
        boolean damageable = stack.isDamageableItem();
        int count = damageable ? 1 : SpawnChance.clampCount(stack.getCount());
        chances[slot] = SpawnChance.DEFAULT;
        countMin[slot] = count;
        countMax[slot] = count;
        randomDamage[slot] = false;
        randomEnchant[slot] = false;
        ItemStack visual = stack.copy();
        visual.setCount(1);
        template.setStackInSlot(slot, visual);
    }

    public void setEntry(int slot, int min, int max, int chance, boolean randomDurability, boolean randomEnchants, String tag, String pick) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        ItemStack stack = template.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        if (stack.isDamageableItem()) {
            randomDamage[slot] = randomDurability;
            countMin[slot] = 1;
            countMax[slot] = 1;
        } else {
            randomDamage[slot] = false;
            countMin[slot] = SpawnChance.clampCount(Math.min(min, max));
            countMax[slot] = SpawnChance.clampCount(Math.max(min, max));
        }
        randomEnchant[slot] = randomEnchants && SpawnChance.canRandomEnchant(stack);
        chances[slot] = SpawnChance.clamp(chance);
        stack.setCount(1);
        TagSlipItem.apply(stack, tag, pick);
        template.setStackInSlot(slot, stack);
    }

    public ItemStack getDisplayedItem() {
        return displayedItem;
    }

    public String getLootTableId() {
        return lootTableId;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public int getRespawnSeconds() {
        return respawnSeconds;
    }

    public float getSpin() {
        return spin;
    }

    public static boolean canConfigure(Player player) {
        return player.getAbilities().instabuild;
    }

    public boolean isConfigured() {
        return mode == Mode.LOOT_TABLE ? !lootTableId.isBlank() : hasTemplateItems();
    }

    private boolean hasTemplateItems() {
        for (int i = 0; i < template.getSlots(); i++) {
            if (!template.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void applySettings(Mode newMode, String lootTable, int seconds) {
        this.mode = newMode;
        this.lootTableId = newMode == Mode.LOOT_TABLE ? sanitizeLootTable(lootTable) : this.lootTableId;
        this.respawnSeconds = Math.max(0, Math.min(MAX_RESPAWN_SECONDS, seconds));
        spawnIfNeeded();
        updateConfiguredState();
        setChanged();
        syncToClient();
    }

    public void spawnIfNeeded() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (displayedItem.isEmpty() && isConfigured() && cooldownTicks >= 0) {
            spawnFromSource();
        } else if (!isConfigured()) {
            displayedItem = ItemStack.EMPTY;
            cooldownTicks = 0;
        }
    }

    public boolean tryPickup(Player player) {
        if (displayedItem.isEmpty()) {
            return false;
        }
        ItemStack taken = displayedItem.copy();
        displayedItem = ItemStack.EMPTY;
        cooldownTicks = respawnSeconds <= 0 ? -1 : respawnSeconds * 20;
        if (!player.getInventory().add(taken)) {
            ItemEntity dropped = player.drop(taken, false);
            if (dropped != null) {
                dropped.setNoPickUpDelay();
            }
        }
        if (level != null) {
            float pitch = (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F;
            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, pitch * 2.0F);
        }
        setChanged();
        syncToClient();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LootPlateBlockEntity plate) {
        if (!plate.displayedItem.isEmpty() || !plate.isConfigured() || plate.cooldownTicks < 0) {
            return;
        }
        if (plate.cooldownTicks > 0) {
            plate.cooldownTicks--;
        }
        if (plate.cooldownTicks == 0) {
            plate.spawnFromSource();
            plate.setChanged();
            plate.syncToClient();
        }
    }

    private void spawnFromSource() {
        displayedItem = rollLoot().copy();
        if (displayedItem.isEmpty()) {
            cooldownTicks = respawnSeconds <= 0 ? -1 : Math.max(20, respawnSeconds * 20);
        } else {
            cooldownTicks = 0;
            if (level != null) {
                spin = level.random.nextFloat() * 360.0F;
            }
        }
    }

    private ItemStack rollLoot() {
        if (mode == Mode.LOOT_TABLE) {
            if (!lootTableId.isBlank() && level instanceof ServerLevel serverLevel) {
                ResourceLocation id = ResourceLocation.tryParse(lootTableId);
                if (id != null) {
                    LootTable table = serverLevel.getServer().getLootData().getLootTable(id);
                    if (table != LootTable.EMPTY) {
                        LootParams params = new LootParams.Builder(serverLevel)
                                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                                .create(LootContextParamSets.CHEST);
                        List<ItemStack> stacks = table.getRandomItems(params);
                        List<ItemStack> options = new ArrayList<>();
                        for (ItemStack stack : stacks) {
                            if (!stack.isEmpty()) {
                                options.add(stack);
                            }
                        }
                        if (!options.isEmpty()) {
                            return options.get(serverLevel.random.nextInt(options.size()));
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        }
        return pickRandomTemplate();
    }

    private ItemStack pickRandomTemplate() {
        if (level == null) {
            return ItemStack.EMPTY;
        }
        int slot = SpawnChance.pickWeighted(level.random, SLOT_COUNT, index -> !template.getStackInSlot(index).isEmpty(), index -> chances[index]);
        if (slot < 0) {
            return ItemStack.EMPTY;
        }
        return SpawnChance.spawnStack(template.getStackInSlot(slot), level.random, countMin[slot], countMax[slot], randomDamage[slot], randomEnchant[slot]);
    }

    private void updateConfiguredState() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        boolean configured = isConfigured();
        if (state.hasProperty(LootPlateBlock.CONFIGURED) && state.getValue(LootPlateBlock.CONFIGURED) != configured) {
            level.setBlock(worldPosition, state.setValue(LootPlateBlock.CONFIGURED, configured), Block.UPDATE_CLIENTS);
        }
    }

    private static String sanitizeLootTable(String raw) {
        return cts.yblk.cts.util.LootTableIds.normalize(raw);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.cts.loot_plate");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new LootPlateMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Template", template.serializeNBT());
        tag.putIntArray("Chances", chances);
        tag.putIntArray("CountMin", countMin);
        tag.putIntArray("CountMax", countMax);
        tag.putIntArray("RandomDamage", SpawnChance.flagArray(randomDamage));
        tag.putIntArray("RandomEnchant", SpawnChance.flagArray(randomEnchant));
        tag.put("Displayed", displayedItem.save(new CompoundTag()));
        tag.putString("LootTable", lootTableId);
        tag.putString("Mode", mode.name());
        tag.putInt("RespawnSeconds", respawnSeconds);
        tag.putInt("Cooldown", cooldownTicks);
        tag.putFloat("Spin", spin);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Template")) {
            template.deserializeNBT(tag.getCompound("Template"));
        }
        SpawnChance.load(tag, chances);
        SpawnChance.loadCounts(tag, countMin, countMax, template);
        SpawnChance.loadFlags(tag, randomDamage);
        SpawnChance.loadFlags(tag, "RandomEnchant", randomEnchant);
        displayedItem = ItemStack.of(tag.getCompound("Displayed"));
        lootTableId = tag.getString("LootTable");
        try {
            mode = tag.contains("Mode") ? Mode.valueOf(tag.getString("Mode")) : Mode.ITEMS;
        } catch (IllegalArgumentException ignored) {
            mode = Mode.ITEMS;
        }
        respawnSeconds = tag.contains("RespawnSeconds")
                ? tag.getInt("RespawnSeconds")
                : CtsConfig.defaultLootRespawnSeconds();
        cooldownTicks = tag.getInt("Cooldown");
        spin = tag.getFloat("Spin");
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
