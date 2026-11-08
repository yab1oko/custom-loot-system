package cts.yblk.cts.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import cts.yblk.cts.blockentity.BarricadeBlockEntity;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModMenus;

public class BarricadeConfigMenu extends AbstractContainerMenu {
    private final BarricadeBlockEntity barricade;

    public BarricadeConfigMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, menuFromPos(inventory, data.readBlockPos()));
    }

    public BarricadeConfigMenu(int id, Inventory inventory, BarricadeBlockEntity barricade) {
        super(ModMenus.BARRICADE_CONFIG.get(), id);
        this.barricade = barricade;
    }

    public BarricadeBlockEntity getBarricade() {
        return barricade;
    }

    private static BarricadeBlockEntity menuFromPos(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof BarricadeBlockEntity found) {
            return found;
        }
        throw new IllegalStateException("Barricade missing at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return barricade.getLevel() != null
                && barricade.getLevel().getBlockState(barricade.getBlockPos()).is(ModBlocks.BARRICADE.get())
                && player.distanceToSqr(Vec3.atCenterOf(barricade.getBlockPos())) <= 64.0D
                && BarricadeBlockEntity.canConfigure(player);
    }
}
