package cts.yblk.cts.client;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ConversionWandClient {
    private ConversionWandClient() {
    }

    public static void openConfirm(BlockPos pos, boolean revert) {
        ConvertConfirmScreen.open(pos, revert);
    }
}
