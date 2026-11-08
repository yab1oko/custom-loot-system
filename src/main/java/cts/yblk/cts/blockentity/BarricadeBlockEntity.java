package cts.yblk.cts.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.block.BarricadeBlock;
import cts.yblk.cts.menu.BarricadeConfigMenu;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModTags;

public class BarricadeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DEFAULT_RESPAWN_SECONDS = 600;
    public static final int DEFAULT_BREAK_SECONDS = 5;
    public static final int MAX_RESPAWN_SECONDS = 36000;

    private int respawnSeconds = DEFAULT_RESPAWN_SECONDS;
    private int cooldownTicks;

    public BarricadeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARRICADE.get(), pos, state);
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

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void applySettings(int seconds) {
        this.respawnSeconds = Math.max(0, Math.min(MAX_RESPAWN_SECONDS, seconds));
        if (getBlockState().getValue(BarricadeBlock.BROKEN) && respawnSeconds > 0 && cooldownTicks <= 0) {
            cooldownTicks = respawnSeconds * 20;
        }
        setChanged();
        syncToClient();
    }

    public static boolean holdsBreakTool(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return false;
        }
        return held.is(ItemTags.AXES)
                || held.is(ItemTags.PICKAXES)
                || held.is(ModTags.AXES)
                || held.is(ModTags.PICKAXES);
    }

    public boolean tryBreak(ServerPlayer player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(BarricadeBlock.BROKEN) || state.getValue(BarricadeBlock.BROKEN)) {
            return false;
        }
        if (!holdsBreakTool(player)) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isDamageableItem()) {
            held.hurtAndBreak(3, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        level.setBlock(worldPosition, state.setValue(BarricadeBlock.BROKEN, true),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        cooldownTicks = respawnSeconds <= 0 ? 0 : respawnSeconds * 20;
        level.playSound(null, worldPosition, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 1.0F, 1.0F);
        setChanged();
        syncToClient();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BarricadeBlockEntity barricade) {
        if (!state.getValue(BarricadeBlock.BROKEN)) {
            if (barricade.cooldownTicks != 0) {
                barricade.cooldownTicks = 0;
                barricade.setChanged();
            }
            return;
        }
        if (barricade.respawnSeconds <= 0) {
            return;
        }
        if (barricade.cooldownTicks > 0) {
            barricade.cooldownTicks--;
            if (barricade.cooldownTicks % 20 == 0) {
                barricade.setChanged();
            }
        }
        if (barricade.cooldownTicks <= 0) {
            // Keep facing / variant / spin — only clear broken.
            level.setBlock(pos, state.setValue(BarricadeBlock.BROKEN, false),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            barricade.cooldownTicks = 0;
            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            barricade.setChanged();
            barricade.syncToClient();
        }
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cts.barricade");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BarricadeConfigMenu(id, inventory, this);
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
