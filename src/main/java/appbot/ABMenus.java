package appbot;

import net.minecraft.world.inventory.MenuType;

import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;

public class ABMenus {

    public static final MenuType<MEStorageMenu> PORTABLE_MANA_CELL_TYPE = MenuTypeBuilder
            .create(MEStorageMenu::new, IPortableTerminal.class).build("portable_mana_cell");

    public static void register() {
    }
}
