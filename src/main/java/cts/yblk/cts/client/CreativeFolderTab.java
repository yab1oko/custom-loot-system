package cts.yblk.cts.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.registry.ModCreativeTabs;
import cts.yblk.cts.registry.ModItems;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
public class CreativeFolderTab {
    private static final int COLUMNS = 9;
    private static final int VISIBLE_ROWS = 5;
    private static final int BORDER = 0xFF373737;
    private static final ResourceLocation CREATIVE_BACKGROUND = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");
    private static final Folder PROPS = new Folder(Component.translatable("itemGroup.cts.props"),
            ModItems.CAN_STASH, ModItems.MEDKIT, ModItems.WASTE_DRUM, ModItems.BOXES, ModItems.AMMO_BOX, ModItems.DIRTY_BACKPACK, ModItems.CLOTHES, ModItems.TOOLBOX, ModItems.CRATE, ModItems.WEAPON_CASE, ModItems.SAFE, ModItems.METAL_SCRAP, ModItems.TRASH_CAN, ModItems.LITTER, ModItems.NEST, ModItems.SHELVING, ModItems.CASH_REGISTER, ModItems.MEDICAL_CRATE, ModItems.LAUNDRY_BASKET, ModItems.TOILET, ModItems.FRIDGE, ModItems.WASHING_MACHINE, ModItems.MICROWAVE, ModItems.CHEST, ModItems.CORPSE, ModItems.AIRDROP);
    private static final Folder BLOCKS = new Folder(Component.translatable("itemGroup.cts.blocks"),
            join(ModItems.DRESSERS, ModItems.KITCHEN_DRAWERS, ModItems.KITCHEN_DRAWER_SINGLES, ModItems.BRICK_CACHES));
    private static final Folder BREAKABLES = new Folder(Component.translatable("itemGroup.cts.breakables"),
            ModItems.BARRICADE, ModItems.VENTILATION_GRILLE, ModItems.METAL_ORE,
            ModItems.HARVEST_TREE, ModItems.HARVEST_TREE_LARGE);

    private static Field selectedTabField;
    private static Field scrollField;
    private static final List<ItemStack> layout = new ArrayList<>();
    private static final List<Folder> headerAtRow = new ArrayList<>();

    private CreativeFolderTab() {
    }

    @SubscribeEvent
    public static void beforeRender(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof CreativeModeInventoryScreen screen && isCtsTab()) {
            apply(screen.getMenu());
        }
    }

    @SubscribeEvent
    public static void onForeground(ContainerScreenEvent.Render.Foreground event) {
        if (!(event.getContainerScreen() instanceof CreativeModeInventoryScreen screen) || !isCtsTab()) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        Font font = Minecraft.getInstance().font;
        int origin = scrollRow(screen);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        RenderSystem.disableDepthTest();
        for (int row = 0; row < headerAtRow.size(); row++) {
            int visible = row - origin;
            if (visible < 0 || visible >= VISIBLE_ROWS || headerAtRow.get(row) == null) {
                continue;
            }
            drawFolder(graphics, font, 9, 18 + visible * 18, headerAtRow.get(row));
        }
        for (int row = 0; row < headerAtRow.size(); row++) {
            int visible = row - origin;
            if (visible < 0 || visible >= VISIBLE_ROWS || headerAtRow.get(row) == null) {
                continue;
            }
            drawFolderBorder(graphics, 9, 18 + visible * 18);
        }
        RenderSystem.enableDepthTest();
        graphics.pose().popPose();
    }

    @SubscribeEvent
    public static void onClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen) || !isCtsTab()) {
            return;
        }
        int x = (int) event.getMouseX() - screen.getGuiLeft() - 9;
        int y = (int) event.getMouseY() - screen.getGuiTop() - 18;
        if (x < -1 || x >= COLUMNS * 18 || y < 0 || y >= VISIBLE_ROWS * 18) {
            return;
        }
        int row = y / 18 + scrollRow(screen);
        if (row < 0 || row >= headerAtRow.size()) {
            return;
        }
        Folder folder = headerAtRow.get(row);
        if (folder == null) {
            return;
        }
        if (event.getButton() == 0) {
            folder.open = !folder.open;
            apply(screen.getMenu());
        }
        event.setCanceled(true);
    }

    private static void apply(CreativeModeInventoryScreen.ItemPickerMenu menu) {
        rebuild();
        if (same(menu.items, layout)) {
            return;
        }
        menu.items.clear();
        menu.items.addAll(layout);
        menu.scrollTo(0.0F);
    }

    private static void rebuild() {
        layout.clear();
        headerAtRow.clear();
        headerAtRow.add(null);
        layout.add(new ItemStack(ModItems.LOOT_PLATE.get()));
        layout.add(new ItemStack(ModItems.ENTITY_PLATE.get()));
        layout.add(new ItemStack(ModItems.LOOT_BOOK.get()));
        layout.add(new ItemStack(ModItems.TAG_SLIP.get()));
        layout.add(new ItemStack(ModItems.CONVERSION_WAND.get()));
        layout.add(new ItemStack(ModItems.CHEST_LIBRARY.get()));
        padRow(6);
        appendFolder(PROPS);
        appendFolder(BLOCKS);
        appendFolder(BREAKABLES);
    }

    private static void appendFolder(Folder folder) {
        headerAtRow.add(folder);
        for (int column = 0; column < COLUMNS; column++) {
            layout.add(ItemStack.EMPTY);
        }
        if (!folder.open) {
            return;
        }
        int column = 0;
        for (RegistryObject<Item> item : folder.items) {
            if (column == 0) {
                headerAtRow.add(null);
            }
            layout.add(new ItemStack(item.get()));
            column++;
            if (column == COLUMNS) {
                column = 0;
            }
        }
        if (column != 0) {
            padRow(column);
        }
    }

    private static void padRow(int column) {
        while (column < COLUMNS) {
            layout.add(ItemStack.EMPTY);
            column++;
        }
    }

    private static boolean same(List<ItemStack> current, List<ItemStack> next) {
        if (current.size() != next.size()) {
            return false;
        }
        for (int i = 0; i < next.size(); i++) {
            ItemStack left = current.get(i);
            ItemStack right = next.get(i);
            if (left.isEmpty() && right.isEmpty()) {
                continue;
            }
            if (!ItemStack.isSameItemSameTags(left, right)) {
                return false;
            }
        }
        return true;
    }

    private static void drawFolder(GuiGraphics graphics, Font font, int x, int y, Folder folder) {
        graphics.blit(CREATIVE_BACKGROUND, x - 1, y - 1, COLUMNS * 18 + 1, 19, 28, 6, 16, 8, 256, 256);
        int textY = y + 4;
        graphics.drawString(font, folder.label, x - 1, textY, 0x404040, false);
        String mark = folder.open ? "-" : "+";
        graphics.drawString(font, mark, x + COLUMNS * 18 - 4 - font.width(mark), textY, 0x404040, false);
    }

    private static void drawFolderBorder(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y + 17, x + COLUMNS * 18 - 1, y + 18, BORDER);
    }

    private static int scrollRow(CreativeModeInventoryScreen screen) {
        int extra = Mth.positiveCeilDiv(screen.getMenu().items.size(), COLUMNS) - VISIBLE_ROWS;
        if (extra <= 0) {
            return 0;
        }
        return Math.max((int) (readScroll(screen) * extra + 0.5D), 0);
    }

    private static float readScroll(CreativeModeInventoryScreen screen) {
        try {
            if (scrollField == null) {
                scrollField = findScrollField();
                scrollField.setAccessible(true);
            }
            return scrollField.getFloat(screen);
        } catch (ReflectiveOperationException ignored) {
            return 0.0F;
        }
    }

    private static Field findScrollField() throws NoSuchFieldException {
        try {
            return CreativeModeInventoryScreen.class.getDeclaredField("scrollOffs");
        } catch (NoSuchFieldException ignored) {
        }
        for (Field field : CreativeModeInventoryScreen.class.getDeclaredFields()) {
            if (field.getType() == float.class && !Modifier.isStatic(field.getModifiers())) {
                return field;
            }
        }
        throw new NoSuchFieldException("scrollOffs");
    }

    private static boolean isCtsTab() {
        CreativeModeTab tab = selectedTab();
        return tab != null && tab == ModCreativeTabs.MAIN.get();
    }

    private static CreativeModeTab selectedTab() {
        try {
            if (selectedTabField == null) {
                try {
                    selectedTabField = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
                } catch (NoSuchFieldException missing) {
                    selectedTabField = CreativeModeInventoryScreen.class.getDeclaredField("f_98507_");
                }
                selectedTabField.setAccessible(true);
            }
            return (CreativeModeTab) selectedTabField.get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static final class Folder {
        private final Component label;
        private final RegistryObject<Item>[] items;
        private boolean open = true;

        @SafeVarargs
        private Folder(Component label, RegistryObject<Item>... items) {
            this.label = label;
            this.items = items;
        }

        private Folder(Component label, List<RegistryObject<Item>> items) {
            this.label = label;
            @SuppressWarnings("unchecked")
            RegistryObject<Item>[] array = items.toArray(RegistryObject[]::new);
            this.items = array;
        }
    }

    @SafeVarargs
    private static List<RegistryObject<Item>> join(List<RegistryObject<Item>>... groups) {
        List<RegistryObject<Item>> all = new ArrayList<>();
        for (List<RegistryObject<Item>> group : groups) {
            all.addAll(group);
        }
        return all;
    }
}
