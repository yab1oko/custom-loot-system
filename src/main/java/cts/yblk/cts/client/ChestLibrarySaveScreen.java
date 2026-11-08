package cts.yblk.cts.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.SaveChestToLibraryPacket;

public class ChestLibrarySaveScreen extends Screen {
    private final BlockPos pos;
    private final String suggestedName;
    private EditBox nameBox;

    public ChestLibrarySaveScreen(BlockPos pos, String suggestedName) {
        super(Component.translatable("gui.cts.chest_library.save_title"));
        this.pos = pos;
        this.suggestedName = suggestedName == null || suggestedName.isBlank() ? "Chest" : suggestedName;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;
        nameBox = new EditBox(font, centerX - 100, centerY - 10, 200, 20, Component.translatable("gui.cts.chest_library.name"));
        nameBox.setMaxLength(64);
        nameBox.setValue(suggestedName);
        addRenderableWidget(nameBox);
        setInitialFocus(nameBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.cts.chest_library.save"), button -> {
            String name = nameBox.getValue();
            ModNetwork.CHANNEL.sendToServer(new SaveChestToLibraryPacket(pos, name));
            onClose();
        }).bounds(centerX - 100, centerY + 20, 95, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cts.cancel"), button -> onClose())
                .bounds(centerX + 5, centerY + 20, 95, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 40, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
