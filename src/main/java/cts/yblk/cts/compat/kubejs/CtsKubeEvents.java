package cts.yblk.cts.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public final class CtsKubeEvents {
    public static final EventGroup GROUP = EventGroup.of("CtsEvents");
    public static final EventHandler LOOT_ROLLED = GROUP.server("lootRolled", () -> CtsLootRolledEvent.class);
    public static final EventHandler CONTAINER_OPENED = GROUP.server("containerOpened", () -> CtsContainerEvent.class);
    public static final EventHandler CONTAINER_REFILLED = GROUP.server("containerRefilled", () -> CtsContainerEvent.class);
    public static final EventHandler AIRDROP_LANDED = GROUP.server("airdropLanded", () -> CtsContainerEvent.class);

    private CtsKubeEvents() {
    }
}
