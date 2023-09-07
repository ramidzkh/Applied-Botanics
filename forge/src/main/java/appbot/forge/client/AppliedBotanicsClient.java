package appbot.forge.client;

import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appbot.common.ae2.ManaKey;
import appbot.common.ae2.ManaKeyType;
import appbot.common.client.ManaRenderer;
import appbot.forge.ABItems;
import appbot.forge.ABMenus;

import appeng.api.client.AEStackRendering;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.init.client.InitScreens;
import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.PortableCellItem;
import appeng.menu.me.common.MEStorageMenu;

public interface AppliedBotanicsClient {

    static void initialize() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener((RegisterColorHandlersEvent.Item event) -> {
            for (var tier : ABItems.Tier.values()) {
                event.register(BasicStorageCell::getColor, ABItems.get(tier).get());
                event.register(PortableCellItem::getColor, ABItems.getPortable(tier).get());
            }
        });

        bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            AEStackRendering.register(ManaKeyType.TYPE, ManaKey.class, new ManaRenderer());
            InitScreens.<MEStorageMenu, MEStorageScreen<MEStorageMenu>>register(ABMenus.PORTABLE_MANA_CELL_TYPE,
                    MEStorageScreen::new, "/screens/terminals/portable_mana_cell.json");
        }));
    }
}
