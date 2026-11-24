package cts.yblk.cts.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.LootPlateBlockEntity;
import cts.yblk.cts.client.CanStashConfigScreen;
import cts.yblk.cts.client.LootPlateScreen;
import cts.yblk.cts.network.ModNetwork;
import cts.yblk.cts.network.PlaceGhostItemPacket;

import java.util.ArrayList;
import java.util.List;

/** Drag an ingredient from JEI onto a loot template slot. */
public class CtsGhostSlots<T extends AbstractContainerScreen<?>> implements IGhostIngredientHandler<T> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(T screen, ITypedIngredient<I> ingredient, boolean doStart) {
        if (!VanillaTypes.ITEM_STACK.equals(ingredient.getType())) {
            return List.of();
        }
        ItemStack ghost = ((ItemStack) ingredient.getIngredient()).copy();
        if (ghost.isEmpty()) {
            return List.of();
        }
        ghost.setCount(1);
        List<Target<I>> targets = new ArrayList<>();
        if (screen instanceof CanStashConfigScreen stash) {
            int limit = CanStashBlockEntity.SLOT_COUNT;
            for (Slot slot : stash.getMenu().slots) {
                if (slot.index < limit && slot.isActive()) {
                    targets.add(target(stash, slot, ghost, false, stash.getMenu().getStash().getBlockPos()));
                }
            }
        } else if (screen instanceof LootPlateScreen plate) {
            int limit = LootPlateBlockEntity.SLOT_COUNT;
            for (Slot slot : plate.getMenu().slots) {
                if (slot.index < limit && slot.isActive()) {
                    targets.add(target(plate, slot, ghost, true, plate.getMenu().getPlate().getBlockPos()));
                }
            }
        }
        return targets;
    }

    private static <I> Target<I> target(AbstractContainerScreen<?> screen, Slot slot, ItemStack ghost,
                                        boolean plate, net.minecraft.core.BlockPos pos) {
        int index = slot.index;
        Rect2i area = new Rect2i(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
        return new Target<>() {
            @Override
            public Rect2i getArea() {
                return area;
            }

            @Override
            public void accept(I ingredient) {
                if (plate && screen.getMenu() instanceof cts.yblk.cts.menu.LootPlateMenu menu) {
                    menu.getPlate().placeTemplate(index, ghost.copy());
                } else if (screen.getMenu() instanceof cts.yblk.cts.menu.CanStashConfigMenu menu) {
                    menu.getStash().placeTemplate(index, ghost.copy());
                }
                ModNetwork.CHANNEL.sendToServer(new PlaceGhostItemPacket(pos, index, ghost.copy(), plate));
            }
        };
    }

    @Override
    public void onComplete() {
    }
}
