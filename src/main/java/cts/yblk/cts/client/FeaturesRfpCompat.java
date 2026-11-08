package cts.yblk.cts.client;

import net.minecraftforge.fml.ModList;

/**
 * Soft-dep on Features Real First Person.
 * <p>
 * RFP already draws the third-person player model in FP (Player Animator poses
 * apply through that pass). CTS must not also enable
 * {@code FirstPersonMode.THIRD_PERSON_MODEL}, or arms/items double up.
 */
public final class FeaturesRfpCompat {
    private static final String FEATURES = "features";
    private static Boolean present;
    private static int apiState; // 0 unknown, 1 ok, -1 missing
    private static java.lang.reflect.Field enabledField;

    private FeaturesRfpCompat() {
    }

    public static boolean isFeaturesLoaded() {
        if (present == null) {
            present = ModList.get().isLoaded(FEATURES);
        }
        return present;
    }

    /** True when Features Real First Person is toggled on. */
    public static boolean isRealFirstPersonActive() {
        if (!isFeaturesLoaded()) {
            return false;
        }
        ensureReflect();
        if (apiState != 1 || enabledField == null) {
            return false;
        }
        try {
            return enabledField.getBoolean(null);
        } catch (Throwable t) {
            apiState = -1;
            enabledField = null;
            return false;
        }
    }

    private static void ensureReflect() {
        if (apiState != 0) {
            return;
        }
        try {
            Class<?> cfg = Class.forName("yblk.features.config.FeaturesConfig");
            enabledField = cfg.getField("realFirstPersonEnabled");
            apiState = 1;
        } catch (Throwable t) {
            enabledField = null;
            apiState = -1;
        }
    }
}
