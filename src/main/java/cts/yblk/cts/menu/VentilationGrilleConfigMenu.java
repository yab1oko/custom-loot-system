package cts.yblk.cts.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import cts.yblk.cts.blockentity.VentilationGrilleBlockEntity;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModMenus;

public class VentilationGrilleConfigMenu extends AbstractContainerMenu {
    private final VentilationGrilleBlockEntity grille;

    public VentilationGrilleConfigMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, menuFromPos(inventory, data.readBlockPos()));
    }

    public VentilationGrilleConfigMenu(int id, Inventory inventory, VentilationGrilleBlockEntity grille) {
        super(ModMenus.VENTILATION_GRILLE_CONFIG.get(), id);
        this.grille = grille;
    }

    public VentilationGrilleBlockEntity getGrille() {
        return grille;
    }

    private static VentilationGrilleBlockEntity menuFromPos(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof VentilationGrilleBlockEntity found) {
            return found;
        }
        throw new IllegalStateException("Ventilation grille missing at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return grille.getLevel() != null
                && grille.getLevel().getBlockState(grille.getBlockPos()).is(ModBlocks.VENTILATION_GRILLE.get())
                && player.distanceToSqr(Vec3.atCenterOf(grille.getBlockPos())) <= 64.0D
                && VentilationGrilleBlockEntity.canConfigure(player);
    }
}
