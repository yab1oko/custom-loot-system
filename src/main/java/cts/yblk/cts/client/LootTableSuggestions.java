package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.Mth;
import cts.yblk.cts.item.LootTableIndex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class LootTableSuggestions {
    private static final int ROW = 12;
    private static final int VISIBLE = 8;
    private static final int FILL = 0xD0000000;
    private static final int SELECTED = 0xFFFFFF00;
    private static final int PLAIN = 0xFFAAAAAA;

    private EditBox box;
    private final List<String> matches = new ArrayList<>();
    private int selected;
    private int offset;
    private String lastQuery = "";
    private int seenGeneration = -1;
    private boolean open;
    private boolean forced;
    private boolean filling;
    private int listX;
    private int listY;
    private int listW;
    private int listH;
    private final IntSupplier generationSource;
    private final Supplier<List<String>> idSource;

    public LootTableSuggestions() {
        this(LootTableIndex::generation, LootTableIndex::ids);
    }

    public LootTableSuggestions(IntSupplier generationSource, Supplier<List<String>> idSource) {
        this.generationSource = generationSource;
        this.idSource = idSource;
    }

    public void attach(EditBox box) {
        this.box = box;
        box.setResponder(value -> refresh());
    }

    public void reveal() {
        forced = true;
        refresh();
    }

    public void hide() {
        open = false;
        forced = false;
        if (box != null) {
            box.setSuggestion(null);
        }
    }

    public void refresh() {
        if (filling || box == null) {
            return;
        }
        seenGeneration = generationSource.getAsInt();
        String value = box.getValue();
        String query = value.toLowerCase(Locale.ROOT);
        matches.clear();
        if (forced || !query.isEmpty()) {
            for (String id : idSource.get()) {
                if (matches(id, query)) {
                    matches.add(id);
                }
            }
            matches.sort(Comparator.comparingInt((String id) -> rank(id, query)).thenComparing(String::compareTo));
        }
        if (!query.equals(lastQuery)) {
            selected = 0;
            offset = 0;
            lastQuery = query;
        }
        if (selected >= matches.size()) {
            selected = 0;
        }
        int maxOffset = Math.max(matches.size() - VISIBLE, 0);
        offset = Mth.clamp(offset, 0, maxOffset);
        boolean exact = matches.size() == 1 && matches.get(0).equals(value);
        open = box.isVisible() && box.isFocused() && !matches.isEmpty() && !exact && (forced || !query.isEmpty());
        updateGhost();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (box == null || !box.isVisible() || !box.isFocused()) {
            return false;
        }
        if (open && keyCode == 256) {
            hide();
            return true;
        }
        if (open && keyCode == 265) {
            move(-1);
            return true;
        }
        if (open && keyCode == 264) {
            move(1);
            return true;
        }
        if (keyCode == 258) {
            if (open) {
                apply();
                return true;
            }
            forced = true;
            refresh();
            if (matches.size() == 1) {
                apply();
            }
            return true;
        }
        if (open && (keyCode == 257 || keyCode == 335)) {
            apply();
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open || button != 0) {
            return false;
        }
        layout();
        if (!inside(mouseX, mouseY)) {
            return false;
        }
        int row = (int) ((mouseY - listY) / ROW) + offset;
        if (row >= 0 && row < matches.size()) {
            selected = row;
            apply();
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!open) {
            return false;
        }
        layout();
        if (!inside(mouseX, mouseY)) {
            return false;
        }
        offset = Mth.clamp(offset - (int) Math.signum(delta), 0, Math.max(matches.size() - VISIBLE, 0));
        return true;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (box == null || !box.isVisible() || !box.isFocused()) {
            if (open) {
                hide();
            }
            return;
        }
        if (seenGeneration != generationSource.getAsInt()) {
            refresh();
        }
        if (!open || matches.isEmpty()) {
            return;
        }
        layout();
        if (inside(mouseX, mouseY)) {
            int row = (int) ((mouseY - listY) / ROW) + offset;
            if (row >= offset && row < matches.size() && row < offset + VISIBLE && row != selected) {
                selected = row;
                updateGhost();
            }
        }
        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        int rows = Math.min(matches.size() - offset, VISIBLE);
        for (int row = 0; row < rows; row++) {
            int index = offset + row;
            int y = listY + row * ROW;
            graphics.fill(listX, y, listX + listW, y + ROW, FILL);
            graphics.drawString(font, matches.get(index), listX + 2, y + 2, index == selected ? SELECTED : PLAIN, false);
        }
        graphics.pose().popPose();
    }

    private void apply() {
        if (matches.isEmpty() || box == null) {
            return;
        }
        String id = matches.get(Math.min(selected, matches.size() - 1));
        filling = true;
        box.setValue(id);
        filling = false;
        hide();
    }

    private void move(int direction) {
        if (matches.isEmpty()) {
            return;
        }
        selected = (selected + direction + matches.size()) % matches.size();
        if (selected < offset) {
            offset = selected;
        } else if (selected >= offset + VISIBLE) {
            offset = selected - VISIBLE + 1;
        }
        updateGhost();
    }

    private void updateGhost() {
        if (box == null) {
            return;
        }
        if (!box.isFocused() || matches.isEmpty()) {
            box.setSuggestion(null);
            return;
        }
        String chosen = matches.get(Math.min(selected, matches.size() - 1));
        String value = box.getValue();
        if (!value.isEmpty() && chosen.regionMatches(true, 0, value, 0, value.length())) {
            box.setSuggestion(chosen.substring(value.length()));
        } else {
            box.setSuggestion(null);
        }
    }

    private void layout() {
        Font font = Minecraft.getInstance().font;
        listW = box.getWidth();
        int rows = Math.min(matches.size(), VISIBLE);
        for (int i = 0; i < rows; i++) {
            listW = Math.max(listW, font.width(matches.get(offset + i)) + 6);
        }
        listH = rows * ROW;
        listX = box.getX();
        listY = box.getY() + box.getHeight() + 1;
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (listY + listH > screenHeight - 4) {
            listY = Math.max(2, box.getY() - listH - 1);
        }
    }

    private boolean inside(double mouseX, double mouseY) {
        return mouseX >= listX && mouseY >= listY && mouseX < listX + listW && mouseY < listY + listH;
    }

    private static boolean matches(String id, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String lower = id.toLowerCase(Locale.ROOT);
        int colon = lower.indexOf(':');
        String path = colon >= 0 ? lower.substring(colon + 1) : lower;
        return lower.contains(query) || path.contains(query);
    }

    private static int rank(String id, String query) {
        if (query.isEmpty()) {
            return 2;
        }
        String lower = id.toLowerCase(Locale.ROOT);
        if (lower.startsWith(query)) {
            return 0;
        }
        int colon = lower.indexOf(':');
        String path = colon >= 0 ? lower.substring(colon + 1) : lower;
        return path.startsWith(query) ? 1 : 2;
    }
}
