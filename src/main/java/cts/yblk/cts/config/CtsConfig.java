package cts.yblk.cts.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import cts.yblk.cts.Cts;
import cts.yblk.cts.util.LootTableIds;

/**
 * Common config ({@code config/cts-common.toml}).
 * Optional Cloth Config UI edits these same values when the mod is installed.
 */
@Mod.EventBusSubscriber(modid = Cts.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CtsConfig {
    public static final ForgeConfigSpec SPEC;

    static final ForgeConfigSpec.IntValue DEFAULT_LOOT_RESPAWN_SECONDS;
    static final ForgeConfigSpec.ConfigValue<String> DEFAULT_AIRDROP_LOOT_TABLE;
    static final ForgeConfigSpec.ConfigValue<String> CREATIVE_THEME;
    static final ForgeConfigSpec.BooleanValue BUTTON_SOUNDS;
    static final ForgeConfigSpec.BooleanValue REFILL_SOUND;
    static final ForgeConfigSpec.BooleanValue AIRDROP_PLANE;

    public static final String FALLBACK_CREATIVE_THEME = "wood";

    /** Fallback before the config file is loaded (1 hour). */
    public static final int FALLBACK_LOOT_RESPAWN_SECONDS = 3600;
    public static final String FALLBACK_AIRDROP_LOOT_TABLE = Cts.MODID + ":airdrop";

    private static ModConfig commonModConfig;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("loot");
        DEFAULT_LOOT_RESPAWN_SECONDS = builder
                .comment(
                        "Default loot respawn time in seconds for new containers / loot plates.",
                        "Already placed blocks keep their saved value. 0 = no respawn.",
                        "Default: 3600 (1 hour).")
                .defineInRange("defaultRespawnSeconds", FALLBACK_LOOT_RESPAWN_SECONDS, 0, 36000);
        builder.pop();

        builder.push("airdrop");
        DEFAULT_AIRDROP_LOOT_TABLE = builder
                .comment(
                        "Default loot table used by /cts airdrop when no table is specified,",
                        "and as fallback when an airdrop entity has an empty table id.",
                        "Example: cts:airdrop")
                .define("defaultLootTable", FALLBACK_AIRDROP_LOOT_TABLE);
        AIRDROP_PLANE = builder
                .comment(
                        "When true, a cargo plane flies over and releases the parachute crate.",
                        "When false, the crate appears in the sky on its own (legacy).")
                .define("plane", true);
        builder.pop();

        builder.push("ui");
        CREATIVE_THEME = builder
                .comment(
                        "Creative screen theme: vanilla, dark, wood, iron.",
                        "Survival loot screens stay vanilla. Default: wood.")
                .define("creativeTheme", FALLBACK_CREATIVE_THEME);
        BUTTON_SOUNDS = builder
                .comment("Play a click when a CTS creative button is pressed. Default: true.")
                .define("buttonSounds", true);
        REFILL_SOUND = builder
                .comment(
                        "Play a short quiet rustle at a container when its loot actually regenerates.",
                        "Default: false, so survival chests do not give themselves away.")
                .define("refillSound", false);
        builder.pop();

        SPEC = builder.build();
    }

    private CtsConfig() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            commonModConfig = event.getConfig();
        }
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            commonModConfig = event.getConfig();
        }
    }

    public static int defaultLootRespawnSeconds() {
        if (SPEC.isLoaded()) {
            return DEFAULT_LOOT_RESPAWN_SECONDS.get();
        }
        return FALLBACK_LOOT_RESPAWN_SECONDS;
    }

    public static String defaultAirdropLootTable() {
        if (!SPEC.isLoaded()) {
            return FALLBACK_AIRDROP_LOOT_TABLE;
        }
        String normalized = LootTableIds.normalize(DEFAULT_AIRDROP_LOOT_TABLE.get());
        return normalized.isEmpty() ? FALLBACK_AIRDROP_LOOT_TABLE : normalized;
    }

    /** Persist ForgeConfigSpec values after Cloth / code edits. */
    public static void save() {
        if (commonModConfig != null) {
            commonModConfig.save();
        }
    }

    static void setDefaultLootRespawnSeconds(int seconds) {
        DEFAULT_LOOT_RESPAWN_SECONDS.set(Math.max(0, Math.min(36000, seconds)));
    }

    static void setDefaultAirdropLootTable(String table) {
        String normalized = LootTableIds.normalize(table);
        DEFAULT_AIRDROP_LOOT_TABLE.set(normalized.isEmpty() ? FALLBACK_AIRDROP_LOOT_TABLE : normalized);
    }

    public static String creativeTheme() {
        if (!SPEC.isLoaded()) {
            return FALLBACK_CREATIVE_THEME;
        }
        return sanitizeTheme(CREATIVE_THEME.get());
    }

    public static void setCreativeTheme(String id) {
        CREATIVE_THEME.set(sanitizeTheme(id));
    }

    public static boolean buttonSounds() {
        return !SPEC.isLoaded() || BUTTON_SOUNDS.get();
    }

    public static void setButtonSounds(boolean enabled) {
        BUTTON_SOUNDS.set(enabled);
    }

    public static boolean refillSound() {
        return SPEC.isLoaded() && REFILL_SOUND.get();
    }

    public static void setRefillSound(boolean enabled) {
        REFILL_SOUND.set(enabled);
    }

    public static boolean airdropPlane() {
        return !SPEC.isLoaded() || AIRDROP_PLANE.get();
    }

    public static void setAirdropPlane(boolean enabled) {
        AIRDROP_PLANE.set(enabled);
    }

    private static String sanitizeTheme(String raw) {
        if (raw == null) {
            return FALLBACK_CREATIVE_THEME;
        }
        String id = raw.trim().toLowerCase();
        return switch (id) {
            case "vanilla", "dark", "wood", "iron" -> id;
            default -> FALLBACK_CREATIVE_THEME;
        };
    }
}
