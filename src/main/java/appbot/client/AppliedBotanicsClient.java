package appbot.client;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

import appbot.ABItems;
import appbot.ABMenus;
import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;

import appeng.api.client.AEStackRendering;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.init.client.InitScreens;
import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.AbstractPortableCell;
import appeng.menu.me.common.MEStorageMenu;

public interface AppliedBotanicsClient {

    static void initialize() {
        AEStackRendering.register(ManaKeyType.TYPE, ManaKey.class, new ManaRenderer());

        for (var tier : ABItems.Tier.values()) {
            ColorProviderRegistry.ITEM.register(BasicStorageCell::getColor, ABItems.get(tier));
            ColorProviderRegistry.ITEM.register(AbstractPortableCell::getColor, ABItems.getPortable(tier));
        }

        InitScreens.<MEStorageMenu, MEStorageScreen<MEStorageMenu>>register(ABMenus.PORTABLE_MANA_CELL_TYPE,
                MEStorageScreen::new, "/screens/terminals/portable_mana_cell.json");
    }
}
