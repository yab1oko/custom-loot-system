package cts.yblk.cts.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import cts.yblk.cts.blockentity.HarvestTreeBlockEntity;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModMenus;

public class HarvestTreeConfigMenu extends AbstractContainerMenu {
    private final HarvestTreeBlockEntity tree;

    public HarvestTreeConfigMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, menuFromPos(inventory, data.readBlockPos()));
    }

    public HarvestTreeConfigMenu(int id, Inventory inventory, HarvestTreeBlockEntity tree) {
        super(ModMenus.HARVEST_TREE_CONFIG.get(), id);
        this.tree = tree;
    }

    public HarvestTreeBlockEntity getTree() {
        return tree;
    }

    private static HarvestTreeBlockEntity menuFromPos(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof HarvestTreeBlockEntity found) {
            return found;
        }
        throw new IllegalStateException("Harvest tree missing at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return tree.getLevel() != null
                && tree.getLevel().getBlockState(tree.getBlockPos()).is(ModBlocks.HARVEST_TREE.get())
                && player.distanceToSqr(Vec3.atCenterOf(tree.getBlockPos())) <= 64.0D
                && HarvestTreeBlockEntity.canConfigure(player);
    }
}
