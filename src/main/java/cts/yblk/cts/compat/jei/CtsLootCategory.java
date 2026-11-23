package cts.yblk.cts.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import cts.yblk.cts.client.ItemEntryEditor;
import cts.yblk.cts.registry.ModItems;

final class CtsLootCategory implements IRecipeCategory<CtsLootRecipe> {
    private static final int COLS = 9;
    private static final int SLOT = 18;
    private final IDrawable background;
    private final IDrawable icon;

    CtsLootCategory(IGuiHelper gui) {
        this.background = gui.createBlankDrawable(COLS * SLOT, 3 * SLOT + 36);
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.CHEST_LIBRARY.get()));
    }

    @Override
    public RecipeType<CtsLootRecipe> getRecipeType() {
        return CtsJeiPlugin.LOOT;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.cts.loot");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CtsLootRecipe recipe, IFocusGroup focuses) {
        if (recipe.lootTable) {
            return;
        }
        for (int slot = 0; slot < recipe.stacks.length; slot++) {
            if (recipe.stacks[slot].isEmpty()) {
                continue;
            }
            int col = slot % COLS;
            int row = slot / COLS;
            int chance = recipe.chance[slot];
            int low = recipe.min[slot];
            int high = recipe.max[slot];
            builder.addSlot(RecipeIngredientRole.OUTPUT, col * SLOT, row * SLOT)
                    .addItemStack(recipe.stacks[slot])
                    .setStandardSlotBackground()
                    .addTooltipCallback((view, tooltip) -> tooltip.add(Component.literal(chance + "%  " + (low == high ? low : low + "-" + high))));
        }
    }

    @Override
    public void draw(CtsLootRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        if (recipe.lootTable) {
            String table = recipe.tableId == null || recipe.tableId.isBlank() ? "-" : recipe.tableId;
            graphics.drawString(font, Component.translatable("gui.cts.chest_library.table", table), 4, 4, 0xFF404040, false);
            graphics.drawWordWrap(font, Component.translatable("gui.cts.chest_library.table_note"), 4, 18, COLS * SLOT - 8, 0xFF6B6256);
            return;
        }
        graphics.drawString(font, recipe.name, 2, 3 * SLOT + 4, 0xFF404040, false);
        for (int slot = 0; slot < recipe.stacks.length; slot++) {
            if (recipe.stacks[slot].isEmpty()) {
                continue;
            }
            int x = (slot % COLS) * SLOT + 1;
            int y = (slot / COLS) * SLOT + 1;
            ItemEntryEditor.drawBadge(graphics, font, x, y, recipe.chance[slot], recipe.min[slot], recipe.max[slot],
                    recipe.stacks[slot].isDamageableItem(), recipe.damage[slot], recipe.enchant[slot]);
        }
    }
}
