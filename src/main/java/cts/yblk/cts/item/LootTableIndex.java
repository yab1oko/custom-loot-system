package cts.yblk.cts.item;

import java.util.List;

public final class LootTableIndex {
    private static volatile List<String> ids = List.of();
    private static volatile int generation = -1;

    private LootTableIndex() {
    }

    public static void replace(List<String> next) {
        ids = List.copyOf(next);
        generation++;
    }

    public static List<String> ids() {
        return ids;
    }

    public static int generation() {
        return generation;
    }
}
