package cts.yblk.cts.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import cts.yblk.cts.Cts;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Cts.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .title(Component.translatable("itemGroup.cts"))
                    .icon(() -> ModItems.LOOT_PLATE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.LOOT_PLATE.get());
                        output.accept(ModItems.ENTITY_PLATE.get());
                        output.accept(ModItems.LOOT_BOOK.get());
                        output.accept(ModItems.TAG_SLIP.get());
                        output.accept(ModItems.CAN_STASH.get());
                        output.accept(ModItems.MEDKIT.get());
                        output.accept(ModItems.WASTE_DRUM.get());
                        output.accept(ModItems.BOXES.get());
                        output.accept(ModItems.AMMO_BOX.get());
                        output.accept(ModItems.DIRTY_BACKPACK.get());
                        output.accept(ModItems.CLOTHES.get());
                        output.accept(ModItems.TOOLBOX.get());
                        output.accept(ModItems.CRATE.get());
                        output.accept(ModItems.WEAPON_CASE.get());
                        output.accept(ModItems.SAFE.get());
                        output.accept(ModItems.METAL_SCRAP.get());
                        output.accept(ModItems.TRASH_CAN.get());
                        output.accept(ModItems.LITTER.get());
                        output.accept(ModItems.NEST.get());
                        output.accept(ModItems.SHELVING.get());
                        output.accept(ModItems.CASH_REGISTER.get());
                        output.accept(ModItems.MEDICAL_CRATE.get());
                        output.accept(ModItems.LAUNDRY_BASKET.get());
                        output.accept(ModItems.TOILET.get());
                        output.accept(ModItems.FRIDGE.get());
                        output.accept(ModItems.WASHING_MACHINE.get());
                        output.accept(ModItems.MICROWAVE.get());
                        output.accept(ModItems.CHEST.get());
                        output.accept(ModItems.CORPSE.get());
                        output.accept(ModItems.AIRDROP.get());
                        output.accept(ModItems.CONVERSION_WAND.get());
                        output.accept(ModItems.CHEST_LIBRARY.get());
                        for (var dresser : ModItems.DRESSERS) {
                            output.accept(dresser.get());
                        }
                        for (var drawer : ModItems.KITCHEN_DRAWERS) {
                            output.accept(drawer.get());
                        }
                        for (var drawer : ModItems.KITCHEN_DRAWER_SINGLES) {
                            output.accept(drawer.get());
                        }
                        for (var cache : ModItems.BRICK_CACHES) {
                            output.accept(cache.get());
                        }
                        output.accept(ModItems.BARRICADE.get());
                        output.accept(ModItems.VENTILATION_GRILLE.get());
                        output.accept(ModItems.METAL_ORE.get());
                        output.accept(ModItems.HARVEST_TREE.get());
                        output.accept(ModItems.HARVEST_TREE_LARGE.get());
                    })
                    .build());
}
