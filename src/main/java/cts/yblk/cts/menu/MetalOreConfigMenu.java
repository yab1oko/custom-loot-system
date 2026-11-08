package cts.yblk.cts.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import cts.yblk.cts.blockentity.MetalOreBlockEntity;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModMenus;

public class MetalOreConfigMenu extends AbstractContainerMenu {
    private final MetalOreBlockEntity ore;

    public MetalOreConfigMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, menuFromPos(inventory, data.readBlockPos()));
    }

    public MetalOreConfigMenu(int id, Inventory inventory, MetalOreBlockEntity ore) {
        super(ModMenus.METAL_ORE_CONFIG.get(), id);
        this.ore = ore;
    }

    public MetalOreBlockEntity getOre() {
        return ore;
    }

    private static MetalOreBlockEntity menuFromPos(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof MetalOreBlockEntity found) {
            return found;
        }
        throw new IllegalStateException("Metal ore missing at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return ore.getLevel() != null
                && ore.getLevel().getBlockState(ore.getBlockPos()).is(ModBlocks.METAL_ORE.get())
                && player.distanceToSqr(Vec3.atCenterOf(ore.getBlockPos())) <= 64.0D
                && MetalOreBlockEntity.canConfigure(player);
    }
}
