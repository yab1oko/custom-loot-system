package cts.yblk.cts.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import cts.yblk.cts.Cts;
import cts.yblk.cts.client.ChestLibraryClient;
import cts.yblk.cts.library.SavedChestLibrary;
import cts.yblk.cts.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

/**
 * Loaded only when JEI scans plugins. Nothing in the always-loaded CTS code references this class.
 */
@JeiPlugin
public class CtsJeiPlugin implements IModPlugin {
    public static final RecipeType<CtsLootRecipe> LOOT = RecipeType.create(Cts.MODID, "loot", CtsLootRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Cts.MODID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CtsLootCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<CtsLootRecipe> recipes = new ArrayList<>();
        for (SavedChestLibrary.Entry entry : ChestLibraryClient.entries()) {
            recipes.add(CtsLootRecipe.from(entry));
        }
        registration.addRecipes(LOOT, recipes);
        registration.addItemStackInfo(new ItemStack(ModItems.CONVERSION_WAND.get()), Component.translatable("jei.cts.conversion_wand"));
        registration.addItemStackInfo(new ItemStack(ModItems.CHEST_LIBRARY.get()), Component.translatable("jei.cts.chest_library"));
        registration.addItemStackInfo(new ItemStack(ModItems.LOOT_BOOK.get()), Component.translatable("jei.cts.loot_book"));
    }
}
