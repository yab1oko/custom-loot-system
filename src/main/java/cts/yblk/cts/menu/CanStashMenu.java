package cts.yblk.cts.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.SlotItemHandler;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.registry.ModMenus;

public class CanStashMenu extends AbstractContainerMenu {
    private final CanStashBlockEntity stash;

    public CanStashMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, menuFromPos(inventory, data.readBlockPos()));
    }

    public CanStashMenu(int id, Inventory inventory, CanStashBlockEntity stash) {
        super(ModMenus.CAN_STASH.get(), id);
        this.stash = stash;
        this.addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> stash.getCooldownTicks();
                    case 1 -> stash.isAirdropArmed() ? stash.getAirdropDespawnTicks() : 0;
                    case 2 -> stash.isAirdropArmed() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (stash.getLevel() == null || !stash.getLevel().isClientSide) {
                    return;
                }
                switch (index) {
                    case 0 -> stash.setCooldownTicks(value);
                    case 1 -> stash.setAirdropDespawnTicks(value);
                    case 2 -> stash.setAirdropArmedClient(value != 0);
                    default -> {
                    }
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new SlotItemHandler(stash.getContents(), col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 85 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 143));
        }
    }

    private static CanStashBlockEntity menuFromPos(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof CanStashBlockEntity found) {
            return found;
        }
        throw new IllegalStateException("Can stash missing at " + pos);
    }

    public int getCooldownTicks() {
        return stash.getCooldownTicks();
    }

    public int getAirdropDespawnTicks() {
        return stash.getAirdropDespawnTicks();
    }

    public boolean isAirdrop() {
        return stash.isAirdropArmed();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < CanStashBlockEntity.SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, CanStashBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, CanStashBlockEntity.SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stash.getLevel() != null
                && CanStashBlock.isSearchable(stash.getLevel().getBlockState(stash.getBlockPos()))
                && player.distanceToSqr(Vec3.atCenterOf(stash.getBlockPos())) <= 64.0D;
    }
}
