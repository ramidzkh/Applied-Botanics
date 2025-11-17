package appbot;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.init.client.InitScreens;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;

public class ABMenus {

    public static final MenuType<MEStorageMenu> PORTABLE_MANA_CELL_TYPE = MenuTypeBuilder
            .create(MEStorageMenu::new, IPortableTerminal.class)
            .build(AppliedBotanics.id("portable_mana_cell"));

    @SuppressWarnings("RedundantTypeArguments")
    public static void initialize(IEventBus bus) {
        bus.addListener((RegisterMenuScreensEvent event) -> {
            InitScreens.<MEStorageMenu, MEStorageScreen<MEStorageMenu>>register(event, PORTABLE_MANA_CELL_TYPE,
                    MEStorageScreen::new, "/screens/terminals/portable_mana_cell.json");
        });
    }
}
