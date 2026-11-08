package cts.yblk.cts.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.library.SavedChestLibrary;

public class ChestLibraryPreviewScreen extends Screen {
    private final Screen parent;
    private final SavedChestLibrary.Entry entry;
    private final ItemStackHandler template = new ItemStackHandler(CanStashBlockEntity.SLOT_COUNT);
    private final String mode;
    private final String lootTable;

    public ChestLibraryPreviewScreen(Screen parent, SavedChestLibrary.Entry entry) {
        super(Component.translatable("gui.cts.chest_library.preview_title", entry.name()));
        this.parent = parent;
        this.entry = entry;
        CompoundTag stash = entry.stash();
        if (stash.contains("Template")) {
            template.deserializeNBT(stash.getCompound("Template"));
        }
        this.mode = stash.getString("Mode");
        this.lootTable = stash.getString("LootTable");
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.cts.done"), button -> minecraft.setScreen(parent))
                .bounds(width / 2 - 40, height / 2 + 70, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 90, 0xFFFFFF);

        boolean lootMode = "LOOT_TABLE".equals(mode);
        if (lootMode) {
            graphics.drawCenteredString(font, Component.translatable("gui.cts.mode.loot_table"),
                    width / 2, height / 2 - 70, 0xA0A0A0);
            String table = lootTable == null || lootTable.isBlank() ? "-" : lootTable;
            graphics.drawCenteredString(font, table, width / 2, height / 2 - 55, 0xFFFFFF);
        } else {
            graphics.drawCenteredString(font, Component.translatable("gui.cts.mode.items"),
                    width / 2, height / 2 - 70, 0xA0A0A0);
            int left = width / 2 - (9 * 18) / 2;
            int top = height / 2 - 40;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    int slot = col + row * 9;
                    int x = left + col * 18;
                    int y = top + row * 18;
                    graphics.fill(x, y, x + 16, y + 16, 0xFF1A1A1A);
                    graphics.fill(x, y, x + 16, y + 1, 0xFF3A3A3A);
                    graphics.fill(x, y, x + 1, y + 16, 0xFF3A3A3A);
                    ItemStack stack = template.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        graphics.renderItem(stack, x, y);
                        graphics.renderItemDecorations(font, stack, x, y);
                    }
                }
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
