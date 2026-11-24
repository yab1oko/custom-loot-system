package cts.yblk.cts.client.ui;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import cts.yblk.cts.Cts;

/**
 * Creative-screen themes. Survival chests stay vanilla.
 */
@OnlyIn(Dist.CLIENT)
public enum CtsTheme {
    VANILLA("vanilla", "config.cts.ui.theme.vanilla", "config.cts.ui.theme.vanilla.desc",
            0xFFC6C6C6, 0xFFFFFFFF, 0xFF373737, 0xFF8B8B8B,
            0xFF404040, 0xFF555555, 0xFF404040,
            0xFF8B8B8B, 0xFFA0A0A0, 0xFF6E6E6E, 0xFF555555,
            0xFFB4B4B4, 0xFF8B8B8B, 0xFF373737,
            false, false),
    DARK("dark", "config.cts.ui.theme.dark", "config.cts.ui.theme.dark.desc",
            0xFF161616, 0xFF6A6A6A, 0xFF6A6A6A, 0xFF1A1A1A,
            0xFFFFFFFF, 0xFFA0A0A0, 0xFFFFFFFF,
            0xFF2A2A2A, 0xFF3A3A3A, 0xFF1E1E1E, 0xFF141414,
            0xFF242424, 0xFF3A3A3A, 0xFF6A6A6A,
            false, false),
    WOOD("wood", "config.cts.ui.theme.wood", "config.cts.ui.theme.wood.desc",
            0xFF8B5A2B, 0xFFC4A574, 0xFF5C3A1E, 0xFFE2D2AA,
            0xFF3E2812, 0xFF6B4A2A, 0xFF3E2812,
            0xFFE8D7B0, 0xFFF3E6C8, 0xFFD4C094, 0xFFCDB892,
            0xFFD8C498, 0xFF8A3A2A, 0xFF9A9A9A,
            true, true),
    IRON("iron", "config.cts.ui.theme.iron", "config.cts.ui.theme.iron.desc",
            0xFF4E555C, 0xFF8A929A, 0xFF2A2E32, 0xFF2A2E32,
            0xFFD0D4D8, 0xFF8E969E, 0xFFE6E8EA,
            0xFF3A4046, 0xFF5A636C, 0xFF2C3238, 0xFF2A2E32,
            0xFF3C444C, 0xFFC45A4A, 0xFF1C1F22,
            false, true);

    private final String id;
    private final String nameKey;
    private final String descriptionKey;
    private final int panel;
    private final int bevelLight;
    private final int bevelDark;
    private final int inset;
    private final int ink;
    private final int muted;
    private final int buttonText;
    private final int button;
    private final int buttonHover;
    private final int buttonPressed;
    private final int buttonDisabled;
    private final int rowSelected;
    private final int danger;
    private final int accent;
    private final boolean ledger;
    private final boolean rivets;

    CtsTheme(String id, String nameKey, String descriptionKey,
             int panel, int bevelLight, int bevelDark, int inset,
             int ink, int muted, int buttonText,
             int button, int buttonHover, int buttonPressed, int buttonDisabled,
             int rowSelected, int danger, int accent,
             boolean ledger, boolean rivets) {
        this.id = id;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.panel = panel;
        this.bevelLight = bevelLight;
        this.bevelDark = bevelDark;
        this.inset = inset;
        this.ink = ink;
        this.muted = muted;
        this.buttonText = buttonText;
        this.button = button;
        this.buttonHover = buttonHover;
        this.buttonPressed = buttonPressed;
        this.buttonDisabled = buttonDisabled;
        this.rowSelected = rowSelected;
        this.danger = danger;
        this.accent = accent;
        this.ledger = ledger;
        this.rivets = rivets;
    }

    public String id() {
        return id;
    }

    public String nameKey() {
        return nameKey;
    }

    public String descriptionKey() {
        return descriptionKey;
    }

    public int panel() {
        return panel;
    }

    public int bevelLight() {
        return bevelLight;
    }

    public int bevelDark() {
        return bevelDark;
    }

    public int inset() {
        return inset;
    }

    public int ink() {
        return ink;
    }

    public int muted() {
        return muted;
    }

    public int buttonText() {
        return buttonText;
    }

    public int button() {
        return button;
    }

    public int buttonHover() {
        return buttonHover;
    }

    public int buttonPressed() {
        return buttonPressed;
    }

    public int buttonDisabled() {
        return buttonDisabled;
    }

    public int rowSelected() {
        return rowSelected;
    }

    public int danger() {
        return danger;
    }

    public int accent() {
        return accent;
    }

    public boolean ledger() {
        return ledger;
    }

    public boolean rivets() {
        return rivets;
    }

    public int slotEdge() {
        return this == DARK ? 0xFF3A3A3A : bevelDark;
    }

    public int slotFill() {
        return this == DARK ? 0xFF1A1A1A : inset;
    }

    public ResourceLocation sheet() {
        return new ResourceLocation(Cts.MODID, "textures/gui/theme_" + id + ".png");
    }

    public CtsTheme next() {
        CtsTheme[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static CtsTheme fromId(String raw) {
        if (raw == null) {
            return WOOD;
        }
        String id = raw.trim().toLowerCase();
        for (CtsTheme theme : values()) {
            if (theme.id.equals(id)) {
                return theme;
            }
        }
        return WOOD;
    }

    public static CtsTheme current() {
        // Creative themes were reverted: every screen uses the original vanilla GUI.
        return VANILLA;
    }
}
