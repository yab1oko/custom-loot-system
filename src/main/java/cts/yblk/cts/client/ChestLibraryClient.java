package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import cts.yblk.cts.library.SavedChestLibrary;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.RequestChestLibraryPacket;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class ChestLibraryClient {
    private static final List<SavedChestLibrary.Entry> CACHE = new ArrayList<>();

    private ChestLibraryClient() {
    }

    public static void openLibrary() {
        ModNetwork.CHANNEL.sendToServer(new RequestChestLibraryPacket());
        Minecraft.getInstance().setScreen(new ChestLibraryScreen());
    }

    public static void openSavePrompt(BlockPos pos, String suggestedName) {
        Minecraft.getInstance().setScreen(new ChestLibrarySaveScreen(pos, suggestedName));
    }

    public static void applySync(List<SavedChestLibrary.Entry> entries) {
        CACHE.clear();
        CACHE.addAll(entries);
        if (Minecraft.getInstance().screen instanceof ChestLibraryScreen screen) {
            screen.onLibrarySynced();
        }
    }

    public static List<SavedChestLibrary.Entry> entries() {
        return CACHE;
    }
}
