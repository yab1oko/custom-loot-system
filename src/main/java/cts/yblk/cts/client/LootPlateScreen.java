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
import cts.yblk.cts.item.TagSlipItem;

import java.util.ArrayList;
import java.util.List;

import cts.yblk.cts.blockentity.LootPlateBlockEntity;
import cts.yblk.cts.menu.LootPlateMenu;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.RequestLootTablesPacket;
import cts.yblk.cts.network.UpdateItemEntryPacket;
import cts.yblk.cts.network.UpdateLootPlatePacket;

public class LootPlateScreen extends AbstractContainerScreen<LootPlateMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int SETTINGS = LootPlateMenu.SETTINGS_PIXELS;

    private EditBox lootTableBox;
    private EditBox respawnBox;
    private LootPlateBlockEntity.Mode mode;
    private boolean sentOnClose;
    private final ItemEntryEditor editor = new ItemEntryEditor();
    private final LootTableSuggestions lootSuggestions = new LootTableSuggestions();

    public LootPlateScreen(LootPlateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 168 + SETTINGS + LootPlateMenu.TOP_EXTRA;
        this.inventoryLabelY = 72 + SETTINGS + LootPlateMenu.TOP_EXTRA;
        this.titleLabelY = 6;
        this.mode = menu.getPlate().getMode();
    }

    @Override
    protected void init() {
        super.init();
        LootPlateBlockEntity plate = menu.getPlate();
        this.mode = plate.getMode();
        this.sentOnClose = false;

        addRenderableWidget(Button.builder(modeLabel(), button -> {
            mode = mode == LootPlateBlockEntity.Mode.ITEMS
                    ? LootPlateBlockEntity.Mode.LOOT_TABLE
                    : LootPlateBlockEntity.Mode.ITEMS;
            menu.getPlate().setMode(mode);
            button.setMessage(modeLabel());
            updateModeWidgets();
            sendSettings();
        }).bounds(leftPos + imageWidth - 78, topPos + 4, 70, 16).build());

        lootTableBox = new EditBox(this.font, leftPos + 8, topPos + 36 + LootPlateMenu.TOP_EXTRA, 160, 16, Component.translatable("gui.cts.loot_table"));
        lootTableBox.setMaxLength(256);
        lootTableBox.setValue(plate.getLootTableId());
        lootSuggestions.attach(lootTableBox);
        addRenderableWidget(lootTableBox);
        ModNetwork.CHANNEL.sendToServer(new RequestLootTablesPacket());

        respawnBox = new EditBox(this.font, leftPos + 8, topPos + 73 + LootPlateMenu.TOP_EXTRA, 40, 16, Component.translatable("gui.cts.respawn"));
        respawnBox.setMaxLength(8);
        respawnBox.setValue(Integer.toString(plate.getRespawnSeconds()));
        respawnBox.setFilter(text -> text.isEmpty() || text.chars().allMatch(Character::isDigit));
        addRenderableWidget(respawnBox);

        updateModeWidgets();
    }

    private Component modeLabel() {
        return Component.translatable(mode == LootPlateBlockEntity.Mode.ITEMS
                ? "gui.cts.mode.items"
                : "gui.cts.mode.loot_table");
    }

    private boolean isItemMode() {
        return mode == LootPlateBlockEntity.Mode.ITEMS;
    }

    private void updateModeWidgets() {
        if (lootTableBox != null) {
            lootTableBox.visible = !isItemMode();
            if (isItemMode()) {
                lootTableBox.setFocused(false);
                lootSuggestions.hide();
            }
        }
    }

    @Override
    public void onClose() {
        if (editor.isOpen()) {
            editor.save();
        }
        sendSettingsOnce();
        super.onClose();
    }

    @Override
    public void removed() {
        sendSettingsOnce();
        super.removed();
    }

    private void sendSettingsOnce() {
        if (!sentOnClose) {
            sentOnClose = true;
            sendSettings();
        }
    }

    private void sendSettings() {
        if (lootTableBox == null || respawnBox == null) {
            return;
        }
        int seconds = menu.getPlate().getRespawnSeconds();
        String raw = respawnBox.getValue();
        if (raw != null && !raw.isBlank()) {
            try {
                seconds = Integer.parseInt(raw);
            } catch (NumberFormatException ignored) {
            }
        }
        ModNetwork.CHANNEL.sendToServer(new UpdateLootPlatePacket(
                menu.getPlate().getBlockPos(),
                mode,
                lootTableBox.getValue(),
                seconds
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        int extra = LootPlateMenu.TOP_EXTRA;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, 17);
        for (int i = 0; i < extra; i++) {
            graphics.blit(TEXTURE, x, y + 17 + i, 0, 16, imageWidth, 1);
        }
        int gridY = y + 17 + extra;
        if (isItemMode()) {
            graphics.blit(TEXTURE, x, gridY, 0, 17, imageWidth, 54);
        } else {
            for (int i = 0; i < 54; i++) {
                graphics.blit(TEXTURE, x, gridY + i, 0, 16, imageWidth, 1);
            }
        }
        int settingsY = gridY + 54;
        for (int i = 0; i < SETTINGS; i++) {
            graphics.blit(TEXTURE, x, settingsY + i, 0, 16, imageWidth, 1);
        }
        graphics.blit(TEXTURE, x, settingsY + SETTINGS, 0, 126, imageWidth, 96);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        int extra = LootPlateMenu.TOP_EXTRA;
        graphics.drawString(this.font, Component.translatable("gui.cts.respawn"), 52, 77 + extra, 0x404040, false);
        if (!isItemMode()) {
            graphics.drawString(this.font, Component.translatable("gui.cts.loot_table"), 8, 22 + extra, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isItemMode()) {
            ItemEntryEditor.drawEntries(graphics, this.font, leftPos, topPos, menu.slots, LootPlateBlockEntity.SLOT_COUNT,
                    menu.getPlate()::getChance, menu.getPlate()::getCountMin, menu.getPlate()::getCountMax,
                    index -> !menu.getPlate().getTemplate().getStackInSlot(index).isEmpty(),
                    index -> menu.getPlate().getTemplate().getStackInSlot(index).isDamageableItem(),
                    menu.getPlate()::isRandomDamage,
                    menu.getPlate()::isRandomEnchant);
        }
        if (editor.isOpen()) {
            editor.render(graphics, this.font, mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
        if (!isItemMode()) {
            lootSuggestions.render(graphics, mouseX, mouseY);
        }
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
        if (!isItemMode() && lootSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if ((lootTableBox.isFocused() || respawnBox.isFocused())
                && minecraft != null
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
        if (!isItemMode() && lootSuggestions.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        Slot slot = templateAt(mouseX, mouseY);
        if (button == 1 && isItemMode() && menu.getCarried().isEmpty() && slot != null && slot.hasItem()) {
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
        if (!isItemMode() && lootSuggestions.mouseScrolled(mouseX, mouseY, delta)) {
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
        ItemStack stack = menu.getPlate().getTemplate().getStackInSlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        editor.open(slot, menu.getPlate().getCountMin(slot), menu.getPlate().getCountMax(slot), menu.getPlate().getChance(slot),
                stack.isDamageableItem(), menu.getPlate().isRandomDamage(slot), menu.getPlate().isRandomEnchant(slot),
                TagSlipItem.isSlip(stack), TagSlipItem.read(stack), TagSlipItem.pickToken(stack), stack, this.font,
                leftPos + 8, topPos + 18, result ->
                ModNetwork.CHANNEL.sendToServer(new UpdateItemEntryPacket(
                        menu.getPlate().getBlockPos(), result.slot(), result.min(), result.max(), result.chance(),
                        result.randomDamage(), result.randomEnchant(), result.tag(), result.pick())));
    }

    private Slot templateAt(double mouseX, double mouseY) {
        if (!isItemMode()) {
            return null;
        }
        for (Slot slot : menu.slots) {
            if (slot.index < LootPlateBlockEntity.SLOT_COUNT
                    && slot.isActive()
                    && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> lines = new ArrayList<>(super.getTooltipFromContainerItem(stack));
        if (isItemMode() && menu.getCarried().isEmpty() && hoveredSlot != null
                && hoveredSlot.index < LootPlateBlockEntity.SLOT_COUNT && hoveredSlot.hasItem()) {
            lines.add(Component.translatable("gui.cts.rmb_open").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("gui.cts.lmb_remove").withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
