package appbot;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.init.client.InitScreens;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;

public class ABMenus {

    public static final MenuType<MEStorageMenu> PORTABLE_MANA_CELL_TYPE = MenuTypeBuilder
            .create(MEStorageMenu::new, IPortableTerminal.class).build("portable_mana_cell");

    @SuppressWarnings("RedundantTypeArguments")
    public static void initialize(IEventBus bus) {
        bus.addGenericListener(MenuType.class, (RegistryEvent.Register<MenuType<?>> event) -> {
            event.getRegistry().registerAll(PORTABLE_MANA_CELL_TYPE);
        });

        bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            InitScreens.<MEStorageMenu, MEStorageScreen<MEStorageMenu>>register(PORTABLE_MANA_CELL_TYPE,
                    MEStorageScreen::new, "/screens/terminals/portable_mana_cell.json");
        }));
    }
}
