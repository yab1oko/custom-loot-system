package cts.yblk.cts.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.block.BrickCacheBlock;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.config.CtsConfig;
import cts.yblk.cts.block.CorpseBlock;
import cts.yblk.cts.block.DresserBlock;
import cts.yblk.cts.block.KitchenDrawerBlock;
import cts.yblk.cts.block.MedicalCrateBlock;
import cts.yblk.cts.block.ShelvingBlock;
import cts.yblk.cts.menu.CanStashConfigMenu;
import cts.yblk.cts.menu.CanStashMenu;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModSounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CanStashBlockEntity extends BlockEntity implements MenuProvider {
    public enum Mode {
        ITEMS,
        LOOT_TABLE
    }

    public static final int SLOT_COUNT = 27;
    public static final int TOOL_SLOTS = 9;
    public static final int DEFAULT_SEARCH_SECONDS = 3;
    public static final int SAFE_SEARCH_SECONDS = 12;
    public static final int AIRDROP_SEARCH_SECONDS = 20;
    public static final int MAX_SEARCH_SECONDS = 3600;
    public static final int SEARCH_TICKS = DEFAULT_SEARCH_SECONDS * 20;
    public static final int MAX_RESPAWN_SECONDS = 7 * 24 * 60 * 60;
    /** Fallback / legacy constant; prefer {@link CtsConfig#defaultLootRespawnSeconds()}. */
    public static final int DEFAULT_RESPAWN_SECONDS = CtsConfig.FALLBACK_LOOT_RESPAWN_SECONDS;
    /** Untouched airdrop despawns after 30 minutes. */
    public static final int AIRDROP_FULL_DESPAWN_TICKS = 30 * 60 * 20;
    /** Partially looted airdrop despawns after 5 minutes. */
    public static final int AIRDROP_PARTIAL_DESPAWN_TICKS = 5 * 60 * 20;

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
            setChanged();
        }
    };

    private boolean filling;
    private boolean airdropArmed;
    /** Ticks until airdrop despawns; 0 = not an active timed airdrop. */
    private int airdropDespawnTicks;
    private boolean airdropPartiallyLooted;
    private int airdropInitialItemCount;

    private final ItemStackHandler contents = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!filling && level != null && !level.isClientSide) {
                if (cooldownTicks <= 0 && respawnSeconds > 0) {
                    cooldownTicks = respawnSeconds * 20;
                }
                if (airdropArmed && getBlockState().getBlock() instanceof cts.yblk.cts.block.AirdropBlock) {
                    if (isEmpty(this)) {
                        cts.yblk.cts.block.AirdropBlock.destroyStructure(level, worldPosition);
                        return;
                    }
                    int remaining = countItems(this);
                    if (!airdropPartiallyLooted && remaining < airdropInitialItemCount) {
                        airdropPartiallyLooted = true;
                        airdropDespawnTicks = AIRDROP_PARTIAL_DESPAWN_TICKS;
                    }
                }
            }
            setChanged();
        }
    };

    private final ItemStackHandler tools = new ItemStackHandler(TOOL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private String lootTableId = "";
    private Mode mode = Mode.ITEMS;
    private boolean requiresTool;
    private int respawnSeconds = DEFAULT_RESPAWN_SECONDS;
    private int searchSeconds = DEFAULT_SEARCH_SECONDS;
    private int cooldownTicks;

    /** Client-only interpolated lid openness for chest-style renderers (0 closed .. 1 open). */
    private float lidOpenness;
    private float oLidOpenness;

    public CanStashBlockEntity(BlockPos pos, BlockState state) {
        super(blockEntityType(state), pos, state);
        this.searchSeconds = defaultSearchSeconds(state);
        this.respawnSeconds = CtsConfig.defaultLootRespawnSeconds();
    }

    public float getLidOpenness(float partialTick) {
        return net.minecraft.util.Mth.lerp(partialTick, oLidOpenness, lidOpenness);
    }

    public static int defaultSearchSeconds(BlockState state) {
        return state.is(ModBlocks.SAFE.get()) ? SAFE_SEARCH_SECONDS : DEFAULT_SEARCH_SECONDS;
    }

    private static net.minecraft.world.level.block.entity.BlockEntityType<?> blockEntityType(BlockState state) {
        if (state.is(ModBlocks.MEDKIT.get())) {
            return ModBlockEntities.MEDKIT.get();
        }
        if (state.is(ModBlocks.WASTE_DRUM.get())) {
            return ModBlockEntities.WASTE_DRUM.get();
        }
        if (state.is(ModBlocks.BOXES.get())) {
            return ModBlockEntities.BOXES.get();
        }
        if (state.is(ModBlocks.AMMO_BOX.get())) {
            return ModBlockEntities.AMMO_BOX.get();
        }
        if (state.is(ModBlocks.DIRTY_BACKPACK.get())) {
            return ModBlockEntities.DIRTY_BACKPACK.get();
        }
        if (state.is(ModBlocks.CLOTHES.get())) {
            return ModBlockEntities.CLOTHES.get();
        }
        if (state.is(ModBlocks.TOOLBOX.get())) {
            return ModBlockEntities.TOOLBOX.get();
        }
        if (state.is(ModBlocks.CRATE.get())) {
            return ModBlockEntities.CRATE.get();
        }
        if (state.is(ModBlocks.WEAPON_CASE.get())) {
            return ModBlockEntities.WEAPON_CASE.get();
        }
        if (state.is(ModBlocks.SAFE.get())) {
            return ModBlockEntities.SAFE.get();
        }
        if (state.is(ModBlocks.METAL_SCRAP.get())) {
            return ModBlockEntities.METAL_SCRAP.get();
        }
        if (state.is(ModBlocks.TRASH_CAN.get())) {
            return ModBlockEntities.TRASH_CAN.get();
        }
        if (state.is(ModBlocks.LITTER.get())) {
            return ModBlockEntities.LITTER.get();
        }
        if (state.getBlock() instanceof DresserBlock) {
            return ModBlockEntities.DRESSER.get();
        }
        if (state.getBlock() instanceof KitchenDrawerBlock) {
            return ModBlockEntities.KITCHEN_DRAWER.get();
        }
        if (state.getBlock() instanceof BrickCacheBlock) {
            return ModBlockEntities.BRICK_CACHE.get();
        }
        if (state.is(ModBlocks.NEST.get())) {
            return ModBlockEntities.NEST.get();
        }
        if (state.getBlock() instanceof ShelvingBlock) {
            return ModBlockEntities.SHELVING.get();
        }
        if (state.is(ModBlocks.CASH_REGISTER.get())) {
            return ModBlockEntities.CASH_REGISTER.get();
        }
        if (state.getBlock() instanceof MedicalCrateBlock) {
            return ModBlockEntities.MEDICAL_CRATE.get();
        }
        if (state.is(ModBlocks.LAUNDRY_BASKET.get())) {
            return ModBlockEntities.LAUNDRY_BASKET.get();
        }
        if (state.is(ModBlocks.TOILET.get())) {
            return ModBlockEntities.TOILET.get();
        }
        if (state.is(ModBlocks.FRIDGE.get())) {
            return ModBlockEntities.FRIDGE.get();
        }
        if (state.is(ModBlocks.WASHING_MACHINE.get())) {
            return ModBlockEntities.WASHING_MACHINE.get();
        }
        if (state.is(ModBlocks.MICROWAVE.get())) {
            return ModBlockEntities.MICROWAVE.get();
        }
        if (state.is(ModBlocks.CHEST.get())) {
            return ModBlockEntities.CHEST.get();
        }
        if (state.is(ModBlocks.CORPSE.get())) {
            return ModBlockEntities.CORPSE.get();
        }
        if (state.is(ModBlocks.AIRDROP.get())) {
            return ModBlockEntities.AIRDROP.get();
        }
        if (state.is(ModBlocks.CONVERTED_CONTAINER.get())) {
            return ModBlockEntities.CONVERTED_CONTAINER.get();
        }
        return ModBlockEntities.CAN_STASH.get();
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
            syncToClient();
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
        syncToClient();
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
        syncToClient();
    }

    public ItemStackHandler getContents() {
        return contents;
    }

    public ItemStackHandler getTools() {
        return tools;
    }

    public boolean requiresTool() {
        return requiresTool;
    }

    public void setRequiresTool(boolean requiresTool) {
        this.requiresTool = requiresTool;
    }

    public void placeTool(int slot, ItemStack stack) {
        if (slot < 0 || slot >= TOOL_SLOTS) {
            return;
        }
        if (stack.isEmpty()) {
            tools.setStackInSlot(slot, ItemStack.EMPTY);
        } else {
            ItemStack visual = stack.copy();
            visual.setCount(1);
            tools.setStackInSlot(slot, visual);
        }
        syncToClient();
    }

    public boolean matchesTool(ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < TOOL_SLOTS; slot++) {
            ItemStack tool = tools.getStackInSlot(slot);
            if (!tool.isEmpty() && matchesToolIgnoringDamage(tool, held)) {
                return true;
            }
        }
        return false;
    }

    /** Same item + full NBT match, except Damage/RepairCost so worn tools still open props. */
    private static boolean matchesToolIgnoringDamage(ItemStack required, ItemStack held) {
        if (!ItemStack.isSameItem(required, held)) {
            return false;
        }
        return java.util.Objects.equals(tagWithoutDurability(required.getTag()), tagWithoutDurability(held.getTag()));
    }

    private static @Nullable CompoundTag tagWithoutDurability(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        CompoundTag copy = tag.copy();
        copy.remove("Damage");
        copy.remove("RepairCost");
        return copy.isEmpty() ? null : copy;
    }

    public boolean allowsSearch(Player player) {
        return !requiresTool || matchesTool(player.getMainHandItem());
    }

    public Component toolRequirementName() {
        net.minecraft.network.chat.MutableComponent names = Component.empty();
        int count = 0;
        for (int slot = 0; slot < TOOL_SLOTS; slot++) {
            ItemStack tool = tools.getStackInSlot(slot);
            if (tool.isEmpty()) {
                continue;
            }
            if (count > 0) {
                names.append(", ");
            }
            names.append(tool.getHoverName());
            count++;
        }
        return count == 0 ? Component.translatable("gui.cts.need_tool") : names;
    }

    public boolean isContentsEmpty() {
        return isEmpty(contents);
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

    public int getSearchSeconds() {
        return searchSeconds;
    }

    /**
     * Effective search duration for survival hold-to-open.
     * <ul>
     *   <li>No loot ready → 1 second</li>
     *   <li>Already opened but still has items → half configured time</li>
     *   <li>Fresh after refill / first search → full configured time</li>
     * </ul>
     */
    public int getEffectiveSearchSeconds() {
        if (!hasLootAvailable()) {
            return 1;
        }
        BlockState state = getBlockState();
        boolean opened = state.hasProperty(CanStashBlock.LOOTED) && state.getValue(CanStashBlock.LOOTED);
        if (opened && !isContentsEmpty()) {
            return Math.max(1, (searchSeconds + 1) / 2);
        }
        return Math.max(1, searchSeconds);
    }

    public boolean hasLootAvailable() {
        if (!isContentsEmpty()) {
            return true;
        }
        return isConfigured() && cooldownTicks <= 0;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int ticks) {
        this.cooldownTicks = ticks;
    }

    public int getAirdropDespawnTicks() {
        return airdropDespawnTicks;
    }

    public void setAirdropDespawnTicks(int ticks) {
        this.airdropDespawnTicks = ticks;
    }

    /** Client-only mirror for menu sync. */
    public void setAirdropArmedClient(boolean armed) {
        this.airdropArmed = armed;
    }

    public boolean isAirdropArmed() {
        return airdropArmed;
    }

    public static boolean canConfigure(Player player) {
        return player.getAbilities().instabuild;
    }

    public boolean isConfigured() {
        return mode == Mode.LOOT_TABLE ? !lootTableId.isBlank() : hasTemplateItems();
    }

    public void applySettings(Mode newMode, String lootTable, int seconds, int openSeconds, boolean toolRequired) {
        this.mode = newMode;
        this.lootTableId = newMode == Mode.LOOT_TABLE ? sanitizeLootTable(lootTable) : this.lootTableId;
        this.respawnSeconds = Math.max(0, Math.min(MAX_RESPAWN_SECONDS, seconds));
        this.searchSeconds = Math.max(0, Math.min(MAX_SEARCH_SECONDS, openSeconds));
        this.requiresTool = toolRequired;
        if (isEmpty(contents) && cooldownTicks > 0) {
            cooldownTicks = respawnSeconds <= 0 ? 0 : respawnSeconds * 20;
        }
        setChanged();
        syncToClient();
    }

    /** One-shot airdrop crate: fixed loot table, no respawn, despawns when emptied or timed out. */
    public void setupAirdrop(String lootTable) {
        this.mode = Mode.LOOT_TABLE;
        this.lootTableId = sanitizeLootTable(lootTable);
        this.respawnSeconds = 0;
        this.cooldownTicks = 0;
        this.requiresTool = false;
        this.searchSeconds = AIRDROP_SEARCH_SECONDS;
        generateContents();
        this.airdropArmed = !isEmpty(contents);
        this.airdropPartiallyLooted = false;
        this.airdropInitialItemCount = countItems(contents);
        this.airdropDespawnTicks = airdropArmed ? AIRDROP_FULL_DESPAWN_TICKS : 0;
        setLooted(false);
        setChanged();
        syncToClient();
    }

    public void openChest(ServerPlayer player) {
        if (!player.getAbilities().instabuild && !consumeTool(player)) {
            return;
        }
        refillIfReady();
        setLooted(true);
        if (level != null) {
            level.playSound(null, worldPosition, openSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return CanStashBlockEntity.this.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player openingPlayer) {
                return new CanStashMenu(id, inventory, CanStashBlockEntity.this);
            }
        }, worldPosition);
    }

    private SoundEvent openSound() {
        BlockState state = getBlockState();
        if (state.is(ModBlocks.CRATE.get()) || state.is(ModBlocks.AMMO_BOX.get()) || state.is(ModBlocks.WEAPON_CASE.get())) {
            return ModSounds.WOOD_SOUND.get();
        }
        if (state.is(ModBlocks.MEDKIT.get())) {
            return ModSounds.ZIP.get();
        }
        if (state.is(ModBlocks.BOXES.get())) {
            return ModSounds.BOXES.get();
        }
        if (state.is(ModBlocks.CLOTHES.get()) || state.is(ModBlocks.DIRTY_BACKPACK.get()) || state.is(ModBlocks.LAUNDRY_BASKET.get())) {
            return ModSounds.CLOTH.get();
        }
        if (state.is(ModBlocks.TOOLBOX.get())) {
            return ModSounds.TOOLBOX.get();
        }
        if (state.is(ModBlocks.SAFE.get())) {
            return ModSounds.SAFE.get();
        }
        if (state.is(ModBlocks.METAL_SCRAP.get())) {
            return ModSounds.METAL_SCRAP.get();
        }
        if (state.is(ModBlocks.WASTE_DRUM.get())) {
            return ModSounds.WASTE_DRUM.get();
        }
        if (state.is(ModBlocks.TRASH_CAN.get())) {
            return ModSounds.PLASTIC.get();
        }
        if (state.is(ModBlocks.LITTER.get())) {
            return ModSounds.PAPER.get();
        }
        if (state.getBlock() instanceof DresserBlock || state.getBlock() instanceof MedicalCrateBlock) {
            return ModSounds.DRESSER.get();
        }
        if (state.getBlock() instanceof KitchenDrawerBlock) {
            return ModSounds.DRAWER.get();
        }
        if (state.getBlock() instanceof BrickCacheBlock) {
            return ModSounds.CACHE.get();
        }
        if (state.is(ModBlocks.NEST.get())) {
            return ModSounds.NEST.get();
        }
        if (state.getBlock() instanceof ShelvingBlock) {
            return ModSounds.SHELVING.get();
        }
        if (state.is(ModBlocks.CASH_REGISTER.get())) {
            return ModSounds.CASH_REGISTER.get();
        }
        if (state.is(ModBlocks.TOILET.get())) {
            return ModSounds.TOILET.get();
        }
        if (state.is(ModBlocks.FRIDGE.get()) || state.is(ModBlocks.WASHING_MACHINE.get())) {
            return ModSounds.PLASTIC_LID.get();
        }
        if (state.is(ModBlocks.MICROWAVE.get())) {
            return ModSounds.MICROWAVE.get();
        }
        if (state.is(ModBlocks.CHEST.get())) {
            return SoundEvents.CHEST_OPEN;
        }
        if (state.is(ModBlocks.CORPSE.get())) {
            return ModSounds.OPEN.get();
        }
        if (state.is(ModBlocks.AIRDROP.get())) {
            return SoundEvents.WOOD_PLACE;
        }
        if (state.is(ModBlocks.CONVERTED_CONTAINER.get()) && this instanceof ConvertedContainerBlockEntity converted) {
            return openSoundForMimic(converted.getMimic());
        }
        return ModSounds.CANSTASH.get();
    }

    private static SoundEvent openSoundForMimic(BlockState mimic) {
        if (mimic.getBlock() instanceof net.minecraft.world.level.block.BarrelBlock) {
            return SoundEvents.BARREL_OPEN;
        }
        if (mimic.getBlock() instanceof net.minecraft.world.level.block.ShulkerBoxBlock) {
            return SoundEvents.SHULKER_BOX_OPEN;
        }
        if (mimic.is(net.minecraft.world.level.block.Blocks.ENDER_CHEST)) {
            return SoundEvents.ENDER_CHEST_OPEN;
        }
        if (mimic.getBlock() instanceof net.minecraft.world.level.block.ChestBlock) {
            return SoundEvents.CHEST_OPEN;
        }
        return ModSounds.CANSTASH.get();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CanStashBlockEntity stash) {
        if (stash.cooldownTicks > 0) {
            stash.cooldownTicks--;
            if (stash.cooldownTicks == 0) {
                stash.generateContents();
                stash.syncToClient();
            } else if (stash.cooldownTicks % 20 == 0) {
                stash.setChanged();
            }
        }
        if (!level.isClientSide && state.is(ModBlocks.AIRDROP.get()) && stash.airdropArmed
                && level instanceof ServerLevel server) {
            if (isEmpty(stash.contents)) {
                cts.yblk.cts.block.AirdropBlock.destroyStructure(level, pos);
                return;
            }
            if (stash.airdropDespawnTicks > 0) {
                stash.airdropDespawnTicks--;
                if (stash.airdropDespawnTicks == 0) {
                    cts.yblk.cts.block.AirdropBlock.destroyStructure(level, pos);
                    return;
                }
                if (stash.airdropDespawnTicks % 20 == 0) {
                    stash.setChanged();
                }
            }
            cts.yblk.cts.util.AirdropSmoke.serverColumnAtCrate(server, pos, level.random);
        }
    }

    /** Client ticker for chest lid interpolation (~8 ticks open/close). */
    public static void clientTick(Level level, BlockPos pos, BlockState state, CanStashBlockEntity stash) {
        stash.oLidOpenness = stash.lidOpenness;
        float target = state.hasProperty(CanStashBlock.LOOTED) && state.getValue(CanStashBlock.LOOTED) ? 1.0F : 0.0F;
        float step = state.is(ModBlocks.WEAPON_CASE.get()) ? 0.1F : 0.125F;
        if (stash.lidOpenness < target) {
            stash.lidOpenness = Math.min(target, stash.lidOpenness + step);
        } else if (stash.lidOpenness > target) {
            stash.lidOpenness = Math.max(target, stash.lidOpenness - step);
        }
    }

    private void refillIfReady() {
        // One-shot airdrop: loot is rolled once on land, never again
        if (airdropArmed) {
            return;
        }
        if (!isConfigured() || cooldownTicks > 0) {
            return;
        }
        if (!isEmpty(contents)) {
            return;
        }
        if (respawnSeconds <= 0) {
            return;
        }
        generateContents();
    }

    private void generateContents() {
        if (!isConfigured()) {
            return;
        }
        filling = true;
        try {
            if (mode == Mode.LOOT_TABLE) {
                fillFromLootTable();
            } else {
                fillRandomTemplates();
            }
        } finally {
            filling = false;
        }
        cooldownTicks = 0;
        if (mode == Mode.ITEMS && isEmpty(contents) && hasTemplateItems() && respawnSeconds > 0) {
            cooldownTicks = respawnSeconds * 20;
        }
        refreshAppearance();
        setChanged();
    }

    private void fillRandomTemplates() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            contents.setStackInSlot(i, ItemStack.EMPTY);
        }
        if (level == null) {
            return;
        }
        List<Integer> chosen = SpawnChance.pickGroup(level.random, SLOT_COUNT, index -> !template.getStackInSlot(index).isEmpty(), index -> chances[index]);
        if (chosen.isEmpty()) {
            return;
        }
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots.add(i);
        }
        shuffle(slots);
        int cursor = 0;
        for (int templateSlot : chosen) {
            int room = slots.size() - cursor;
            if (room <= 0) {
                break;
            }
            ItemStack templateStack = template.getStackInSlot(templateSlot);
            if (TagSlipItem.isSlip(templateStack)) {
                int memberChance = chances[templateSlot];
                for (Item item : TagSlipItem.selected(templateStack)) {
                    if (cursor >= slots.size()) {
                        break;
                    }
                    if (memberChance <= 0 || level.random.nextInt(100) >= memberChance) {
                        continue;
                    }
                    ItemStack one = new ItemStack(item);
                    if (one.isDamageableItem()) {
                        contents.setStackInSlot(slots.get(cursor++), SpawnChance.spawnStack(one, level.random, 1, 1, true, randomEnchant[templateSlot]));
                        continue;
                    }
                    int total = SpawnChance.rollCount(level.random, countMin[templateSlot], countMax[templateSlot]);
                    one.setCount(total);
                    if (randomEnchant[templateSlot]) {
                        SpawnChance.applyRandomEnchantment(one, level.random);
                    }
                    contents.setStackInSlot(slots.get(cursor++), one);
                }
                continue;
            }
            ItemStack concrete = TagSlipItem.resolve(templateStack, level.random);
            if (concrete.isEmpty()) {
                continue;
            }
            if (concrete.isDamageableItem()) {
                contents.setStackInSlot(slots.get(cursor++), SpawnChance.spawnStack(concrete, level.random, 1, 1, randomDamage[templateSlot], randomEnchant[templateSlot]));
                continue;
            }
            int total = SpawnChance.rollCount(level.random, countMin[templateSlot], countMax[templateSlot]);
            for (int pile : SpawnChance.splitCount(level.random, total, room)) {
                if (cursor >= slots.size()) {
                    break;
                }
                ItemStack stack = concrete.copy();
                stack.setCount(pile);
                if (randomEnchant[templateSlot]) {
                    SpawnChance.applyRandomEnchantment(stack, level.random);
                }
                contents.setStackInSlot(slots.get(cursor++), stack);
            }
        }
    }

    private void shuffle(List<?> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int swap = level.random.nextInt(i + 1);
            Collections.swap(list, i, swap);
        }
    }

    private void fillFromLootTable() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            contents.setStackInSlot(i, ItemStack.EMPTY);
        }
        if (lootTableId.isBlank() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(lootTableId);
        if (id == null) {
            return;
        }
        LootTable table = serverLevel.getServer().getLootData().getLootTable(id);
        if (table == LootTable.EMPTY) {
            return;
        }
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
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots.add(i);
        }
        shuffle(slots);
        int cursor = 0;
        for (ItemStack stack : options) {
            int room = slots.size() - cursor;
            if (room <= 0) {
                break;
            }
            if (stack.getCount() <= 1 || stack.isDamageableItem()) {
                contents.setStackInSlot(slots.get(cursor++), stack.copy());
                continue;
            }
            int maxPile = Math.max(1, stack.getMaxStackSize());
            for (int pile : SpawnChance.splitCount(level.random, stack.getCount(), room)) {
                int left = pile;
                while (left > 0 && cursor < slots.size()) {
                    ItemStack part = stack.copy();
                    part.setCount(Math.min(left, maxPile));
                    contents.setStackInSlot(slots.get(cursor++), part);
                    left -= part.getCount();
                }
            }
        }
    }

    private boolean hasTemplateItems() {
        return !isEmpty(template);
    }

    private static boolean isEmpty(ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static int countItems(ItemStackHandler handler) {
        int total = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static String sanitizeLootTable(String raw) {
        return cts.yblk.cts.util.LootTableIds.normalize(raw);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CanStashConfigMenu(id, inventory, this);
    }

    @Override
    public void saveToItem(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.put("Template", template.serializeNBT());
        tag.putIntArray("Chances", chances);
        tag.putIntArray("CountMin", countMin);
        tag.putIntArray("CountMax", countMax);
        tag.putIntArray("RandomDamage", SpawnChance.flagArray(randomDamage));
        tag.putIntArray("RandomEnchant", SpawnChance.flagArray(randomEnchant));
        tag.putString("LootTable", lootTableId);
        tag.putString("Mode", mode.name());
        tag.putInt("RespawnSeconds", respawnSeconds);
        tag.putInt("SearchSeconds", searchSeconds);
        tag.putBoolean("RequiresTool", requiresTool);
        tag.put("Tools", tools.serializeNBT());
        BlockItem.setBlockEntityData(stack, getType(), tag);
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
        tag.put("Contents", contents.serializeNBT());
        tag.putString("LootTable", lootTableId);
        tag.putString("Mode", mode.name());
        tag.putInt("RespawnSeconds", respawnSeconds);
        tag.putInt("SearchSeconds", searchSeconds);
        tag.putBoolean("RequiresTool", requiresTool);
        tag.put("Tools", tools.serializeNBT());
        tag.putInt("Cooldown", cooldownTicks);
        tag.putBoolean("AirdropArmed", airdropArmed);
        tag.putInt("AirdropDespawnTicks", airdropDespawnTicks);
        tag.putBoolean("AirdropPartiallyLooted", airdropPartiallyLooted);
        tag.putInt("AirdropInitialItemCount", airdropInitialItemCount);
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
        if (tag.contains("Contents")) {
            filling = true;
            contents.deserializeNBT(tag.getCompound("Contents"));
            filling = false;
        }
        lootTableId = tag.getString("LootTable");
        try {
            mode = tag.contains("Mode") ? Mode.valueOf(tag.getString("Mode")) : Mode.ITEMS;
        } catch (IllegalArgumentException ignored) {
            mode = Mode.ITEMS;
        }
        respawnSeconds = tag.contains("RespawnSeconds")
                ? tag.getInt("RespawnSeconds")
                : CtsConfig.defaultLootRespawnSeconds();
        searchSeconds = tag.contains("SearchSeconds") ? tag.getInt("SearchSeconds") : defaultSearchSeconds(getBlockState());
        requiresTool = tag.getBoolean("RequiresTool");
        if (tag.contains("Tools")) {
            tools.deserializeNBT(tag.getCompound("Tools"));
        }
        cooldownTicks = tag.getInt("Cooldown");
        airdropArmed = tag.getBoolean("AirdropArmed");
        airdropPartiallyLooted = tag.getBoolean("AirdropPartiallyLooted");
        airdropInitialItemCount = tag.getInt("AirdropInitialItemCount");
        if (airdropArmed && !tag.contains("AirdropDespawnTicks")) {
            airdropDespawnTicks = AIRDROP_FULL_DESPAWN_TICKS;
            if (airdropInitialItemCount <= 0) {
                airdropInitialItemCount = countItems(contents);
            }
        } else {
            airdropDespawnTicks = tag.getInt("AirdropDespawnTicks");
        }
    }

    private boolean consumeTool(ServerPlayer player) {
        if (!requiresTool) {
            return true;
        }
        ItemStack held = player.getMainHandItem();
        if (!matchesTool(held)) {
            return false;
        }
        if (held.isDamageableItem()) {
            held.hurtAndBreak(1, player, opening -> opening.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        } else {
            held.shrink(1);
        }
        return true;
    }

    private void refreshAppearance() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(CanStashBlock.FACING) || !state.hasProperty(CanStashBlock.LOOTED)) {
            return;
        }
        Direction facing = state.getValue(CanStashBlock.FACING);
        if (!(state.getBlock() instanceof CanStashBlock block) || !block.usesPlayerFacing()) {
            facing = Direction.from2DDataValue((facing.get2DDataValue() + 1 + level.getRandom().nextInt(3)) & 3);
        }
        boolean empty = isEmpty(contents);
        BlockState next = state.setValue(CanStashBlock.FACING, facing).setValue(CanStashBlock.LOOTED, empty);
        if (!empty && state.hasProperty(ShelvingBlock.VARIANT)) {
            next = next.setValue(ShelvingBlock.VARIANT, ShelvingBlock.randomVariant(level.getRandom()));
        }
        if (!empty && state.hasProperty(CorpseBlock.VARIANT)) {
            next = next.setValue(CorpseBlock.VARIANT, CorpseBlock.randomVariant(level.getRandom()));
        }
        level.setBlock(worldPosition, next, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    private void setLooted(boolean looted) {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(CanStashBlock.LOOTED) || state.getValue(CanStashBlock.LOOTED) == looted) {
            return;
        }
        level.setBlock(worldPosition, state.setValue(CanStashBlock.LOOTED, looted), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
