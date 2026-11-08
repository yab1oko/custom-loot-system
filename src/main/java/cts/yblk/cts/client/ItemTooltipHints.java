package cts.yblk.cts.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.registry.ModItems;

@Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
public class ItemTooltipHints {
    private ItemTooltipHints() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (item == ModItems.LOOT_PLATE.get() || item == ModItems.ENTITY_PLATE.get()) {
            event.getToolTip().add(Component.translatable("item.cts.tooltip.shift_configure")
                    .withStyle(ChatFormatting.GRAY));
        } else if (item == ModItems.BARRICADE.get()) {
            event.getToolTip().add(Component.translatable("item.cts.barricade.tooltip")
                    .withStyle(ChatFormatting.GRAY));
        } else if (item == ModItems.VENTILATION_GRILLE.get()) {
            event.getToolTip().add(Component.translatable("item.cts.ventilation_grille.tooltip")
                    .withStyle(ChatFormatting.GRAY));
        } else if (item == ModItems.METAL_ORE.get()) {
            event.getToolTip().add(Component.translatable("item.cts.metal_ore.tooltip")
                    .withStyle(ChatFormatting.GRAY));
        } else if (item == ModItems.HARVEST_TREE.get() || item == ModItems.HARVEST_TREE_LARGE.get()) {
            event.getToolTip().add(Component.translatable("item.cts.harvest_tree.tooltip")
                    .withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.translatable("item.cts.harvest_tree.tooltip_caution")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.cts.harvest_tree.tooltip_config")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
