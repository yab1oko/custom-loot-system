package cts.yblk.cts.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import cts.yblk.cts.blockentity.SpawnChance;
import cts.yblk.cts.item.TagSlipItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

public class ItemEntryEditor {
    private static final int WIDTH = 160;
    private static final int PICK_COLS = 9;
    private static final int PICK_ROWS = 4;
    private static final int PICK_SLOT = 18;

    private int left;
    private int top;
    private int height = 104;
    private int slot = -1;
    private boolean damageable;
    private boolean enchantable;
    private boolean tagSlip;
    private boolean randomDamage;
    private boolean randomEnchant;
    private AbstractSliderButton minSlider;
    private AbstractSliderButton maxSlider;
    private AbstractSliderButton chanceSlider;
    private Button durabilityButton;
    private Button enchantButton;
    private Button doneButton;
    private EditBox tagBox;
    private LootTableSuggestions tagSuggestions;
    private final List<ItemStack> pickItems = new ArrayList<>();
    private final Set<String> picked = new HashSet<>();
    private final Set<String> savedPick = new HashSet<>();
    private String savedTag = "";
    private boolean savedAll = true;
    private String shownTag = "";
    private int pickScroll;
    private int pickLeft;
    private int pickTop;
    private int pickCols = PICK_COLS;
    private ItemStack pickHover = ItemStack.EMPTY;
    private Consumer<Result> onSave;
    private AbstractSliderButton dragWidget;

    public boolean isOpen() {
        return slot >= 0;
    }

    public int slot() {
        return slot;
    }

    public void open(int slot, int min, int max, int chance, boolean damageable, boolean randomDamage, boolean randomEnchant,
                     boolean tagSlip, String tag, String pick, ItemStack preview, Font font, int panelLeft, int panelTop, Consumer<Result> onSave) {
        this.slot = slot;
        this.left = panelLeft;
        this.top = panelTop;
        this.tagSlip = tagSlip;
        this.damageable = damageable && !tagSlip;
        this.enchantable = SpawnChance.canRandomEnchant(preview == null ? ItemStack.EMPTY : preview);
        this.randomDamage = randomDamage;
        this.randomEnchant = randomEnchant && this.enchantable;
        this.onSave = onSave;
        int row = top + 18;
        if (this.damageable) {
            minSlider = null;
            maxSlider = null;
            durabilityButton = Button.builder(durabilityLabel(), button -> toggleDurability())
                    .bounds(left + 8, row, WIDTH - 16, 18)
                    .build();
            row += 22;
        } else {
            durabilityButton = null;
            int low = Math.max(1, Math.min(min, max));
            int high = Math.max(low, Math.min(64, Math.max(min, max)));
            minSlider = new CountSlider(left + 8, row, WIDTH - 16, "gui.cts.min", low);
            row += 22;
            maxSlider = new CountSlider(left + 8, row, WIDTH - 16, "gui.cts.max", high);
            row += 22;
        }
        if (this.enchantable) {
            enchantButton = Button.builder(enchantLabel(), button -> toggleEnchant())
                    .bounds(left + 8, row, WIDTH - 16, 18)
                    .build();
            row += 22;
        } else {
            enchantButton = null;
        }
        if (tagSlip) {
            tagBox = new EditBox(font, left + 8, row + 10, WIDTH - 16, 16, Component.translatable("gui.cts.tag"));
            tagBox.setMaxLength(128);
            tagBox.setValue(tag == null ? "" : tag);
            tagSuggestions = new LootTableSuggestions(() -> 0, TagIndex::ids);
            tagSuggestions.attach(tagBox);
            savedTag = TagSlipItem.sanitize(tag);
            savedAll = pick == null || pick.isBlank() || TagSlipItem.ALL.equals(pick);
            savedPick.clear();
            if (!savedAll) {
                for (String part : pick.split(",")) {
                    String id = part.trim();
                    if (!id.isEmpty()) {
                        savedPick.add(id);
                    }
                }
            }
            shownTag = "\0";
            pickItems.clear();
            picked.clear();
            pickScroll = 0;
            tagBox.setFocused(true);
            tagSuggestions.reveal();
            row += 32;
        } else {
            tagBox = null;
            tagSuggestions = null;
        }
        chanceSlider = new ChanceSlider(left + 8, row, WIDTH - 16, chance);
        row += 22;
        doneButton = Button.builder(Component.translatable("gui.cts.done"), button -> save())
                .bounds(left + 8, row, WIDTH - 16, 18)
                .build();
        this.height = row + 26 - top;
    }

    private void toggleDurability() {
        randomDamage = !randomDamage;
        durabilityButton.setMessage(durabilityLabel());
    }

    private void toggleEnchant() {
        randomEnchant = !randomEnchant;
        enchantButton.setMessage(enchantLabel());
    }

    private Component durabilityLabel() {
        return Component.translatable(randomDamage
                ? "gui.cts.random_durability.on"
                : "gui.cts.random_durability.off");
    }

    private Component enchantLabel() {
        return Component.translatable(randomEnchant
                ? "gui.cts.random_enchant.on"
                : "gui.cts.random_enchant.off");
    }

    public void save() {
        if (!isOpen() || onSave == null) {
            close();
            return;
        }
        Consumer<Result> callback = onSave;
        int min = minSlider == null ? 1 : minSlider();
        int max = maxSlider == null ? 1 : maxSlider();
        syncPick();
        String tag = tagBox == null ? "" : tagBox.getValue();
        Result result = new Result(slot, min, max, chanceSlider(), randomDamage, randomEnchant, tag, pickToken());
        close();
        callback.accept(result);
    }

    public void close() {
        slot = -1;
        damageable = false;
        enchantable = false;
        tagSlip = false;
        randomDamage = false;
        randomEnchant = false;
        minSlider = null;
        maxSlider = null;
        chanceSlider = null;
        durabilityButton = null;
        enchantButton = null;
        doneButton = null;
        if (tagSuggestions != null) {
            tagSuggestions.hide();
        }
        tagBox = null;
        tagSuggestions = null;
        pickItems.clear();
        picked.clear();
        savedPick.clear();
        shownTag = "";
        pickHover = ItemStack.EMPTY;
        onSave = null;
        dragWidget = null;
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        if (!isOpen()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        RenderSystem.disableDepthTest();
        graphics.fill(left, top, left + WIDTH, top + height, 0xFF161616);
        graphics.fill(left, top, left + WIDTH, top + 1, 0xFF6A6A6A);
        graphics.fill(left, top + height - 1, left + WIDTH, top + height, 0xFF6A6A6A);
        graphics.fill(left, top, left + 1, top + height, 0xFF6A6A6A);
        graphics.fill(left + WIDTH - 1, top, left + WIDTH, top + height, 0xFF6A6A6A);
        graphics.drawString(font, Component.translatable("gui.cts.item_entry"), left + 8, top + 6, 0xFFFFFF, false);
        if (damageable) {
            durabilityButton.render(graphics, mouseX, mouseY, 0);
        } else {
            minSlider.render(graphics, mouseX, mouseY, 0);
            maxSlider.render(graphics, mouseX, mouseY, 0);
        }
        if (enchantButton != null) {
            enchantButton.render(graphics, mouseX, mouseY, 0);
        }
        if (tagSlip && tagBox != null) {
            graphics.drawString(font, Component.translatable("gui.cts.tag"), tagBox.getX(), tagBox.getY() - 10, 0xA0A0A0, false);
            tagBox.render(graphics, mouseX, mouseY, 0);
        }
        chanceSlider.render(graphics, mouseX, mouseY, 0);
        doneButton.render(graphics, mouseX, mouseY, 0);
        RenderSystem.enableDepthTest();
        graphics.pose().popPose();
        if (tagSlip) {
            syncPick();
            renderPick(graphics, font, mouseX, mouseY);
        }
        if (tagSuggestions != null) {
            tagSuggestions.render(graphics, mouseX, mouseY);
        }
        if (!pickHover.isEmpty()) {
            graphics.renderTooltip(font, pickHover, mouseX, mouseY);
            pickHover = ItemStack.EMPTY;
        }
    }

    public static void drawEntries(GuiGraphics graphics, Font font, int guiLeft, int guiTop, List<Slot> menuSlots, int slots,
                                   IntUnaryOperator chance, IntUnaryOperator min, IntUnaryOperator max, IntPredicate filled,
                                   IntPredicate damageable, IntPredicate randomDamage) {
        drawEntries(graphics, font, guiLeft, guiTop, menuSlots, slots, chance, min, max, filled, damageable, randomDamage, i -> false);
    }

    public static void drawEntries(GuiGraphics graphics, Font font, int guiLeft, int guiTop, List<Slot> menuSlots, int slots,
                                   IntUnaryOperator chance, IntUnaryOperator min, IntUnaryOperator max, IntPredicate filled,
                                   IntPredicate damageable, IntPredicate randomDamage, IntPredicate randomEnchant) {
        for (int i = 0; i < slots; i++) {
            if (!filled.test(i)) {
                continue;
            }
            Slot slot = menuSlots.get(i);
            int x = guiLeft + slot.x;
            int y = guiTop + slot.y;
            int value = chance.applyAsInt(i);
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
            graphics.pose().scale(0.5F, 0.5F, 1);
            String topMark = value + "%";
            if (randomEnchant.test(i)) {
                topMark = Component.translatable("gui.cts.enchant_mark").getString() + " " + topMark;
            }
            graphics.drawString(font, topMark, (x + 1) * 2, (y + 1) * 2, 0xFFFFFF, true);
            String mark;
            if (damageable.test(i)) {
                mark = randomDamage.test(i) ? Component.translatable("gui.cts.random_mark").getString() : "";
            } else {
                int low = min.applyAsInt(i);
                int high = max.applyAsInt(i);
                mark = low == high ? Integer.toString(low) : low + "-" + high;
            }
            if (!mark.isEmpty()) {
                int width = font.width(mark);
                graphics.drawString(font, mark, (x + 16) * 2 - width, (y + 11) * 2, 0xFFFFFF, true);
            }
            graphics.pose().popPose();
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isOpen() || button != 0) {
            return isOpen();
        }
        if (tagSuggestions != null && tagSuggestions.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (pickClicked(mouseX, mouseY)) {
            return true;
        }
        if (tagBox != null && tagBox.isMouseOver(mouseX, mouseY)) {
            tagBox.setFocused(true);
            tagBox.mouseClicked(mouseX, mouseY, button);
            if (tagSuggestions != null) {
                tagSuggestions.reveal();
            }
            return true;
        }
        if (tagBox != null) {
            tagBox.setFocused(false);
        }
        if (doneButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (durabilityButton != null && durabilityButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (enchantButton != null && enchantButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        dragWidget = null;
        if (minSlider != null && minSlider.mouseClicked(mouseX, mouseY, button)) {
            dragWidget = minSlider;
            keepOrder();
            return true;
        }
        if (maxSlider != null && maxSlider.mouseClicked(mouseX, mouseY, button)) {
            dragWidget = maxSlider;
            keepOrder();
            return true;
        }
        if (chanceSlider.mouseClicked(mouseX, mouseY, button)) {
            dragWidget = chanceSlider;
            return true;
        }
        return contains(mouseX, mouseY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isOpen() || dragWidget == null) {
            return isOpen();
        }
        boolean handled = dragWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        keepOrder();
        return handled;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isOpen()) {
            return false;
        }
        if (dragWidget != null) {
            dragWidget.mouseReleased(mouseX, mouseY, button);
            dragWidget = null;
        }
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isOpen()) {
            return false;
        }
        if (tagSuggestions != null && tagSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (tagBox != null && tagBox.isFocused() && tagBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return tagBox != null && tagBox.isFocused()
                && Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return isOpen() && tagBox != null && tagBox.isFocused() && tagBox.charTyped(codePoint, modifiers);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isOpen()) {
            return false;
        }
        if (scrollPick(delta, mouseX, mouseY)) {
            return true;
        }
        return tagSuggestions != null && tagSuggestions.mouseScrolled(mouseX, mouseY, delta);
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= left && mouseX < left + WIDTH && mouseY >= top && mouseY < top + height;
    }

    private void syncPick() {
        if (tagBox == null) {
            return;
        }
        String id = TagSlipItem.sanitize(tagBox.getValue());
        if (id.equals(shownTag)) {
            return;
        }
        shownTag = id;
        pickItems.clear();
        picked.clear();
        pickScroll = 0;
        if (id.isEmpty()) {
            return;
        }
        Set<String> seen = new HashSet<>();
        for (Item item : TagSlipItem.members(id)) {
            String key = BuiltInRegistries.ITEM.getKey(item).toString();
            if (seen.add(key)) {
                pickItems.add(new ItemStack(item));
            }
        }
        if (id.equals(savedTag) && !savedAll) {
            for (ItemStack stack : pickItems) {
                String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (savedPick.contains(key)) {
                    picked.add(key);
                }
            }
        } else {
            for (ItemStack stack : pickItems) {
                picked.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            }
        }
    }

    private String pickToken() {
        if (pickItems.isEmpty()) {
            return savedAll ? TagSlipItem.ALL : String.join(",", savedPick);
        }
        if (picked.size() >= pickItems.size()) {
            return TagSlipItem.ALL;
        }
        return String.join(",", picked);
    }

    private void layoutPick() {
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        pickCols = PICK_COLS;
        int need = 8 + pickCols * PICK_SLOT;
        int gapRight = screenW - (left + WIDTH + 4);
        int gapLeft = left - 4;
        if (gapRight >= need) {
            pickLeft = left + WIDTH + 4;
        } else if (gapLeft >= need) {
            pickLeft = left - 4 - need;
        } else {
            int room = Math.max(gapRight, gapLeft);
            pickCols = Math.max(4, (room - 8) / PICK_SLOT);
            need = 8 + pickCols * PICK_SLOT;
            if (gapRight >= gapLeft) {
                pickLeft = left + WIDTH + 4;
            } else {
                pickLeft = Math.max(2, left - 4 - need);
            }
        }
        pickTop = top;
    }

    private int pickWidth() {
        return 8 + pickCols * PICK_SLOT;
    }

    private int pickHeight() {
        return 16 + PICK_ROWS * PICK_SLOT + 22;
    }

    private boolean pickOpen() {
        return tagSlip && !pickItems.isEmpty();
    }

    private boolean pickContains(double mouseX, double mouseY) {
        return pickOpen()
                && mouseX >= pickLeft && mouseX < pickLeft + pickWidth()
                && mouseY >= pickTop && mouseY < pickTop + pickHeight();
    }

    private void renderPick(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        pickHover = ItemStack.EMPTY;
        if (!pickOpen()) {
            return;
        }
        layoutPick();
        int width = pickWidth();
        int height = pickHeight();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 420);
        RenderSystem.disableDepthTest();
        graphics.fill(pickLeft, pickTop, pickLeft + width, pickTop + height, 0xFF161616);
        graphics.fill(pickLeft, pickTop, pickLeft + width, pickTop + 1, 0xFF6A6A6A);
        graphics.fill(pickLeft, pickTop + height - 1, pickLeft + width, pickTop + height, 0xFF6A6A6A);
        graphics.fill(pickLeft, pickTop, pickLeft + 1, pickTop + height, 0xFF6A6A6A);
        graphics.fill(pickLeft + width - 1, pickTop, pickLeft + width, pickTop + height, 0xFF6A6A6A);
        String count = picked.size() + "/" + pickItems.size();
        int countWidth = font.width(count);
        graphics.drawString(font, count, pickLeft + width - 6 - countWidth, pickTop + 4, 0xFFFFFF, false);
        graphics.drawString(font, font.plainSubstrByWidth(shownTag, width - 12 - countWidth), pickLeft + 4, pickTop + 4, 0xA0A0A0, false);
        int start = pickScroll * pickCols;
        ItemStack hovered = ItemStack.EMPTY;
        for (int row = 0; row < PICK_ROWS; row++) {
            for (int col = 0; col < pickCols; col++) {
                int index = start + row * pickCols + col;
                if (index >= pickItems.size()) {
                    break;
                }
                int x = pickLeft + 4 + col * PICK_SLOT;
                int y = pickTop + 16 + row * PICK_SLOT;
                ItemStack stack = pickItems.get(index);
                String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                boolean on = picked.contains(key);
                graphics.fill(x, y, x + 16, y + 16, on ? 0xFF3A3A3A : 0xFF1A1A1A);
                RenderSystem.enableDepthTest();
                graphics.renderItem(stack, x, y);
                RenderSystem.disableDepthTest();
                if (!on) {
                    graphics.fill(x, y, x + 16, y + 16, 0xB0000000);
                } else {
                    graphics.fill(x, y, x + 16, y + 1, 0xFFFFFFFF);
                    graphics.fill(x, y + 15, x + 16, y + 16, 0xFFFFFFFF);
                    graphics.fill(x, y, x + 1, y + 16, 0xFFFFFFFF);
                    graphics.fill(x + 15, y, x + 16, y + 16, 0xFFFFFFFF);
                }
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    hovered = stack;
                }
            }
        }
        int buttonY = pickTop + 16 + PICK_ROWS * PICK_SLOT + 2;
        int buttonW = (width - 12) / 2;
        drawPickButton(graphics, font, pickLeft + 4, buttonY, buttonW, Component.translatable("gui.cts.tag_all"), mouseX, mouseY);
        drawPickButton(graphics, font, pickLeft + 8 + buttonW, buttonY, buttonW, Component.translatable("gui.cts.tag_none"), mouseX, mouseY);
        RenderSystem.enableDepthTest();
        graphics.pose().popPose();
        pickHover = hovered;
    }

    private void drawPickButton(GuiGraphics graphics, Font font, int x, int y, int width, Component label, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 16;
        graphics.fill(x, y, x + width, y + 16, hover ? 0xFF4A4A4A : 0xFF2A2A2A);
        graphics.drawCenteredString(font, label, x + width / 2, y + 4, 0xFFFFFF);
    }

    private boolean pickClicked(double mouseX, double mouseY) {
        if (!pickContains(mouseX, mouseY)) {
            return false;
        }
        int buttonY = pickTop + 16 + PICK_ROWS * PICK_SLOT + 2;
        int buttonW = (pickWidth() - 12) / 2;
        if (mouseY >= buttonY && mouseY < buttonY + 16) {
            if (mouseX >= pickLeft + 4 && mouseX < pickLeft + 4 + buttonW) {
                for (ItemStack stack : pickItems) {
                    picked.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                }
            } else if (mouseX >= pickLeft + 8 + buttonW && mouseX < pickLeft + 8 + buttonW * 2) {
                picked.clear();
            }
            return true;
        }
        int col = (int) ((mouseX - (pickLeft + 4)) / PICK_SLOT);
        int row = (int) ((mouseY - (pickTop + 16)) / PICK_SLOT);
        if (col < 0 || col >= pickCols || row < 0 || row >= PICK_ROWS) {
            return true;
        }
        int index = pickScroll * pickCols + row * pickCols + col;
        if (index < 0 || index >= pickItems.size()) {
            return true;
        }
        String key = BuiltInRegistries.ITEM.getKey(pickItems.get(index).getItem()).toString();
        if (!picked.add(key)) {
            picked.remove(key);
        }
        return true;
    }

    private boolean scrollPick(double delta, double mouseX, double mouseY) {
        if (!pickContains(mouseX, mouseY)) {
            return false;
        }
        int rows = (pickItems.size() + pickCols - 1) / pickCols;
        int max = Math.max(0, rows - PICK_ROWS);
        pickScroll = Mth.clamp(pickScroll - (int) Math.signum(delta), 0, max);
        return true;
    }

    private void keepOrder() {
        if (minSlider == null || maxSlider == null) {
            return;
        }
        CountSlider min = (CountSlider) minSlider;
        CountSlider max = (CountSlider) maxSlider;
        if (min.count() <= max.count()) {
            return;
        }
        if (dragWidget == minSlider) {
            max.setCount(min.count());
        } else if (dragWidget == maxSlider) {
            min.setCount(max.count());
        }
    }

    private int minSlider() {
        return ((CountSlider) minSlider).count();
    }

    private int maxSlider() {
        return ((CountSlider) maxSlider).count();
    }

    private int chanceSlider() {
        return ((ChanceSlider) chanceSlider).chance();
    }

    public record Result(int slot, int min, int max, int chance, boolean randomDamage, boolean randomEnchant, String tag, String pick) {
    }

    private static final class CountSlider extends AbstractSliderButton {
        private final String label;

        private CountSlider(int x, int y, int width, String label, int count) {
            super(x, y, width, 18, Component.empty(), (Math.max(1, Math.min(64, count)) - 1) / 63.0D);
            this.label = label;
            updateMessage();
        }

        private int count() {
            return (int) Math.round(value * 63) + 1;
        }

        private void setCount(int count) {
            value = (Math.max(1, Math.min(64, count)) - 1) / 63.0D;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(label, count()));
        }

        @Override
        protected void applyValue() {
        }
    }

    private static final class ChanceSlider extends AbstractSliderButton {
        private ChanceSlider(int x, int y, int width, int chance) {
            super(x, y, width, 18, Component.empty(), Math.max(0, Math.min(100, chance)) / 100.0D);
            updateMessage();
        }

        private int chance() {
            return (int) Math.round(value * 100);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(chance() + "%"));
        }

        @Override
        protected void applyValue() {
        }
    }
}
