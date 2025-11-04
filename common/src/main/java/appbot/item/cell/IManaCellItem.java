package appbot.item.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import appbot.ae2.ManaKey;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.core.AEConfig;
import appeng.items.storage.StorageCellTooltipComponent;

public interface IManaCellItem extends ICellWorkbenchItem {

    long getTotalBytes();

    double getIdleDrain();

    @Override
    default FuzzyMode getFuzzyMode(ItemStack is) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    default void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
    }

    default void addCellInformationToTooltip(ItemStack is, List<Component> lines) {
        Preconditions.checkArgument(is.getItem() == this);
        ManaCellHandler.INSTANCE.addCellInformationToTooltip(is, lines);
    }

    default Optional<TooltipComponent> getCellTooltipImage(ItemStack is) {
        var upgradeStacks = new ArrayList<ItemStack>();

        if (AEConfig.instance().isTooltipShowCellUpgrades()) {
            for (var upgrade : this.getUpgrades(is)) {
                upgradeStacks.add(upgrade);
            }
        }

        List<GenericStack> content = List.of();

        if (AEConfig.instance().isTooltipShowCellContent()) {
            var handler = ManaCellHandler.INSTANCE.getCellInventory(is, null);
            var amount = 0L;

            if (handler != null) {
                amount = handler.extract(ManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, IActionSource.empty());
            }

            if (amount > 0) {
                content = List.of(new GenericStack(ManaKey.KEY, amount));
            }
        }

        return Optional.of(new StorageCellTooltipComponent(
                upgradeStacks,
                content,
                false,
                true));
    }
}
