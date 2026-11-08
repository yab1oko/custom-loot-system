package cts.yblk.cts.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import cts.yblk.cts.blockentity.EntityPlateBlockEntity;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModMenus;

public class EntityPlateMenu extends AbstractContainerMenu {
    private final EntityPlateBlockEntity plate;

    public EntityPlateMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, menuFromPos(inventory, data.readBlockPos()));
    }

    public EntityPlateMenu(int id, Inventory inventory, EntityPlateBlockEntity plate) {
        super(ModMenus.ENTITY_PLATE.get(), id);
        this.plate = plate;
    }

    public EntityPlateBlockEntity getPlate() {
        return plate;
    }

    private static EntityPlateBlockEntity menuFromPos(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof EntityPlateBlockEntity found) {
            return found;
        }
        throw new IllegalStateException("Entity plate missing at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return plate.getLevel() != null
                && plate.getLevel().getBlockState(plate.getBlockPos()).is(ModBlocks.ENTITY_PLATE.get())
                && player.distanceToSqr(Vec3.atCenterOf(plate.getBlockPos())) <= 64.0D
                && EntityPlateBlockEntity.canConfigure(player);
    }
}
