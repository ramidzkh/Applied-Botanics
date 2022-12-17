package appbot;

import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.core.AppEng;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;

public class ABMenus {

    public static final MenuType<MEStorageMenu> PORTABLE_MANA_CELL_TYPE = MenuTypeBuilder
            .create(MEStorageMenu::new, IPortableTerminal.class).build("portable_mana_cell");

    public static void initialize(IEventBus bus) {
        bus.addListener((RegisterEvent event) -> {
            if (!event.getRegistryKey().equals(Registry.BLOCK_REGISTRY)) {
                return;
            }

            ForgeRegistries.MENU_TYPES.register(AppEng.makeId("portable_mana_cell"), PORTABLE_MANA_CELL_TYPE);
        });
    }
}
