package cts.yblk.cts.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.SlotItemHandler;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModMenus;

public class LootPlateMenu extends AbstractContainerMenu {
    public static final int SETTINGS_PIXELS = 24;
    public static final int TOP_EXTRA = 10;

    private final LootPlateBlockEntity plate;

    public LootPlateMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, menuFromPos(inventory, data.readBlockPos()));
    }

    public LootPlateMenu(int id, Inventory inventory, LootPlateBlockEntity plate) {
        super(ModMenus.LOOT_PLATE.get(), id);
        this.plate = plate;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new SlotItemHandler(plate.getTemplate(), col + row * 9, 8 + col * 18, 18 + TOP_EXTRA + row * 18) {
                    @Override
                    public boolean isActive() {
                        return plate.getMode() == LootPlateBlockEntity.Mode.ITEMS;
                    }
                });
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 85 + SETTINGS_PIXELS + TOP_EXTRA + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 143 + SETTINGS_PIXELS + TOP_EXTRA));
        }
    }

    private static LootPlateBlockEntity menuFromPos(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof LootPlateBlockEntity found) {
            return found;
        }
        throw new IllegalStateException("Loot plate missing at " + pos);
    }

    public LootPlateBlockEntity getPlate() {
        return plate;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < LootPlateBlockEntity.SLOT_COUNT) {
            if (plate.getMode() != LootPlateBlockEntity.Mode.ITEMS) {
                return;
            }
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                if (button == 0) {
                    plate.placeTemplate(slotId, ItemStack.EMPTY);
                    broadcastChanges();
                }
                return;
            }
            plate.placeTemplate(slotId, carried.copy());
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (plate.getMode() != LootPlateBlockEntity.Mode.ITEMS) {
            return ItemStack.EMPTY;
        }
        if (index >= LootPlateBlockEntity.SLOT_COUNT) {
            ItemStack stack = this.slots.get(index).getItem();
            if (!stack.isEmpty()) {
                for (int i = 0; i < LootPlateBlockEntity.SLOT_COUNT; i++) {
                    if (plate.getTemplate().getStackInSlot(i).isEmpty()) {
                        plate.placeTemplate(i, stack.copy());
                        break;
                    }
                }
            }
        } else if (index >= 0) {
            plate.placeTemplate(index, ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return plate.getLevel() != null
                && plate.getLevel().getBlockState(plate.getBlockPos()).is(ModBlocks.LOOT_PLATE.get())
                && player.distanceToSqr(Vec3.atCenterOf(plate.getBlockPos())) <= 64.0D
                && LootPlateBlockEntity.canConfigure(player);
    }
}
