package cts.yblk.cts.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.registries.ForgeRegistries;
import cts.yblk.cts.block.VentilationGrilleBlock;
import cts.yblk.cts.menu.VentilationGrilleConfigMenu;
import cts.yblk.cts.registry.ModBlockEntities;
import cts.yblk.cts.registry.ModSounds;
import cts.yblk.cts.registry.ModTags;

public class VentilationGrilleBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DEFAULT_RESPAWN_SECONDS = 600;
    public static final int DEFAULT_OPEN_SECONDS = 5;
    public static final int MAX_RESPAWN_SECONDS = 36000;

    private int respawnSeconds = DEFAULT_RESPAWN_SECONDS;
    private int cooldownTicks;

    public VentilationGrilleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VENTILATION_GRILLE.get(), pos, state);
    }

    public static boolean canConfigure(Player player) {
        return player != null && player.getAbilities().instabuild;
    }

    public int getRespawnSeconds() {
        return respawnSeconds;
    }

    public int getOpenSeconds() {
        return DEFAULT_OPEN_SECONDS;
    }

    public void applySettings(int seconds) {
        this.respawnSeconds = Math.max(0, Math.min(MAX_RESPAWN_SECONDS, seconds));
        if (getBlockState().getValue(VentilationGrilleBlock.BROKEN) && respawnSeconds > 0 && cooldownTicks <= 0) {
            cooldownTicks = respawnSeconds * 20;
        }
        setChanged();
        syncToClient();
    }

    public static boolean holdsScrewdriver(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return false;
        }
        if (held.is(ModTags.SCREWDRIVERS)) {
            return true;
        }
        // Fallback: empty/incomplete datapack tags still match any *screwdriver* item id.
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(held.getItem());
        return id != null && id.getPath().contains("screwdriver");
    }

    public boolean tryOpen(ServerPlayer player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(VentilationGrilleBlock.BROKEN) || state.getValue(VentilationGrilleBlock.BROKEN)) {
            return false;
        }
        if (!holdsScrewdriver(player)) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (!player.getAbilities().instabuild && held.isDamageableItem()) {
            held.hurtAndBreak(3, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        level.setBlock(worldPosition, state.setValue(VentilationGrilleBlock.BROKEN, true),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        cooldownTicks = respawnSeconds <= 0 ? 0 : respawnSeconds * 20;
        level.playSound(null, worldPosition, ModSounds.VENT_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        setChanged();
        syncToClient();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VentilationGrilleBlockEntity grille) {
        if (!state.getValue(VentilationGrilleBlock.BROKEN)) {
            if (grille.cooldownTicks != 0) {
                grille.cooldownTicks = 0;
                grille.setChanged();
            }
            return;
        }
        if (grille.respawnSeconds <= 0) {
            return;
        }
        if (grille.cooldownTicks > 0) {
            grille.cooldownTicks--;
            if (grille.cooldownTicks % 20 == 0) {
                grille.setChanged();
            }
        }
        if (grille.cooldownTicks <= 0) {
            level.setBlock(pos, state.setValue(VentilationGrilleBlock.BROKEN, false),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            grille.cooldownTicks = 0;
            level.playSound(null, pos, ModSounds.VENT_OPEN.get(), SoundSource.BLOCKS, 0.6F, 0.8F);
            grille.setChanged();
            grille.syncToClient();
        }
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cts.ventilation_grille");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new VentilationGrilleConfigMenu(id, inventory, this);
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
