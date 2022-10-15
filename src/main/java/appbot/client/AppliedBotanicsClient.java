package appbot.client;

import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appbot.ABItems;

import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.PortableCellItem;

public interface AppliedBotanicsClient {

    static void initialize() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(AppliedBotanicsClient::registerItemColors);
        ManaRenderer.initialize(bus);
    }

    private static void registerItemColors(ColorHandlerEvent.Item event) {
        var colors = event.getItemColors();

        for (var tier : ABItems.Tier.values()) {
            colors.register(BasicStorageCell::getColor, ABItems.get(tier)::get);
            colors.register(PortableCellItem::getColor, ABItems.getPortableCell(tier)::get);
        }
    }
}
