package appbot.client;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import appbot.ABItems;
import appbot.AppliedBotanics;

import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.PortableCellItem;

@Mod(value = AppliedBotanics.MOD_ID, dist = Dist.CLIENT)
public class AppliedBotanicsClient {

    public AppliedBotanicsClient(IEventBus bus) {
        bus.addListener(this::registerItemColors);
        ManaRenderer.initialize(bus);
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColor cells = (stack, tintIndex) -> {
            return FastColor.ARGB32.opaque(BasicStorageCell.getColor(stack, tintIndex));
        };
        ItemColor portableCells = (stack, tintIndex) -> {
            return FastColor.ARGB32.opaque(PortableCellItem.getColor(stack, tintIndex));
        };

        for (var tier : ABItems.Tier.values()) {
            event.register(cells, ABItems.get(tier)::get);
            event.register(portableCells, ABItems.getPortableCell(tier)::get);
        }
    }
}
