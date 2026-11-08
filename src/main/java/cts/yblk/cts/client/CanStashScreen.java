package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import cts.yblk.cts.menu.CanStashMenu;

public class CanStashScreen extends AbstractContainerScreen<CanStashMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public CanStashScreen(CanStashMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, 71);
        graphics.blit(TEXTURE, leftPos, topPos + 71, 0, 126, imageWidth, 96);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int ticks = menu.isAirdrop() ? menu.getAirdropDespawnTicks() : menu.getCooldownTicks();
        if (ticks <= 0) {
            return;
        }
        int totalSeconds = (ticks + 19) / 20;
        String time = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
        Component text = menu.isAirdrop()
                ? Component.translatable("gui.cts.until_despawn", time)
                : Component.translatable("gui.cts.until_update", time);
        float scale = 0.8F;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.imageWidth - 7.0F - this.font.width(text) * scale, this.titleLabelY, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(this.font, text, 0, 0, 0x404040, false);
        pose.popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
