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
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.registry.ModMenus;

public class CanStashConfigMenu extends AbstractContainerMenu {
    public static final int SETTINGS_PIXELS = 82;
    public static final int TOP_EXTRA = 10;
    public static final int TOOL_SLOT_Y = 132 + TOP_EXTRA;

    private final CanStashBlockEntity stash;

    public CanStashConfigMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, menuFromPos(inventory, data.readBlockPos()));
    }

    public CanStashConfigMenu(int id, Inventory inventory, CanStashBlockEntity stash) {
        super(ModMenus.CAN_STASH_CONFIG.get(), id);
        this.stash = stash;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new SlotItemHandler(stash.getTemplate(), col + row * 9, 8 + col * 18, 18 + TOP_EXTRA + row * 18) {
                    @Override
                    public boolean isActive() {
                        return stash.getMode() == CanStashBlockEntity.Mode.ITEMS;
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
        for (int col = 0; col < CanStashBlockEntity.TOOL_SLOTS; ++col) {
            this.addSlot(new SlotItemHandler(stash.getTools(), col, 8 + col * 18, TOOL_SLOT_Y) {
                @Override
                public boolean isActive() {
                    return stash.requiresTool();
                }
            });
        }
    }

    private int toolIndex(int slotId) {
        int start = CanStashBlockEntity.SLOT_COUNT + 36;
        if (slotId >= start && slotId < start + CanStashBlockEntity.TOOL_SLOTS) {
            return slotId - start;
        }
        return -1;
    }

    private static CanStashBlockEntity menuFromPos(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof CanStashBlockEntity found) {
            return found;
        }
        throw new IllegalStateException("Can stash missing at " + pos);
    }

    public CanStashBlockEntity getStash() {
        return stash;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < CanStashBlockEntity.SLOT_COUNT) {
            if (stash.getMode() != CanStashBlockEntity.Mode.ITEMS) {
                return;
            }
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                if (button == 0) {
                    stash.placeTemplate(slotId, ItemStack.EMPTY);
                    broadcastChanges();
                }
                return;
            }
            stash.placeTemplate(slotId, carried.copy());
            broadcastChanges();
            return;
        }
        int toolSlot = toolIndex(slotId);
        if (toolSlot >= 0) {
            if (!stash.requiresTool()) {
                return;
            }
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                if (button == 0) {
                    stash.placeTool(toolSlot, ItemStack.EMPTY);
                    broadcastChanges();
                }
                return;
            }
            stash.placeTool(toolSlot, carried.copy());
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (stash.getMode() != CanStashBlockEntity.Mode.ITEMS) {
            return ItemStack.EMPTY;
        }
        if (index >= CanStashBlockEntity.SLOT_COUNT) {
            ItemStack stack = this.slots.get(index).getItem();
            if (!stack.isEmpty()) {
                for (int i = 0; i < CanStashBlockEntity.SLOT_COUNT; i++) {
                    if (stash.getTemplate().getStackInSlot(i).isEmpty()) {
                        stash.placeTemplate(i, stack.copy());
                        break;
                    }
                }
            }
        } else if (index >= 0) {
            stash.placeTemplate(index, ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stash.getLevel() != null
                && CanStashBlock.isSearchable(stash.getLevel().getBlockState(stash.getBlockPos()))
                && player.distanceToSqr(Vec3.atCenterOf(stash.getBlockPos())) <= 64.0D
                && CanStashBlockEntity.canConfigure(player);
    }
}
