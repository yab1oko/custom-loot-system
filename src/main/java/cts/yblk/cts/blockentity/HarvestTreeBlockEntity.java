package cts.yblk.cts.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import cts.yblk.cts.block.HarvestTreeBlock;
import cts.yblk.cts.block.HarvestTreeStructure;
import cts.yblk.cts.block.HarvestTreeTrunkBlock;
import cts.yblk.cts.menu.HarvestTreeConfigMenu;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModTags;

public class HarvestTreeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DEFAULT_RESPAWN_SECONDS = 1800;
    public static final int DEFAULT_BREAK_SECONDS = 8;
    public static final int BREAK_SECONDS_MEDIUM = 8;
    public static final int BREAK_SECONDS_LARGE = 12;
    public static final int MAX_RESPAWN_SECONDS = 36000;
    private static final ResourceLocation MATERIALS_WOOD = new ResourceLocation("roleplay_materials", "wood");

    private int respawnSeconds = DEFAULT_RESPAWN_SECONDS;
    private int cooldownTicks;
    private HarvestTreeStructure.Size treeSize = HarvestTreeStructure.Size.MEDIUM;

    public HarvestTreeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HARVEST_TREE.get(), pos, state);
    }

    public static boolean canConfigure(Player player) {
        return player != null && player.getAbilities().instabuild;
    }

    public HarvestTreeStructure.Size getTreeSize() {
        return treeSize;
    }

    public void setTreeSize(HarvestTreeStructure.Size treeSize) {
        this.treeSize = treeSize == null ? HarvestTreeStructure.Size.MEDIUM : treeSize;
        setChanged();
    }

    public int getRespawnSeconds() {
        return respawnSeconds;
    }

    public int getBreakSeconds() {
        return treeSize == HarvestTreeStructure.Size.LARGE ? BREAK_SECONDS_LARGE : BREAK_SECONDS_MEDIUM;
    }

    public void applySettings(int seconds) {
        this.respawnSeconds = Math.max(0, Math.min(MAX_RESPAWN_SECONDS, seconds));
        if (getBlockState().getValue(HarvestTreeBlock.BROKEN) && respawnSeconds > 0 && cooldownTicks <= 0) {
            cooldownTicks = respawnSeconds * 20;
        }
        setChanged();
        syncToClient();
    }

    public static boolean holdsAxe(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return false;
        }
        return held.is(ItemTags.AXES) || held.is(ModTags.AXES);
    }

    public boolean tryChop(ServerPlayer player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(HarvestTreeBlock.BROKEN) || state.getValue(HarvestTreeBlock.BROKEN)) {
            return false;
        }
        if (!holdsAxe(player)) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isDamageableItem()) {
            int durabilityCost = treeSize == HarvestTreeStructure.Size.LARGE ? 5 : 3;
            held.hurtAndBreak(durabilityCost, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }

        HarvestTreeTrunkBlock.setClearing(true);
        try {
            HarvestTreeStructure.clearParts(level, worldPosition);
        } finally {
            HarvestTreeTrunkBlock.setClearing(false);
        }

        level.setBlock(worldPosition, state.setValue(HarvestTreeBlock.BROKEN, true),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        cooldownTicks = respawnSeconds <= 0 ? 0 : respawnSeconds * 20;
        dropLoot(level, worldPosition, treeSize);
        level.playSound(null, worldPosition, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        setChanged();
        syncToClient();
        return true;
    }

    private static void dropLoot(Level level, BlockPos pos, HarvestTreeStructure.Size size) {
        RandomSource random = level.getRandom();
        Item drop = resolveDropItem();
        int count = size == HarvestTreeStructure.Size.LARGE
                ? 6 + random.nextInt(5)  // 6-10
                : 3 + random.nextInt(5); // 3-7
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.7D;
            double y = pos.getY() + 0.4D + random.nextDouble() * 0.4D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.7D;
            ItemEntity entity = new ItemEntity(level, x, y, z, new ItemStack(drop));
            entity.setDefaultPickUpDelay();
            entity.setDeltaMovement(
                    (random.nextDouble() - 0.5D) * 0.12D,
                    0.12D + random.nextDouble() * 0.08D,
                    (random.nextDouble() - 0.5D) * 0.12D);
            level.addFreshEntity(entity);
        }
    }

    private static Item resolveDropItem() {
        if (ModList.get().isLoaded(MATERIALS_WOOD.getNamespace())) {
            Item scrap = ForgeRegistries.ITEMS.getValue(MATERIALS_WOOD);
            if (scrap != null && scrap != Items.AIR) {
                return scrap;
            }
        }
        return Items.OAK_LOG;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HarvestTreeBlockEntity tree) {
        if (!state.getValue(HarvestTreeBlock.BROKEN)) {
            if (tree.cooldownTicks != 0) {
                tree.cooldownTicks = 0;
                tree.setChanged();
            }
            return;
        }
        if (tree.respawnSeconds <= 0) {
            return;
        }
        if (tree.cooldownTicks > 0) {
            tree.cooldownTicks--;
            if (tree.cooldownTicks % 20 == 0) {
                tree.setChanged();
            }
        }
        if (tree.cooldownTicks <= 0) {
            level.setBlock(pos, state.setValue(HarvestTreeBlock.BROKEN, false),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            HarvestTreeTrunkBlock.setClearing(true);
            try {
                HarvestTreeStructure.grow(level, pos, level.getRandom(), tree.treeSize);
            } finally {
                HarvestTreeTrunkBlock.setClearing(false);
            }
            tree.cooldownTicks = 0;
            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            tree.setChanged();
            tree.syncToClient();
        }
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(treeSize == HarvestTreeStructure.Size.LARGE
                ? "block.cts.harvest_tree_large"
                : "block.cts.harvest_tree");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new HarvestTreeConfigMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RespawnSeconds", respawnSeconds);
        tag.putInt("Cooldown", cooldownTicks);
        tag.putString("TreeSize", treeSize.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        respawnSeconds = tag.contains("RespawnSeconds") ? tag.getInt("RespawnSeconds") : DEFAULT_RESPAWN_SECONDS;
        cooldownTicks = tag.getInt("Cooldown");
        if (tag.contains("TreeSize")) {
            try {
                treeSize = HarvestTreeStructure.Size.valueOf(tag.getString("TreeSize"));
            } catch (IllegalArgumentException ignored) {
                treeSize = HarvestTreeStructure.Size.MEDIUM;
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
