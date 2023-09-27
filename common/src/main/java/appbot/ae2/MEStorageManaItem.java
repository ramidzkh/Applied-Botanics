package appbot.ae2;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import vazkii.botania.api.mana.ManaItem;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.StorageCell;
import appeng.items.tools.powered.AbstractPortableCell;

public class MEStorageManaItem implements ManaItem {

    private final StorageCell storage;
    private final IEnergySource energy;
    private final IActionSource source;

    public MEStorageManaItem(StorageCell storage, IEnergySource energy, IActionSource source) {
        this.storage = storage;
        this.energy = energy;
        this.source = source;
    }

    @Nullable
    public static ManaItem forPortable(ItemStack stack) {
        if (!(stack.getItem()instanceof AbstractPortableCell item)) {
            return null;
        }

        var storage = StorageCells.getCellInventory(stack, null);

        if (storage == null) {
            return null;
        }

        return new MEStorageManaItem(storage, (amount, mode, multiplier) -> {
            amount = multiplier.multiply(amount);

            if (mode == Actionable.SIMULATE) {
                return multiplier.divide(Math.min(amount, item.getAECurrentPower(stack)));
            }

            return multiplier.divide(item.extractAEPower(stack, amount, Actionable.MODULATE));
        }, IActionSource.empty());
    }

    @Override
    public int getMana() {
        return (int) storage.extract(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, source);
    }

    @Override
    public int getMaxMana() {
        return Ints.saturatedCast(storage.extract(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, source)
                + storage.insert(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, source));
    }

    @Override
    public void addMana(int mana) {
        if (mana > 0) {
            StorageHelper.poweredInsert(energy, storage, ManaKey.KEY, mana, source);
        } else {
            StorageHelper.poweredExtraction(energy, storage, ManaKey.KEY, -mana, source);
        }
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity pool) {
        return true;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack otherStack) {
        return true;
    }

    @Override
    public boolean canExportManaToPool(BlockEntity pool) {
        return true;
    }

    @Override
    public boolean canExportManaToItem(ItemStack otherStack) {
        return true;
    }

    @Override
    public boolean isNoExport() {
        return false;
    }
}
