package cts.yblk.cts.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import cts.yblk.cts.client.ui.CtsTheme;
import cts.yblk.cts.client.ui.ThemeRenderer;
import cts.yblk.cts.client.ui.ThemedButton;
import cts.yblk.cts.network.ConvertContainerPacket;
import cts.yblk.cts.network.ModNetwork;

@OnlyIn(Dist.CLIENT)
public final class ConvertConfirmScreen extends Screen {
    private final BlockPos pos;
    private final boolean revert;
    private final ItemStack icon;

    private ConvertConfirmScreen(BlockPos pos, boolean revert, ItemStack icon) {
        super(Component.translatable(revert ? "gui.cts.convert_revert_title" : "gui.cts.convert_title"));
        this.pos = pos;
        this.revert = revert;
        this.icon = icon;
    }

    public static void open(BlockPos pos, boolean revert) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack icon = ItemStack.EMPTY;
        if (minecraft.level != null) {
            BlockState state = minecraft.level.getBlockState(pos);
            Block block = state.getBlock();
            if (block.asItem() != net.minecraft.world.item.Items.AIR) {
                icon = new ItemStack(block);
            }
        }
        minecraft.setScreen(new ConvertConfirmScreen(pos, revert, icon));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 + 36;
        addRenderableWidget(ThemedButton.create(Component.translatable("gui.cts.confirm"), button -> {
            ModNetwork.CHANNEL.sendToServer(new ConvertContainerPacket(pos, revert));
            onClose();
        }).bounds(cx - 104, y, 100, 20).build());
        addRenderableWidget(ThemedButton.create(Component.translatable("gui.cts.cancel"), button -> onClose())
                .bounds(cx + 4, y, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int cx = this.width / 2;
        int top = this.height / 2 - 58;
        ThemeRenderer.panel(graphics, cx - 120, top, 240, 130);
        CtsTheme theme = CtsTheme.current();
        if (!icon.isEmpty()) {
            graphics.renderItem(icon, cx - 8, top + 10);
        }
        graphics.drawCenteredString(this.font, this.title, cx, top + 32, theme.ink());
        int lineY = top + 48;
        if (revert) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.cts.convert_revert_short"), cx, lineY, theme.muted());
        } else {
            graphics.drawCenteredString(this.font, Component.translatable("gui.cts.convert_keep_short"), cx, lineY, theme.muted());
            graphics.drawCenteredString(this.font, Component.translatable("gui.cts.convert_table_wins"), cx, lineY + 12, theme.muted());
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
