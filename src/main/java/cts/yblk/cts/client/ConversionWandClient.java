package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import cts.yblk.cts.network.ConvertContainerPacket;
import cts.yblk.cts.network.ModNetwork;

@OnlyIn(Dist.CLIENT)
public final class ConversionWandClient {
    private ConversionWandClient() {
    }

    public static void openConfirm(BlockPos pos, boolean revert) {
        Minecraft minecraft = Minecraft.getInstance();
        Component title = Component.translatable(revert ? "gui.cts.convert_revert_title" : "gui.cts.convert_title");
        Component body = Component.translatable(revert ? "gui.cts.convert_revert_body" : "gui.cts.convert_body");
        minecraft.setScreen(new ConfirmScreen(confirmed -> {
            minecraft.setScreen(null);
            if (confirmed) {
                ModNetwork.CHANNEL.sendToServer(new ConvertContainerPacket(pos, revert));
            }
        }, title, body));
    }
}
