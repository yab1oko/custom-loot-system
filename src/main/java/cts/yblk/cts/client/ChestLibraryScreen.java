package cts.yblk.cts.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import cts.yblk.cts.library.SavedChestLibrary;
import cts.yblk.cts.network.DeleteSavedChestPacket;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.RenameSavedChestPacket;
import cts.yblk.cts.network.RequestChestLibraryPacket;
import cts.yblk.cts.network.TakeSavedChestPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChestLibraryScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int PANEL_WIDTH = 320;

    private final List<Button> rowButtons = new ArrayList<>();
    private int scroll;
    private UUID renamingId;
    private EditBox renameBox;

    public ChestLibraryScreen() {
        super(Component.translatable("gui.cts.chest_library.title"));
    }

    public void onLibrarySynced() {
        rebuildRows();
    }

    @Override
    protected void init() {
        scroll = 0;
        renamingId = null;
        renameBox = null;
        addRenderableWidget(Button.builder(Component.translatable("gui.cts.chest_library.refresh"), button ->
                ModNetwork.CHANNEL.sendToServer(new RequestChestLibraryPacket()))
                .bounds(width / 2 - PANEL_WIDTH / 2, 28, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cts.done"), button -> onClose())
                .bounds(width / 2 + PANEL_WIDTH / 2 - 80, 28, 80, 20).build());
        rebuildRows();
    }

    private void rebuildRows() {
        for (Button button : rowButtons) {
            removeWidget(button);
        }
        rowButtons.clear();
        if (renameBox != null) {
            removeWidget(renameBox);
            renameBox = null;
        }

        List<SavedChestLibrary.Entry> entries = ChestLibraryClient.entries();
        int listTop = 56;
        int visible = Math.max(1, (height - listTop - 20) / ROW_HEIGHT);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, entries.size() - visible)));

        int y = listTop;
        int end = Math.min(entries.size(), scroll + visible);
        for (int i = scroll; i < end; i++) {
            SavedChestLibrary.Entry entry = entries.get(i);
            int rowY = y;
            UUID id = entry.id();

            if (id.equals(renamingId)) {
                renameBox = new EditBox(font, width / 2 - PANEL_WIDTH / 2, rowY, 140, 20,
                        Component.translatable("gui.cts.chest_library.name"));
                renameBox.setMaxLength(64);
                renameBox.setValue(entry.name());
                addRenderableWidget(renameBox);
                rowButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.cts.done"), button -> {
                    ModNetwork.CHANNEL.sendToServer(new RenameSavedChestPacket(id, renameBox.getValue()));
                    renamingId = null;
                }).bounds(width / 2 - PANEL_WIDTH / 2 + 145, rowY, 50, 20).build()));
                rowButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.cts.cancel"), button -> {
                    renamingId = null;
                    rebuildRows();
                }).bounds(width / 2 - PANEL_WIDTH / 2 + 200, rowY, 50, 20).build()));
            } else {
                rowButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.cts.chest_library.preview"), button ->
                        minecraft.setScreen(new ChestLibraryPreviewScreen(this, entry)))
                        .bounds(width / 2 - PANEL_WIDTH / 2 + 150, rowY, 50, 20).build()));
                rowButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.cts.chest_library.take"), button ->
                        ModNetwork.CHANNEL.sendToServer(new TakeSavedChestPacket(id)))
                        .bounds(width / 2 - PANEL_WIDTH / 2 + 205, rowY, 40, 20).build()));
                rowButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.cts.chest_library.rename"), button -> {
                    renamingId = id;
                    rebuildRows();
                }).bounds(width / 2 - PANEL_WIDTH / 2 + 250, rowY, 50, 20).build()));
                rowButtons.add(addRenderableWidget(Button.builder(Component.literal("X"), button ->
                        ModNetwork.CHANNEL.sendToServer(new DeleteSavedChestPacket(id)))
                        .bounds(width / 2 + PANEL_WIDTH / 2 - 22, rowY, 20, 20).build()));
            }
            y += ROW_HEIGHT;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<SavedChestLibrary.Entry> entries = ChestLibraryClient.entries();
        int visible = Math.max(1, (height - 76) / ROW_HEIGHT);
        if (entries.size() > visible) {
            scroll = (int) Math.max(0, Math.min(entries.size() - visible, scroll - Math.signum(delta)));
            rebuildRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);

        List<SavedChestLibrary.Entry> entries = ChestLibraryClient.entries();
        if (entries.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gui.cts.chest_library.empty"),
                    width / 2, height / 2, 0xA0A0A0);
        } else {
            int listTop = 56;
            int visible = Math.max(1, (height - listTop - 20) / ROW_HEIGHT);
            int end = Math.min(entries.size(), scroll + visible);
            int y = listTop;
            int left = width / 2 - PANEL_WIDTH / 2;
            for (int i = scroll; i < end; i++) {
                SavedChestLibrary.Entry entry = entries.get(i);
                graphics.fill(left - 4, y - 2, left + PANEL_WIDTH + 4, y + 22, 0x66000000);
                if (!entry.id().equals(renamingId)) {
                    graphics.drawString(font, entry.name(), left, y + 6, 0xFFFFFF, false);
                }
                y += ROW_HEIGHT;
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
