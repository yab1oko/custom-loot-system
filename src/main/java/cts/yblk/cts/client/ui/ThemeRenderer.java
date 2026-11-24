package cts.yblk.cts.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Shared creative chrome. Panels blit a 9-slice from {@code assets/cts/textures/gui/theme_*.png}
 * and then paint theme details that must stay crisp at GUI scale 1–4.
 */
@OnlyIn(Dist.CLIENT)
public final class ThemeRenderer {
    public static final int SHEET = 64;
    private static final int PANEL_U = 0;
    private static final int PANEL_V = 0;
    private static final int PANEL_SIZE = 16;
    private static final int PANEL_BORDER = 4;

    private ThemeRenderer() {
    }

    private static final ResourceLocation CHEST = new ResourceLocation("textures/gui/container/generic_54.png");

    public static void panel(GuiGraphics graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        graphics.blit(CHEST, x, y, w, h, 0, 4, 176, 12, 256, 256);
    }

    public static void inset(GuiGraphics graphics, int x, int y, int w, int h) {
        CtsTheme theme = CtsTheme.current();
        graphics.fill(x, y, x + w, y + h, theme.inset());
        graphics.fill(x, y, x + w, y + 1, theme.bevelDark());
        graphics.fill(x, y, x + 1, y + h, theme.bevelDark());
        graphics.fill(x, y + h - 1, x + w, y + h, theme.bevelLight());
        graphics.fill(x + w - 1, y, x + w, y + h, theme.bevelLight());
    }

    public static void slot(GuiGraphics graphics, int x, int y) {
        graphics.blit(CHEST, x, y, 18, 18, 7, 17, 18, 18, 256, 256);
    }

    public static void listRow(GuiGraphics graphics, int x, int y, int w, int h, boolean selected) {
        CtsTheme theme = CtsTheme.current();
        int fill = selected ? theme.rowSelected() : (theme.panel() & 0x00FFFFFF) | 0x00000000;
        if (selected || theme == CtsTheme.DARK) {
            graphics.fill(x, y, x + w, y + h, selected ? theme.rowSelected() : 0x00000000);
        } else if (fill != 0) {
            graphics.fill(x, y, x + w, y + h, fill);
        }
        if (selected) {
            graphics.fill(x, y, x + w, y + 1, theme.slotEdge());
            graphics.fill(x, y + h - 1, x + w, y + h, theme.slotEdge());
            graphics.fill(x, y, x + 1, y + h, theme.slotEdge());
            graphics.fill(x + w - 1, y, x + w, y + h, theme.slotEdge());
        }
    }

    public static void scrollbar(GuiGraphics graphics, int x, int y, int h, int thumbY, int thumbH) {
        CtsTheme theme = CtsTheme.current();
        graphics.fill(x, y, x + 6, y + h, theme.inset());
        graphics.fill(x, y, x + 6, y + 1, theme.bevelDark());
        int thumb = Math.max(8, thumbH);
        int top = Math.max(y, Math.min(y + h - thumb, thumbY));
        graphics.fill(x + 1, top, x + 5, top + thumb, theme.bevelLight());
    }

    public static void tab(GuiGraphics graphics, int x, int y, int w, int h, boolean selected) {
        CtsTheme theme = CtsTheme.current();
        int fill = selected ? theme.panel() : theme.inset();
        graphics.fill(x, y, x + w, y + h, fill);
        graphics.fill(x, y, x + w, y + 1, theme.bevelLight());
        graphics.fill(x, y, x + 1, y + h, theme.bevelLight());
        graphics.fill(x + w - 1, y, x + w, y + h, theme.bevelDark());
        if (!selected) {
            graphics.fill(x, y + h - 1, x + w, y + h, theme.bevelDark());
        }
    }

    public static void searchField(GuiGraphics graphics, int x, int y, int w, int h, boolean focused) {
        CtsTheme theme = CtsTheme.current();
        graphics.fill(x, y, x + w, y + h, theme == CtsTheme.WOOD ? 0xFFF4E7C4 : theme.inset());
        int edge = focused ? theme.bevelLight() : theme.bevelDark();
        graphics.fill(x, y, x + w, y + 1, edge);
        graphics.fill(x, y, x + 1, y + h, edge);
        graphics.fill(x, y + h - 1, x + w, y + h, theme.bevelDark());
        graphics.fill(x + w - 1, y, x + w, y + h, theme.bevelDark());
    }

    public static void tooltip(GuiGraphics graphics, int x, int y, int w, int h) {
        CtsTheme theme = CtsTheme.current();
        int bg = theme == CtsTheme.VANILLA ? 0xF0100010 : (theme.panel() & 0x00FFFFFF) | 0xF0000000;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.fill(x, y, x + w, y + 1, theme.bevelLight());
        graphics.fill(x, y + h - 1, x + w, y + h, theme.bevelDark());
        graphics.fill(x, y, x + 1, y + h, theme.bevelLight());
        graphics.fill(x + w - 1, y, x + w, y + h, theme.bevelDark());
    }

    public static void button(GuiGraphics graphics, int x, int y, int w, int h, boolean hovered, boolean pressed, boolean active) {
        CtsTheme theme = CtsTheme.current();
        int fill = !active ? theme.buttonDisabled() : pressed ? theme.buttonPressed() : hovered ? theme.buttonHover() : theme.button();
        graphics.fill(x, y, x + w, y + h, fill);
        int light = pressed ? theme.bevelDark() : theme.bevelLight();
        int dark = pressed ? theme.bevelLight() : theme.bevelDark();
        graphics.fill(x, y, x + w, y + 1, light);
        graphics.fill(x, y, x + 1, y + h, light);
        graphics.fill(x, y + h - 1, x + w, y + h, dark);
        graphics.fill(x + w - 1, y, x + w, y + h, dark);
    }

    /** Soft survival backdrop. Alpha stays low on purpose. */
    public static void softBackdrop(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0x40000000);
        graphics.fill(x, y, x + w, y + 1, 0x18000000);
        graphics.fill(x, y + h - 1, x + w, y + h, 0x18000000);
    }

    private static void rivet(GuiGraphics graphics, int x, int y, int metal, int hole) {
        graphics.fill(x, y, x + 3, y + 3, metal);
        graphics.fill(x + 1, y + 1, x + 2, y + 2, hole);
    }

    private static void blitNine(GuiGraphics graphics, ResourceLocation sheet,
                                  int x, int y, int w, int h,
                                  int u, int v, int src, int srcH, int border) {
        int b = Math.min(border, Math.min(w / 2, h / 2));
        int centerSrc = src - border * 2;
        int centerSrcH = srcH - border * 2;
        if (centerSrc <= 0 || centerSrcH <= 0 || b <= 0) {
            graphics.blit(sheet, x, y, w, h, u, v, src, srcH, SHEET, SHEET);
            return;
        }
        int right = x + w - b;
        int bottom = y + h - b;
        int uRight = u + src - b;
        int vBottom = v + srcH - b;
        blit(graphics, sheet, x, y, b, b, u, v, b, b);
        blit(graphics, sheet, right, y, b, b, uRight, v, b, b);
        blit(graphics, sheet, x, bottom, b, b, u, vBottom, b, b);
        blit(graphics, sheet, right, bottom, b, b, uRight, vBottom, b, b);
        blit(graphics, sheet, x + b, y, w - b * 2, b, u + border, v, centerSrc, b);
        blit(graphics, sheet, x + b, bottom, w - b * 2, b, u + border, vBottom, centerSrc, b);
        blit(graphics, sheet, x, y + b, b, h - b * 2, u, v + border, b, centerSrcH);
        blit(graphics, sheet, right, y + b, b, h - b * 2, uRight, v + border, b, centerSrcH);
        blit(graphics, sheet, x + b, y + b, w - b * 2, h - b * 2, u + border, v + border, centerSrc, centerSrcH);
    }

    private static void blit(GuiGraphics graphics, ResourceLocation sheet,
                             int x, int y, int w, int h, int u, int v, int uw, int vh) {
        if (w <= 0 || h <= 0 || uw <= 0 || vh <= 0) {
            return;
        }
        graphics.blit(sheet, x, y, w, h, u, v, uw, vh, SHEET, SHEET);
    }
}
