package cts.yblk.cts.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import cts.yblk.cts.item.LootBookData;
import cts.yblk.cts.item.TagSlipItem;
import cts.yblk.cts.menu.LootBookMenu;
import cts.yblk.cts.network.ExportLootBookPacket;
import cts.yblk.cts.network.LoadLootBookPacket;
import cts.yblk.cts.network.LootBookNamePacket;
import cts.yblk.cts.network.LootBookPagePacket;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.RequestLootTablesPacket;
import cts.yblk.cts.network.UpdateLootBookEntryPacket;

import java.util.ArrayList;
import java.util.List;

public class LootBookScreen extends AbstractContainerScreen<LootBookMenu> {
    private static final ResourceLocation INVENTORY = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int PANEL = 0xFF161616;
    private static final int BORDER = 0xFF6A6A6A;
    private static final int SLOT_BG = 0xFF1A1A1A;
    private static final int SLOT_EDGE = 0xFF3A3A3A;
    private static final int TEXT = 0xFFFFFF;
    private static final int MUTED = 0xA0A0A0;

    private final ItemEntryEditor editor = new ItemEntryEditor();
    private final LootTableSuggestions lootSuggestions = new LootTableSuggestions();
    private EditBox nameBox;
    private Button previousButton;
    private Button nextButton;
    private int shownPage = -1;

    public LootBookScreen(LootBookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 114 + LootBookData.ROWS * 18 + LootBookMenu.HEADER + LootBookMenu.PAGE_BAR;
        this.titleLabelY = 6;
        this.inventoryLabelX = LootBookMenu.INVENTORY_X;
        this.inventoryLabelY = LootBookMenu.INVENTORY_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        nameBox = new EditBox(this.font, leftPos + 8, topPos + 18, imageWidth - 16, 16, Component.translatable("gui.cts.loot_table"));
        nameBox.setMaxLength(256);
        nameBox.setTextColor(TEXT);
        nameBox.setTextColorUneditable(MUTED);
        nameBox.setValue(menu.book().name());
        lootSuggestions.attach(nameBox);
        addRenderableWidget(nameBox);
        int buttonY = topPos + 38;
        int buttonW = (imageWidth - 20) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.cts.load"), button ->
                ModNetwork.CHANNEL.sendToServer(new LoadLootBookPacket(nameBox.getValue())))
                .bounds(leftPos + 8, buttonY, buttonW, 18)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cts.export"), button ->
                ModNetwork.CHANNEL.sendToServer(new ExportLootBookPacket(nameBox.getValue())))
                .bounds(leftPos + 12 + buttonW, buttonY, buttonW, 18)
                .build());
        int pageY = topPos + LootBookMenu.SLOT_Y + LootBookData.ROWS * 18 + 1;
        previousButton = addRenderableWidget(Button.builder(Component.literal("<"), button ->
                ModNetwork.CHANNEL.sendToServer(new LootBookPagePacket(-1)))
                .bounds(leftPos + LootBookMenu.SLOT_X, pageY, 20, 16)
                .build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), button ->
                ModNetwork.CHANNEL.sendToServer(new LootBookPagePacket(1)))
                .bounds(leftPos + LootBookMenu.SLOT_X + LootBookData.COLUMNS * 18 - 20, pageY, 20, 16)
                .build());
        updatePageButtons();
        ModNetwork.CHANNEL.sendToServer(new RequestLootTablesPacket());
    }

    public void onBookSynced() {
        if (nameBox != null) {
            nameBox.setValue(menu.book().name());
        }
        updatePageButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (nameBox != null) {
            nameBox.tick();
        }
        if (shownPage != menu.page()) {
            updatePageButtons();
        }
    }

    private void updatePageButtons() {
        shownPage = menu.page();
        previousButton.visible = shownPage > 0;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int bookBottom = y + LootBookMenu.INVENTORY_Y - 14;
        graphics.fill(x, y, x + w, bookBottom, PANEL);
        graphics.fill(x, y, x + w, y + 1, BORDER);
        graphics.fill(x, bookBottom - 1, x + w, bookBottom, BORDER);
        graphics.fill(x, y, x + 1, bookBottom, BORDER);
        graphics.fill(x + w - 1, y, x + w, bookBottom, BORDER);
        for (Slot slot : menu.slots) {
            if (slot.index < LootBookData.PAGE_SIZE) {
                drawSlot(graphics, x + slot.x - 1, y + slot.y - 1);
            }
        }
        graphics.blit(INVENTORY, x, bookBottom, 0, 126, w, 96);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT_EDGE);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BG);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        String page = (menu.page() + 1) + "/" + menu.pageCount();
        graphics.drawString(this.font, page, (imageWidth - this.font.width(page)) / 2,
                LootBookMenu.SLOT_Y + LootBookData.ROWS * 18 + 5, MUTED, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!editor.isOpen()) {
            ItemEntryEditor.drawEntries(graphics, this.font, leftPos, topPos, menu.slots, LootBookData.PAGE_SIZE,
                    menu::chance, menu::countMin, menu::countMax,
                    index -> !menu.template(index).isEmpty(),
                    index -> menu.template(index).isDamageableItem(),
                    menu::randomDamage,
                    menu::randomEnchant);
        }
        if (editor.isOpen()) {
            editor.render(graphics, this.font, mouseX, mouseY);
        } else {
            lootSuggestions.render(graphics, mouseX, mouseY);
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        if (editor.isOpen()) {
            editor.save();
        }
        if (nameBox != null) {
            ModNetwork.CHANNEL.sendToServer(new LootBookNamePacket(nameBox.getValue()));
        }
        lootSuggestions.hide();
        super.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editor.isOpen() && editor.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (editor.isOpen() && keyCode == 256) {
            editor.save();
            return true;
        }
        if (!editor.isOpen() && lootSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (nameBox != null && nameBox.isFocused() && minecraft != null
                && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (editor.isOpen() && editor.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (editor.isOpen()) {
            if (editor.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            editor.save();
            return true;
        }
        if (lootSuggestions.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (nameBox != null && nameBox.isMouseOver(mouseX, mouseY)) {
            lootSuggestions.reveal();
        }
        Slot slot = templateAt(mouseX, mouseY);
        if (button == 1 && menu.getCarried().isEmpty() && slot != null && slot.hasItem()) {
            openEditor(slot.index);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (editor.isOpen() && editor.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        if (!editor.isOpen() && lootSuggestions.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (editor.isOpen() && editor.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (editor.isOpen() && editor.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void openEditor(int slot) {
        ItemStack stack = menu.template(slot);
        if (stack.isEmpty()) {
            return;
        }
        editor.open(slot, menu.countMin(slot), menu.countMax(slot), menu.chance(slot),
                stack.isDamageableItem(), menu.randomDamage(slot), menu.randomEnchant(slot),
                TagSlipItem.isSlip(stack), TagSlipItem.read(stack), TagSlipItem.pickToken(stack), stack, this.font,
                leftPos + 8, topPos + 18, result ->
                ModNetwork.CHANNEL.sendToServer(new UpdateLootBookEntryPacket(
                        result.slot(), result.min(), result.max(), result.chance(), result.randomDamage(), result.randomEnchant(), result.tag(), result.pick())));
    }

    private Slot templateAt(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            if (slot.index < LootBookData.PAGE_SIZE
                    && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> lines = new ArrayList<>(super.getTooltipFromContainerItem(stack));
        if (menu.getCarried().isEmpty() && hoveredSlot != null
                && hoveredSlot.index < LootBookData.PAGE_SIZE && hoveredSlot.hasItem()) {
            lines.add(Component.translatable("gui.cts.rmb_open").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("gui.cts.lmb_remove").withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
