package appbot.common.item.cell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appbot.common.ae2.ManaKey;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;

public class CreativeManaCellInventory implements StorageCell {

    private final ItemStack i;

    public CreativeManaCellInventory(ItemStack o) {
        this.i = o;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return what instanceof ManaKey ? amount : 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return what instanceof ManaKey ? amount : 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        out.add(ManaKey.KEY, Integer.MAX_VALUE);
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what instanceof ManaKey;
    }

    @Override
    public CellState getStatus() {
        return CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public Component getDescription() {
        return this.i.getHoverName();
    }

    @Override
    public void persist() {
    }
}
