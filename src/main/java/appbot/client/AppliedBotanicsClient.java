package appbot.client;

import appbot.ABMenus;
import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;

import appeng.api.client.AEStackRendering;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.init.client.InitScreens;
import appeng.menu.me.common.MEStorageMenu;

public interface AppliedBotanicsClient {

    static void initialize() {
        AEStackRendering.register(ManaKeyType.TYPE, ManaKey.class, new ManaRenderer());

        InitScreens.<MEStorageMenu, MEStorageScreen<MEStorageMenu>>register(ABMenus.PORTABLE_MANA_CELL_TYPE,
                MEStorageScreen::new, "/screens/terminals/portable_mana_cell.json");
    }
}
