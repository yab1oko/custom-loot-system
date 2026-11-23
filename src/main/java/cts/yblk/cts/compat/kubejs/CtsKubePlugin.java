package cts.yblk.cts.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;

public class CtsKubePlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        CtsKubeEvents.GROUP.register();
    }
}
