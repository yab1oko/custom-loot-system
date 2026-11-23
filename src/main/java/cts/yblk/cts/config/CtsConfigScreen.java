package cts.yblk.cts.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import cts.yblk.cts.Cts;
import cts.yblk.cts.client.ui.CtsTheme;
import cts.yblk.cts.client.ui.ThemeRenderer;
import cts.yblk.cts.client.ui.ThemedButton;
import cts.yblk.cts.client.ui.ThemedEditBox;

/**
 * Vanilla config UI — always works in userdev / without Cloth.
 * Cloth is preferred in production when available ({@link ClothConfigHook}).
 */
public final class CtsConfigScreen extends Screen {
    private final Screen parent;
    private EditBox respawnBox;
    private EditBox lootTableBox;

    public CtsConfigScreen(Screen parent) {
        super(Component.translatable("screen." + Cts.MODID + ".config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 48;

        addRenderableWidget(ThemedButton.create(themeLabel(), button -> {
            CtsTheme next = CtsTheme.current().next();
            CtsConfig.setCreativeTheme(next.id());
            CtsConfig.save();
            button.setMessage(themeLabel());
        }).bounds(cx - 100, y, 200, 20).build());

        addRenderableWidget(ThemedButton.create(soundsLabel(), button -> {
            CtsConfig.setButtonSounds(!CtsConfig.buttonSounds());
            CtsConfig.save();
            button.setMessage(soundsLabel());
        }).bounds(cx - 100, y + 36, 200, 20).build());

        addRenderableWidget(ThemedButton.create(refillLabel(), button -> {
            CtsConfig.setRefillSound(!CtsConfig.refillSound());
            CtsConfig.save();
            button.setMessage(refillLabel());
        }).bounds(cx - 100, y + 60, 200, 20).build());

        respawnBox = new ThemedEditBox(this.font, cx - 100, y + 104, 200, 16,
                Component.translatable("config." + Cts.MODID + ".loot.defaultRespawnSeconds"));
        respawnBox.setValue(Integer.toString(CtsConfig.defaultLootRespawnSeconds()));
        respawnBox.setMaxLength(8);
        addRenderableWidget(respawnBox);

        lootTableBox = new ThemedEditBox(this.font, cx - 100, y + 146, 200, 16,
                Component.translatable("config." + Cts.MODID + ".airdrop.defaultLootTable"));
        lootTableBox.setValue(CtsConfig.defaultAirdropLootTable());
        lootTableBox.setMaxLength(128);
        addRenderableWidget(lootTableBox);

        addRenderableWidget(ThemedButton.create(CommonComponents.GUI_DONE, button -> {
            save();
            onClose();
        }).bounds(cx - 100, this.height - 32, 200, 20).build());
    }

    private static Component themeLabel() {
        return Component.translatable("config." + Cts.MODID + ".ui.creativeTheme")
                .append(": ")
                .append(Component.translatable(CtsTheme.current().nameKey()));
    }

    private static Component soundsLabel() {
        return Component.translatable("config." + Cts.MODID + ".ui.buttonSounds")
                .append(": ")
                .append(Component.translatable(CtsConfig.buttonSounds() ? "config.cts.ui.on" : "config.cts.ui.off"));
    }

    private static Component refillLabel() {
        return Component.translatable("config." + Cts.MODID + ".ui.refillSound")
                .append(": ")
                .append(Component.translatable(CtsConfig.refillSound() ? "config.cts.ui.on" : "config.cts.ui.off"));
    }

    private void save() {
        try {
            CtsConfig.setDefaultLootRespawnSeconds(Integer.parseInt(respawnBox.getValue().trim()));
        } catch (NumberFormatException ignored) {
            // keep previous
        }
        CtsConfig.setDefaultAirdropLootTable(lootTableBox.getValue());
        CtsConfig.save();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int cx = this.width / 2;
        int panelTop = 28;
        int panelBottom = this.height - 12;
        ThemeRenderer.panel(graphics, cx - 120, panelTop, 240, panelBottom - panelTop);
        CtsTheme theme = CtsTheme.current();
        graphics.drawCenteredString(this.font, this.title, cx, 36, theme.ink());
        graphics.drawCenteredString(this.font,
                Component.translatable(theme.descriptionKey()),
                cx, 72, theme.muted());
        graphics.drawString(this.font,
                Component.translatable("config." + Cts.MODID + ".loot.defaultRespawnSeconds"),
                cx - 100, 140, theme.muted());
        graphics.drawString(this.font,
                Component.translatable("config." + Cts.MODID + ".airdrop.defaultLootTable"),
                cx - 100, 182, theme.muted());
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}
