package cts.yblk.cts.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import cts.yblk.cts.blockentity.EntityPlateBlockEntity;
import cts.yblk.cts.menu.EntityPlateMenu;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.UpdateEntityPlatePacket;

public class EntityPlateScreen extends AbstractContainerScreen<EntityPlateMenu> {
    private final EditBox[] idBoxes = new EditBox[EntityPlateBlockEntity.ID_COUNT];
    private final EditBox[] chanceBoxes = new EditBox[EntityPlateBlockEntity.ID_COUNT];
    private final LootTableSuggestions[] idSuggestions = new LootTableSuggestions[EntityPlateBlockEntity.ID_COUNT];
    private EditBox respawnBox;
    private EditBox radiusBox;
    private CountSlider countSlider;
    private boolean randomize;
    private boolean sentOnClose;

    public EntityPlateScreen(EntityPlateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 230;
        this.titleLabelY = 6;
        this.randomize = menu.getPlate().isRandomize();
    }

    @Override
    protected void init() {
        super.init();
        EntityPlateBlockEntity plate = menu.getPlate();
        this.randomize = plate.isRandomize();
        this.sentOnClose = false;

        addRenderableWidget(Button.builder(modeLabel(), button -> {
            randomize = !randomize;
            button.setMessage(modeLabel());
        }).bounds(leftPos + 98, topPos + 18, 70, 16).build());

        countSlider = new CountSlider(leftPos + 8, topPos + 18, 84, 16, plate.getCount());
        addRenderableWidget(countSlider);

        respawnBox = numberBox(leftPos + 8, topPos + 52, plate.getRespawnSeconds());
        radiusBox = numberBox(leftPos + 98, topPos + 52, plate.getRadius());
        addRenderableWidget(respawnBox);
        addRenderableWidget(radiusBox);

        for (int i = 0; i < idBoxes.length; i++) {
            EditBox box = new EditBox(this.font, leftPos + 8, topPos + 86 + i * 15, 112, 14, Component.translatable("gui.cts.entity"));
            box.setMaxLength(128);
            box.setValue(plate.getEntityId(i));
            addRenderableWidget(box);
            idBoxes[i] = box;
            LootTableSuggestions suggestions = new LootTableSuggestions(() -> 0, EntityIdIndex::ids);
            suggestions.attach(box);
            idSuggestions[i] = suggestions;

            EditBox chance = numberBox(leftPos + 126, topPos + 86 + i * 15, plate.getChance(i));
            chance.setMaxLength(3);
            addRenderableWidget(chance);
            chanceBoxes[i] = chance;
        }
    }

    private EditBox numberBox(int x, int y, int value) {
        EditBox box = new EditBox(this.font, x, y, 40, 14, Component.literal(""));
        box.setMaxLength(8);
        box.setValue(Integer.toString(value));
        box.setFilter(text -> text.isEmpty() || text.chars().allMatch(Character::isDigit));
        return box;
    }

    private Component modeLabel() {
        return Component.translatable(randomize ? "gui.cts.random" : "gui.cts.all");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (LootTableSuggestions suggestions : idSuggestions) {
            if (suggestions != null && suggestions.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        if (getFocused() instanceof EditBox && minecraft != null
                && minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (LootTableSuggestions suggestions : idSuggestions) {
            if (suggestions != null && suggestions.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (LootTableSuggestions suggestions : idSuggestions) {
            if (suggestions != null && suggestions.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
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
        if (respawnBox == null || radiusBox == null || countSlider == null) {
            return;
        }
        String[] ids = new String[EntityPlateBlockEntity.ID_COUNT];
        int[] chances = new int[EntityPlateBlockEntity.ID_COUNT];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = idBoxes[i] == null ? "" : idBoxes[i].getValue();
            chances[i] = chanceBoxes[i] == null
                    ? menu.getPlate().getChance(i)
                    : Math.max(0, Math.min(100, parse(chanceBoxes[i].getValue(), menu.getPlate().getChance(i))));
        }
        ModNetwork.CHANNEL.sendToServer(new UpdateEntityPlatePacket(
                menu.getPlate().getBlockPos(),
                ids,
                chances,
                randomize,
                countSlider.count(),
                parse(respawnBox.getValue(), menu.getPlate().getRespawnSeconds()),
                parse(radiusBox.getValue(), menu.getPlate().getRadius())
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
        graphics.drawString(this.font, Component.translatable("gui.cts.respawn_label"), 8, 40, 0xC6C6C6, false);
        graphics.drawString(this.font, Component.translatable("gui.cts.radius"), 98, 40, 0xC6C6C6, false);
        graphics.drawString(this.font, Component.translatable("gui.cts.entities"), 8, 72, 0xC6C6C6, false);
        graphics.drawString(this.font, Component.translatable("gui.cts.chance"), 126, 72, 0xC6C6C6, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        for (LootTableSuggestions suggestions : idSuggestions) {
            if (suggestions != null) {
                suggestions.render(graphics, mouseX, mouseY);
            }
        }
    }

    private static final class CountSlider extends AbstractSliderButton {
        private CountSlider(int x, int y, int width, int height, int count) {
            super(x, y, width, height, Component.empty(), (Math.max(1, Math.min(EntityPlateBlockEntity.MAX_COUNT, count)) - 1) / (double) (EntityPlateBlockEntity.MAX_COUNT - 1));
            updateMessage();
        }

        private int count() {
            return 1 + (int) Math.round(this.value * (EntityPlateBlockEntity.MAX_COUNT - 1));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.cts.count", count()));
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }
    }
}
