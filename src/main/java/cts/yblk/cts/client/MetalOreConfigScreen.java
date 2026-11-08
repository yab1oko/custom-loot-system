package cts.yblk.cts.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import cts.yblk.cts.menu.MetalOreConfigMenu;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.UpdateMetalOrePacket;

public class MetalOreConfigScreen extends AbstractContainerScreen<MetalOreConfigMenu> {
    private EditBox respawnBox;
    private boolean sentOnClose;

    public MetalOreConfigScreen(MetalOreConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 90;
        this.titleLabelY = 8;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.sentOnClose = false;
        respawnBox = new EditBox(this.font, leftPos + 8, topPos + 40, 60, 16, Component.translatable("gui.cts.respawn"));
        respawnBox.setMaxLength(8);
        respawnBox.setValue(Integer.toString(menu.getOre().getRespawnSeconds()));
        respawnBox.setFilter(text -> text.isEmpty() || text.chars().allMatch(Character::isDigit));
        addRenderableWidget(respawnBox);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() instanceof EditBox && minecraft != null
                && minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
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
        if (respawnBox == null) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(new UpdateMetalOrePacket(
                menu.getOre().getBlockPos(),
                parse(respawnBox.getValue(), menu.getOre().getRespawnSeconds())
        ));
    }

    private static int parse(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0101010);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFF6E6E6E);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.cts.respawn_label"), 8, 28, 0xC6C6C6, false);
        graphics.drawString(this.font, Component.translatable("gui.cts.respawn_hint_ore"), 72, 44, 0x808080, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
