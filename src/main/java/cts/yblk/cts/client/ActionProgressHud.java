package cts.yblk.cts.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Shared crosshair spinner + labels used by search / break / open holds.
 */
public final class ActionProgressHud {
    private static final int SPINNER_DOTS = 8;
    private static final int[][] DOT_OFFSETS = {
            {0, -14}, {10, -10}, {14, 0}, {10, 10},
            {0, 14}, {-10, 10}, {-14, 0}, {-10, -10},
    };
    private static final int DOT_SIZE = 5;
    private static final boolean[][] DOT_STAMP = {
            {false, true, true, true, false},
            {true, true, true, true, true},
            {true, true, true, true, true},
            {true, true, true, true, true},
            {false, true, true, true, false},
    };
    private static final float SPINNER_STEP_TICKS = 1.75F;
    private static final int LABEL_GAP = 6;

    private ActionProgressHud() {
    }

    public static void render(GuiGraphics graphics, Component title, Component subtitle, int ticks, int duration, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        int cx = minecraft.getWindow().getGuiScaledWidth() / 2;
        int cy = minecraft.getWindow().getGuiScaledHeight() / 2;
        float secondsLeft = Math.max(0.0F, (duration - ticks) / 20.0F);

        drawSpinner(graphics, cx, cy, ticks + partialTick);

        int labelY = cy + 14 + (DOT_SIZE / 2) + LABEL_GAP;
        if (title != null) {
            graphics.drawCenteredString(minecraft.font, title, cx, labelY, 0xFFFFFF);
            labelY += minecraft.font.lineHeight + 2;
        }
        if (subtitle != null) {
            graphics.drawCenteredString(minecraft.font, subtitle, cx, labelY, 0xFFFFFF);
            labelY += minecraft.font.lineHeight + 2;
        }
        graphics.drawCenteredString(minecraft.font, String.format("%.1f", secondsLeft), cx, labelY, 0xFFFFFF);
    }

    private static void drawSpinner(GuiGraphics graphics, int centerX, int centerY, float animTicks) {
        float lead = animTicks / SPINNER_STEP_TICKS;
        RenderSystem.enableBlend();
        int half = DOT_SIZE / 2;
        for (int i = 0; i < SPINNER_DOTS; i++) {
            float trail = (lead - i) % SPINNER_DOTS;
            if (trail < 0.0F) {
                trail += SPINNER_DOTS;
            }
            float brightness = 1.0F - trail / SPINNER_DOTS;
            int alpha = Mth.clamp(Math.round(36 + brightness * 219), 36, 255);
            int color = (alpha << 24) | 0xFFFFFF;
            int[] off = DOT_OFFSETS[i];
            blitDot(graphics, centerX + off[0] - half, centerY + off[1] - half, color);
        }
    }

    private static void blitDot(GuiGraphics graphics, int x, int y, int color) {
        for (int row = 0; row < DOT_SIZE; row++) {
            for (int col = 0; col < DOT_SIZE; col++) {
                if (DOT_STAMP[row][col]) {
                    graphics.fill(x + col, y + row, x + col + 1, y + row + 1, color);
                }
            }
        }
    }
}
