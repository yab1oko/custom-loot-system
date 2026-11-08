package cts.yblk.cts.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import cts.yblk.cts.item.LootBookData;
import cts.yblk.cts.registry.ModItems;
import cts.yblk.cts.registry.ModMenus;

public class LootBookMenu extends AbstractContainerMenu {
    public static final int HEADER = 48;
    public static final int PAGE_BAR = 18;
    public static final int SLOT_X = 8;
    public static final int SLOT_Y = 18 + HEADER;
    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_Y = 103 + (LootBookData.ROWS - 4) * 18 + HEADER + PAGE_BAR;
    private static final int SLOT_DATA = 5;

    private final Player player;
    private final InteractionHand hand;
    private final LootBookData book = new LootBookData();
    private final int[] values = new int[2 + LootBookData.PAGE_SIZE * SLOT_DATA];
    private boolean suppress;

    private final ItemStackHandler templates = new ItemStackHandler(LootBookData.PAGE_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!suppress) {
                book.place(slot, getStackInSlot(slot));
                writeSlot(slot);
                save();
            }
        }
    };

    public LootBookMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readEnum(InteractionHand.class));
    }

    public LootBookMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ModMenus.LOOT_BOOK.get(), id);
        this.player = inventory.player;
        this.hand = hand;
        ItemStack stack = inventory.player.getItemInHand(hand);
        if (stack.is(ModItems.LOOT_BOOK.get()) && stack.hasTag()) {
            book.read(stack.getTag());
        }
        for (int row = 0; row < LootBookData.ROWS; row++) {
            for (int column = 0; column < LootBookData.COLUMNS; column++) {
                int slot = column + row * LootBookData.COLUMNS;
                addSlot(new SlotItemHandler(templates, slot, SLOT_X + column * 18, SLOT_Y + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, INVENTORY_X + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, INVENTORY_X + column * 18, INVENTORY_Y + 58));
        }
        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return values[index];
            }

            @Override
            public void set(int index, int value) {
                values[index] = value;
            }

            @Override
            public int getCount() {
                return values.length;
            }
        });
        showPage(book.page());
    }

    public LootBookData book() {
        return book;
    }

    public int page() {
        return values[0];
    }

    public int pageCount() {
        return values[1];
    }

    public int countMin(int slot) {
        return values[2 + slot * SLOT_DATA];
    }

    public int countMax(int slot) {
        return values[3 + slot * SLOT_DATA];
    }

    public int chance(int slot) {
        return values[4 + slot * SLOT_DATA];
    }

    public boolean randomDamage(int slot) {
        return values[5 + slot * SLOT_DATA] != 0;
    }

    public boolean randomEnchant(int slot) {
        return values[6 + slot * SLOT_DATA] != 0;
    }

    public ItemStack template(int slot) {
        return templates.getStackInSlot(slot);
    }

    public void setEntry(int slot, int min, int max, int chance, boolean randomDamage, boolean randomEnchant, String tag, String pick) {
        if (slot < 0 || slot >= LootBookData.PAGE_SIZE || templates.getStackInSlot(slot).isEmpty()) {
            return;
        }
        book.setEntry(slot, min, max, chance, randomDamage, randomEnchant, tag, pick);
        writeSlot(slot);
        save();
    }

    public void setName(String name) {
        book.setName(name);
        save();
    }

    public void replaceBook(LootBookData data) {
        book.replace(data);
        showPage(book.page());
        save();
    }

    public void applySync(CompoundTag tag) {
        book.read(tag);
        showPage(book.page());
    }

    public void movePage(int direction) {
        boolean moved = direction < 0 ? book.openPrevious() : book.openNext();
        if (moved) {
            showPage(book.page());
            save();
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < LootBookData.PAGE_SIZE) {
            if (clickType != ClickType.PICKUP) {
                return;
            }
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                if (button == 0) {
                    book.place(slotId, ItemStack.EMPTY);
                    showSlot(slotId);
                    save();
                }
                return;
            }
            book.place(slotId, carried.copy());
            showSlot(slotId);
            save();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= LootBookData.PAGE_SIZE) {
            ItemStack stack = slots.get(index).getItem();
            if (!stack.isEmpty()) {
                for (int slot = 0; slot < LootBookData.PAGE_SIZE; slot++) {
                    if (templates.getStackInSlot(slot).isEmpty()) {
                        book.place(slot, stack.copy());
                        showSlot(slot);
                        save();
                        break;
                    }
                }
            }
        } else if (index >= 0) {
            book.place(index, ItemStack.EMPTY);
            showSlot(index);
            save();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).is(ModItems.LOOT_BOOK.get());
    }

    private void showPage(int index) {
        suppress = true;
        for (int slot = 0; slot < LootBookData.PAGE_SIZE; slot++) {
            templates.setStackInSlot(slot, book.entry(slot).stack().copy());
            writeSlot(slot);
        }
        suppress = false;
        values[0] = index;
        values[1] = book.pageCount();
        broadcastChanges();
    }

    private void showSlot(int slot) {
        suppress = true;
        templates.setStackInSlot(slot, book.entry(slot).stack().copy());
        suppress = false;
        writeSlot(slot);
        broadcastChanges();
    }

    private void writeSlot(int slot) {
        LootBookData.Entry entry = book.entry(slot);
        values[2 + slot * SLOT_DATA] = entry.min();
        values[3 + slot * SLOT_DATA] = entry.max();
        values[4 + slot * SLOT_DATA] = entry.chance();
        values[5 + slot * SLOT_DATA] = entry.randomDamage() ? 1 : 0;
        values[6 + slot * SLOT_DATA] = entry.randomEnchant() ? 1 : 0;
    }

    private void save() {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.LOOT_BOOK.get())) {
            return;
        }
        book.write(stack.getOrCreateTag());
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setItemInHand(hand, stack);
        }
        broadcastChanges();
    }

    public static final class Provider implements MenuProvider {
        private final InteractionHand hand;

        public Provider(InteractionHand hand) {
            this.hand = hand;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.cts.loot_book");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new LootBookMenu(id, inventory, hand);
        }
    }
}
