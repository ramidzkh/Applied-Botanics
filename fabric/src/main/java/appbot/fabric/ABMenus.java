package appbot.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.core.AppEng;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;

public class ABMenus {

    public static final MenuType<MEStorageMenu> PORTABLE_MANA_CELL_TYPE = MenuTypeBuilder
            .create(MEStorageMenu::new, IPortableTerminal.class).build("portable_mana_cell");

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, AppEng.makeId("portable_mana_cell"), PORTABLE_MANA_CELL_TYPE);
    }
}
