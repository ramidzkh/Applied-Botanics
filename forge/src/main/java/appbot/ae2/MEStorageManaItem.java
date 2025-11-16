package appbot.ae2;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import vazkii.botania.api.mana.ManaItem;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.items.tools.powered.AbstractPortableCell;

public class MEStorageManaItem implements ManaItem {

    private final MEStorage storage;
    private final IEnergySource energy;
    private final IActionSource source;

    public MEStorageManaItem(MEStorage storage, IEnergySource energy, IActionSource source) {
        this.storage = storage;
        this.energy = energy;
        this.source = source;
    }

    @Nullable
    public static ManaItem forItem(ItemStack stack) {
        if (stack.getItem() instanceof AbstractPortableCell item) {
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

        // we could also add wireless terminal support, but no player
        return null;
    }

    @Override
    public int getMana() {
        return (int) StorageHelper.poweredExtraction(energy, storage, ManaKey.KEY, Integer.MAX_VALUE, source,
                Actionable.SIMULATE);
    }

    @Override
    public int getMaxMana() {
        return Ints.saturatedCast(StorageHelper.poweredExtraction(energy, storage, ManaKey.KEY, Integer.MAX_VALUE,
                source, Actionable.SIMULATE)
                + StorageHelper.poweredInsert(energy, storage, ManaKey.KEY, Integer.MAX_VALUE, source,
                        Actionable.SIMULATE));
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
