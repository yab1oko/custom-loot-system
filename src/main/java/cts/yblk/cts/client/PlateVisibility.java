package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import cts.yblk.cts.registry.ModItems;

public final class PlateVisibility {
    private PlateVisibility() {
    }

    public static boolean localPlayerHoldingPlate() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && isHoldingPlate(minecraft.player);
    }

    public static boolean isHoldingPlate(Player player) {
        if (player == null) {
            return false;
        }
        return isPlate(player.getMainHandItem()) || isPlate(player.getOffhandItem());
    }

    private static boolean isPlate(ItemStack stack) {
        return stack.is(ModItems.LOOT_PLATE.get());
    }
}
