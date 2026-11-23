package cts.yblk.cts.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import cts.yblk.cts.config.CtsConfig;

@OnlyIn(Dist.CLIENT)
public class ThemedButton extends Button {
    private boolean pressed;
    private boolean danger;

    public ThemedButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public ThemedButton danger() {
        this.danger = true;
        return this;
    }

    public static Builder create(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
            this.pressed = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.pressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (CtsConfig.buttonSounds()) {
            super.playDownSound(handler);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean down = this.pressed && this.isHovered();
        ThemeRenderer.button(graphics, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused(), down, this.active);
        CtsTheme theme = CtsTheme.current();
        int color = !this.active ? theme.muted() : this.danger ? theme.danger() : theme.buttonText();
        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Builder {
        private final Component message;
        private final OnPress onPress;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private boolean danger;

        private Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder danger() {
            this.danger = true;
            return this;
        }

        public ThemedButton build() {
            ThemedButton button = new ThemedButton(x, y, width, height, message, onPress);
            if (danger) {
                button.danger();
            }
            return button;
        }
    }
}
