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
import cts.yblk.cts.block.MetalOreBlock;
import cts.yblk.cts.menu.MetalOreConfigMenu;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModTags;

public class MetalOreBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DEFAULT_RESPAWN_SECONDS = 1800;
    public static final int DEFAULT_BREAK_SECONDS = 5;
    public static final int MAX_RESPAWN_SECONDS = 36000;
    /** Soft-dep: when RPM is present, ore drops forged iron nuggets instead of vanilla iron nuggets. */
    private static final ResourceLocation MATERIALS_FORGED_IRON_NUGGET =
            new ResourceLocation("roleplay_materials", "forged_iron_nugget");

    private int respawnSeconds = DEFAULT_RESPAWN_SECONDS;
    private int cooldownTicks;

    public MetalOreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.METAL_ORE.get(), pos, state);
    }

    public static boolean canConfigure(Player player) {
        return player != null && player.getAbilities().instabuild;
    }

    public int getRespawnSeconds() {
        return respawnSeconds;
    }

    public int getBreakSeconds() {
        return DEFAULT_BREAK_SECONDS;
    }

    public void applySettings(int seconds) {
        this.respawnSeconds = Math.max(0, Math.min(MAX_RESPAWN_SECONDS, seconds));
        if (getBlockState().getValue(MetalOreBlock.BROKEN) && respawnSeconds > 0 && cooldownTicks <= 0) {
            cooldownTicks = respawnSeconds * 20;
        }
        setChanged();
        syncToClient();
    }

    public static boolean holdsPickaxe(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return false;
        }
        return held.is(ItemTags.PICKAXES) || held.is(ModTags.PICKAXES);
    }

    public boolean tryMine(ServerPlayer player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(MetalOreBlock.BROKEN) || state.getValue(MetalOreBlock.BROKEN)) {
            return false;
        }
        if (!holdsPickaxe(player)) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isDamageableItem()) {
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        level.setBlock(worldPosition, state.setValue(MetalOreBlock.BROKEN, true),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        cooldownTicks = respawnSeconds <= 0 ? 0 : respawnSeconds * 20;
        dropLoot(level, worldPosition, player);
        level.playSound(null, worldPosition, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        setChanged();
        syncToClient();
        return true;
    }

    private static void dropLoot(Level level, BlockPos pos, ServerPlayer player) {
        RandomSource random = level.getRandom();
        Item drop = resolveDropItem();
        int count = 1 + random.nextInt(3);
        double towardX = player.getX() - (pos.getX() + 0.5D);
        double towardZ = player.getZ() - (pos.getZ() + 0.5D);
        double horizontal = Math.sqrt(towardX * towardX + towardZ * towardZ);
        if (horizontal < 1.0E-4D) {
            towardX = player.getLookAngle().x;
            towardZ = player.getLookAngle().z;
            horizontal = Math.sqrt(towardX * towardX + towardZ * towardZ);
        }
        if (horizontal < 1.0E-4D) {
            towardX = 1.0D;
            towardZ = 0.0D;
            horizontal = 1.0D;
        }
        towardX /= horizontal;
        towardZ /= horizontal;

        for (int i = 0; i < count; i++) {
            // Spawn just outside the stone face toward the player, not inside the block.
            double x = pos.getX() + 0.5D + towardX * 0.65D + (random.nextDouble() - 0.5D) * 0.15D;
            double y = pos.getY() + 0.55D + random.nextDouble() * 0.25D;
            double z = pos.getZ() + 0.5D + towardZ * 0.65D + (random.nextDouble() - 0.5D) * 0.15D;
            ItemEntity entity = new ItemEntity(level, x, y, z, new ItemStack(drop));
            entity.setDefaultPickUpDelay();
            double speed = 0.22D + random.nextDouble() * 0.1D;
            entity.setDeltaMovement(
                    towardX * speed + (random.nextDouble() - 0.5D) * 0.06D,
                    0.18D + random.nextDouble() * 0.1D,
                    towardZ * speed + (random.nextDouble() - 0.5D) * 0.06D);
            level.addFreshEntity(entity);
        }
    }

    private static Item resolveDropItem() {
        if (ModList.get().isLoaded(MATERIALS_FORGED_IRON_NUGGET.getNamespace())) {
            Item nugget = ForgeRegistries.ITEMS.getValue(MATERIALS_FORGED_IRON_NUGGET);
            if (nugget != null && nugget != Items.AIR) {
                return nugget;
            }
        }
        return Items.IRON_NUGGET;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MetalOreBlockEntity ore) {
        if (!state.getValue(MetalOreBlock.BROKEN)) {
            if (ore.cooldownTicks != 0) {
                ore.cooldownTicks = 0;
                ore.setChanged();
            }
            return;
        }
        if (ore.respawnSeconds <= 0) {
            return;
        }
        if (ore.cooldownTicks > 0) {
            ore.cooldownTicks--;
            if (ore.cooldownTicks % 20 == 0) {
                ore.setChanged();
            }
        }
        if (ore.cooldownTicks <= 0) {
            level.setBlock(pos, state.setValue(MetalOreBlock.BROKEN, false),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            ore.cooldownTicks = 0;
            level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            ore.setChanged();
            ore.syncToClient();
        }
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cts.metal_ore");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MetalOreConfigMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RespawnSeconds", respawnSeconds);
        tag.putInt("Cooldown", cooldownTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        respawnSeconds = tag.contains("RespawnSeconds") ? tag.getInt("RespawnSeconds") : DEFAULT_RESPAWN_SECONDS;
        cooldownTicks = tag.getInt("Cooldown");
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
