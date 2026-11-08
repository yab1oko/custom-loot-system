package cts.yblk.cts.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import cts.yblk.cts.Cts;
import cts.yblk.cts.menu.BarricadeConfigMenu;
import cts.yblk.cts.menu.HarvestTreeConfigMenu;
import cts.yblk.cts.menu.MetalOreConfigMenu;
import cts.yblk.cts.menu.VentilationGrilleConfigMenu;
import cts.yblk.cts.menu.CanStashConfigMenu;
import cts.yblk.cts.menu.CanStashMenu;
import cts.yblk.cts.menu.EntityPlateMenu;
import cts.yblk.cts.menu.LootBookMenu;
import cts.yblk.cts.menu.LootPlateMenu;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Cts.MODID);

    public static final RegistryObject<MenuType<LootPlateMenu>> LOOT_PLATE =
            MENUS.register("loot_plate", () -> IForgeMenuType.create(LootPlateMenu::new));

    public static final RegistryObject<MenuType<CanStashMenu>> CAN_STASH =
            MENUS.register("can_stash", () -> IForgeMenuType.create(CanStashMenu::new));

    public static final RegistryObject<MenuType<CanStashConfigMenu>> CAN_STASH_CONFIG =
            MENUS.register("can_stash_config", () -> IForgeMenuType.create(CanStashConfigMenu::new));

    public static final RegistryObject<MenuType<EntityPlateMenu>> ENTITY_PLATE =
            MENUS.register("entity_plate", () -> IForgeMenuType.create(EntityPlateMenu::new));

    public static final RegistryObject<MenuType<LootBookMenu>> LOOT_BOOK =
            MENUS.register("loot_book", () -> IForgeMenuType.create(LootBookMenu::new));

    public static final RegistryObject<MenuType<BarricadeConfigMenu>> BARRICADE_CONFIG =
            MENUS.register("barricade_config", () -> IForgeMenuType.create(BarricadeConfigMenu::new));

    public static final RegistryObject<MenuType<VentilationGrilleConfigMenu>> VENTILATION_GRILLE_CONFIG =
            MENUS.register("ventilation_grille_config", () -> IForgeMenuType.create(VentilationGrilleConfigMenu::new));

    public static final RegistryObject<MenuType<MetalOreConfigMenu>> METAL_ORE_CONFIG =
            MENUS.register("metal_ore_config", () -> IForgeMenuType.create(MetalOreConfigMenu::new));

    public static final RegistryObject<MenuType<HarvestTreeConfigMenu>> HARVEST_TREE_CONFIG =
            MENUS.register("harvest_tree_config", () -> IForgeMenuType.create(HarvestTreeConfigMenu::new));
}
