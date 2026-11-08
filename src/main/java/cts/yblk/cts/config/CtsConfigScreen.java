package cts.yblk.cts.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import cts.yblk.cts.Cts;

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
        int y = this.height / 4;

        respawnBox = new EditBox(this.font, cx - 100, y + 24, 200, 20,
                Component.translatable("config." + Cts.MODID + ".loot.defaultRespawnSeconds"));
        respawnBox.setValue(Integer.toString(CtsConfig.defaultLootRespawnSeconds()));
        respawnBox.setMaxLength(8);
        addRenderableWidget(respawnBox);

        lootTableBox = new EditBox(this.font, cx - 100, y + 72, 200, 20,
                Component.translatable("config." + Cts.MODID + ".airdrop.defaultLootTable"));
        lootTableBox.setValue(CtsConfig.defaultAirdropLootTable());
        lootTableBox.setMaxLength(128);
        addRenderableWidget(lootTableBox);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            save();
            onClose();
        }).bounds(cx - 100, this.height - 40, 200, 20).build());
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
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        int cx = this.width / 2;
        int y = this.height / 4;
        graphics.drawString(this.font,
                Component.translatable("config." + Cts.MODID + ".loot.defaultRespawnSeconds"),
                cx - 100, y + 12, 0xA0A0A0);
        graphics.drawString(this.font,
                Component.translatable("config." + Cts.MODID + ".airdrop.defaultLootTable"),
                cx - 100, y + 60, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}
