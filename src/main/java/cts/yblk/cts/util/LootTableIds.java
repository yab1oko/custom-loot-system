package cts.yblk.cts.util;

import net.minecraft.resources.ResourceLocation;
import cts.yblk.cts.Cts;

public final class LootTableIds {
    private LootTableIds() {
    }

    /** Accepts {@code airdrop} or {@code cts:airdrop}; bare names get the mod namespace. */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (!trimmed.contains(":")) {
            trimmed = Cts.MODID + ":" + trimmed;
        }
        return ResourceLocation.tryParse(trimmed) == null ? "" : trimmed;
    }
}
