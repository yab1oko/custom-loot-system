package cts.yblk.cts.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.client.ui.CtsTheme;
import cts.yblk.cts.client.ui.ThemeRenderer;
import cts.yblk.cts.client.ui.ThemedButton;
import cts.yblk.cts.client.ui.ThemedEditBox;
import cts.yblk.cts.library.SavedChestLibrary;
import cts.yblk.cts.network.DeleteSavedChestPacket;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.RenameSavedChestPacket;
import cts.yblk.cts.network.RequestChestLibraryPacket;
import cts.yblk.cts.network.TakeSavedChestPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChestLibraryScreen extends Screen {
    private static final int ROW = 22;
    private static final int LEFT_W = 168;

    private EditBox searchBox;
    private EditBox renameBox;
    private ThemedButton takeButton;
    private ThemedButton renameButton;
    private ThemedButton deleteButton;
    private ThemedButton confirmButton;
    private ThemedButton cancelButton;
    private int scroll;
    private UUID selectedId;
    private boolean renaming;
    private boolean confirmDelete;
    private final List<SavedChestLibrary.Entry> visible = new ArrayList<>();

    public ChestLibraryScreen() {
        super(Component.translatable("gui.cts.chest_library.title"));
    }

    public void onLibrarySynced() {
        if (selectedId != null && ChestLibraryClient.entries().stream().noneMatch(entry -> entry.id().equals(selectedId))) {
            selectedId = null;
            renaming = false;
            confirmDelete = false;
        }
        refreshVisible();
    }

    @Override
    protected void init() {
        int left = 16;
        int top = 32;
        searchBox = new ThemedEditBox(font, left + 4, top + 6, LEFT_W - 8, 16,
                Component.translatable("gui.cts.chest_library.search"));
        searchBox.setMaxLength(64);
        searchBox.setResponder(text -> {
            scroll = 0;
            refreshVisible();
        });
        addRenderableWidget(searchBox);

        int right = left + LEFT_W + 12;
        int buttonY = this.height - 36;
        takeButton = addRenderableWidget(ThemedButton.create(Component.translatable("gui.cts.chest_library.take"), button -> {
            if (selectedId != null) {
                ModNetwork.CHANNEL.sendToServer(new TakeSavedChestPacket(selectedId));
            }
        }).bounds(right, buttonY, 72, 20).build());
        renameButton = addRenderableWidget(ThemedButton.create(Component.translatable("gui.cts.chest_library.rename"), button -> beginRename())
                .bounds(right + 76, buttonY, 80, 20).build());
        deleteButton = addRenderableWidget(ThemedButton.create(Component.translatable("gui.cts.chest_library.delete"), button -> {
            confirmDelete = true;
            updateButtons();
        }).danger().bounds(right + 160, buttonY, 80, 20).build());
        confirmButton = addRenderableWidget(ThemedButton.create(Component.translatable("gui.cts.confirm"), button -> {
            if (selectedId != null) {
                ModNetwork.CHANNEL.sendToServer(new DeleteSavedChestPacket(selectedId));
            }
            confirmDelete = false;
            updateButtons();
        }).danger().bounds(right, buttonY, 100, 20).build());
        cancelButton = addRenderableWidget(ThemedButton.create(Component.translatable("gui.cts.cancel"), button -> {
            confirmDelete = false;
            renaming = false;
            if (renameBox != null) {
                renameBox.visible = false;
            }
            updateButtons();
        }).bounds(right + 108, buttonY, 100, 20).build());

        renameBox = new ThemedEditBox(font, right, buttonY - 24, 180, 16,
                Component.translatable("gui.cts.chest_library.name"));
        renameBox.setMaxLength(64);
        renameBox.visible = false;
        addRenderableWidget(renameBox);

        addRenderableWidget(ThemedButton.create(Component.translatable("gui.cts.chest_library.refresh"), button ->
                ModNetwork.CHANNEL.sendToServer(new RequestChestLibraryPacket()))
                .bounds(this.width - 96, 8, 80, 18).build());

        refreshVisible();
        updateButtons();
    }

    private void beginRename() {
        SavedChestLibrary.Entry entry = selected();
        if (entry == null) {
            return;
        }
        renaming = true;
        confirmDelete = false;
        renameBox.setValue(entry.name());
        renameBox.visible = true;
        renameBox.setFocused(true);
        setFocused(renameBox);
        updateButtons();
    }

    private void updateButtons() {
        boolean has = selected() != null;
        takeButton.visible = has && !confirmDelete && !renaming;
        renameButton.visible = has && !confirmDelete && !renaming;
        deleteButton.visible = has && !confirmDelete && !renaming;
        confirmButton.visible = has && (confirmDelete || renaming);
        cancelButton.visible = has && (confirmDelete || renaming);
        if (renaming) {
            confirmButton.setMessage(Component.translatable("gui.cts.done"));
            confirmButton.active = true;
        } else if (confirmDelete) {
            confirmButton.setMessage(Component.translatable("gui.cts.confirm"));
        }
        if (renaming) {
            confirmButton = confirmButton;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (renaming && confirmButton != null && confirmButton.isMouseOver(mouseX, mouseY)) {
            if (selectedId != null && renameBox != null) {
                ModNetwork.CHANNEL.sendToServer(new RenameSavedChestPacket(selectedId, renameBox.getValue()));
            }
            renaming = false;
            renameBox.visible = false;
            updateButtons();
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int index = rowAt(mouseX, mouseY);
        if (index >= 0) {
            selectedId = visible.get(index).id();
            renaming = false;
            confirmDelete = false;
            if (renameBox != null) {
                renameBox.visible = false;
            }
            updateButtons();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int visibleRows = listRows();
        if (visible.size() > visibleRows && mouseX < 16 + LEFT_W) {
            scroll = (int) Math.max(0, Math.min(visible.size() - visibleRows, scroll - Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (renaming && keyCode == 257 && selectedId != null && renameBox != null) {
            ModNetwork.CHANNEL.sendToServer(new RenameSavedChestPacket(selectedId, renameBox.getValue()));
            renaming = false;
            renameBox.visible = false;
            updateButtons();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelTop = 28;
        int panelH = this.height - 40;
        ThemeRenderer.panel(graphics, 8, panelTop, this.width - 16, panelH);
        CtsTheme theme = CtsTheme.current();
        graphics.drawString(font, this.title, 16, 10, theme.ink(), false);

        int listTop = 56;
        int rows = listRows();
        int left = 16;
        for (int i = 0; i < rows; i++) {
            int index = scroll + i;
            if (index >= visible.size()) {
                break;
            }
            SavedChestLibrary.Entry entry = visible.get(index);
            int y = listTop + i * ROW;
            boolean selected = entry.id().equals(selectedId);
            ThemeRenderer.listRow(graphics, left, y, LEFT_W, ROW - 2, selected);
            ItemStack icon = icon(entry);
            graphics.renderItem(icon, left + 2, y + 2);
            graphics.drawString(font, trim(entry.name(), 78), left + 22, y + 2, theme.ink(), false);
            graphics.drawString(font, trim(badge(entry), 78), left + 22, y + 11, theme.muted(), false);
        }
        if (visible.size() > rows) {
            int track = rows * ROW;
            int thumb = Math.max(12, track * rows / visible.size());
            int travel = Math.max(1, visible.size() - rows);
            int thumbY = listTop + (track - thumb) * scroll / travel;
            ThemeRenderer.scrollbar(graphics, left + LEFT_W - 8, listTop, track, thumbY, thumb);
        }
        if (visible.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.cts.chest_library.empty"), left + 4, listTop + 8, theme.ink(), false);
            graphics.drawWordWrap(font, Component.translatable("gui.cts.chest_library.hint"), left + 4, listTop + 22, LEFT_W - 12, theme.muted());
        }

        int right = left + LEFT_W + 12;
        SavedChestLibrary.Entry entry = selected();
        if (entry != null) {
            drawDetail(graphics, theme, entry, right, listTop);
        }
        if (confirmDelete && entry != null) {
            graphics.drawString(font, Component.translatable("gui.cts.chest_library.delete_title", entry.name()),
                    right, this.height - 52, theme.danger(), false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawDetail(GuiGraphics graphics, CtsTheme theme, SavedChestLibrary.Entry entry, int x, int y) {
        CompoundTag stash = entry.stash();
        boolean lootTable = "LOOT_TABLE".equals(stash.getString("Mode"));
        graphics.drawString(font, entry.name(), x, y, theme.ink(), false);
        if (lootTable) {
            String table = stash.getString("LootTable");
            graphics.drawString(font, Component.translatable("gui.cts.chest_library.table", table.isBlank() ? "-" : table),
                    x, y + 14, theme.ink(), false);
            graphics.drawWordWrap(font, Component.translatable("gui.cts.chest_library.table_note"), x, y + 28, this.width - x - 24, theme.muted());
        } else {
            ItemStackHandler template = new ItemStackHandler(CanStashBlockEntity.SLOT_COUNT);
            if (stash.contains("Template")) {
                template.deserializeNBT(stash.getCompound("Template"));
            }
            int[] chances = stash.getIntArray("Chances");
            int[] min = stash.getIntArray("CountMin");
            int[] max = stash.getIntArray("CountMax");
            int[] damage = stash.getIntArray("RandomDamage");
            int[] enchant = stash.getIntArray("RandomEnchant");
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    int slot = col + row * 9;
                    int sx = x + col * 18;
                    int sy = y + 16 + row * 18;
                    ThemeRenderer.slot(graphics, sx, sy);
                    ItemStack stack = template.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        graphics.renderItem(stack, sx + 1, sy + 1);
                        int chance = slot < chances.length ? chances[slot] : 100;
                        int low = slot < min.length ? min[slot] : stack.getCount();
                        int high = slot < max.length ? max[slot] : low;
                        ItemEntryEditor.drawBadge(graphics, font, sx + 1, sy + 1, chance, low, high,
                                stack.isDamageableItem(), flag(damage, slot), flag(enchant, slot));
                    }
                }
            }
        }
        int infoY = lootTable ? y + 64 : y + 76;
        graphics.drawString(font, Component.translatable("gui.cts.chest_library.respawn", stash.getInt("RespawnSeconds")),
                x, infoY, theme.muted(), false);
        graphics.drawString(font, Component.translatable("gui.cts.chest_library.search_time", stash.getInt("SearchSeconds")),
                x, infoY + 12, theme.muted(), false);
        graphics.drawString(font, Component.translatable(stash.getBoolean("RequiresTool")
                        ? "gui.cts.chest_library.tool_yes" : "gui.cts.chest_library.tool_no"),
                x, infoY + 24, theme.muted(), false);
        if (stash.contains("PerPlayer")) {
            graphics.drawString(font, Component.translatable(stash.getBoolean("PerPlayer")
                            ? "gui.cts.per_player.on" : "gui.cts.per_player.off"),
                    x, infoY + 36, theme.muted(), false);
        }
    }

    private void refreshVisible() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        visible.clear();
        for (SavedChestLibrary.Entry entry : ChestLibraryClient.entries()) {
            if (query.isEmpty()
                    || entry.name().toLowerCase(Locale.ROOT).contains(query)
                    || entry.stash().getString("LootTable").toLowerCase(Locale.ROOT).contains(query)) {
                visible.add(entry);
            }
        }
        int rows = listRows();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, visible.size() - rows)));
    }

    private int listRows() {
        return Math.max(1, (this.height - 56 - 28) / ROW);
    }

    private int rowAt(double mouseX, double mouseY) {
        int left = 16;
        int listTop = 56;
        if (mouseX < left || mouseX > left + LEFT_W - 8) {
            return -1;
        }
        int row = (int) ((mouseY - listTop) / ROW);
        if (row < 0 || row >= listRows()) {
            return -1;
        }
        int index = scroll + row;
        return index < visible.size() ? index : -1;
    }

    private SavedChestLibrary.Entry selected() {
        if (selectedId == null) {
            return null;
        }
        for (SavedChestLibrary.Entry entry : ChestLibraryClient.entries()) {
            if (entry.id().equals(selectedId)) {
                return entry;
            }
        }
        return null;
    }

    private static ItemStack icon(SavedChestLibrary.Entry entry) {
        if (!entry.mimic().isEmpty()) {
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry.mimic());
            if (state.getBlock().asItem() != Items.AIR) {
                return new ItemStack(state.getBlock());
            }
        }
        Block block = BuiltInRegistries.BLOCK.get(entry.blockId());
        if (block != null && block.asItem() != Items.AIR) {
            return new ItemStack(block);
        }
        return new ItemStack(Items.CHEST);
    }

    private static String badge(SavedChestLibrary.Entry entry) {
        CompoundTag stash = entry.stash();
        if ("LOOT_TABLE".equals(stash.getString("Mode"))) {
            String table = stash.getString("LootTable");
            return table.isBlank() ? "-" : table;
        }
        return Component.translatable("gui.cts.mode.items").getString();
    }

    private String trim(String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        String ellipsis = "...";
        while (!text.isEmpty() && font.width(text + ellipsis) > width) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }

    private static boolean flag(int[] flags, int index) {
        return flags != null && index >= 0 && index < flags.length && flags[index] != 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
