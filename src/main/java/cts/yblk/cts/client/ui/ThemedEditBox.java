package cts.yblk.cts.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThemedEditBox extends EditBox {
    public ThemedEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        setBordered(false);
        applyColors();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        applyColors();
        ThemeRenderer.searchField(graphics, getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, isFocused());
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    private void applyColors() {
        CtsTheme theme = CtsTheme.current();
        setTextColor(theme.ink());
        setTextColorUneditable(theme.muted());
    }
}
