package appbot.item.cell;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;

public class ManaCellInventory implements StorageCell {

    private static final String AMOUNT = "amount";

    private final IManaCellItem cellType;
    private final ItemStack i;
    private final ISaveProvider container;

    private long manaAmount;
    private boolean isPersisted = true;

    public ManaCellInventory(IManaCellItem cellType, ItemStack o, ISaveProvider container) {
        this.cellType = cellType;
        this.i = o;
        this.container = container;

        this.manaAmount = getTag().getLong(AMOUNT);
    }

    private CompoundTag getTag() {
        return this.i.getOrCreateTag();
    }

    @Override
    public CellState getStatus() {
        if (this.manaAmount == 0) {
            return CellState.EMPTY;
        }
        if (this.manaAmount == getMaxMana()) {
            return CellState.FULL;
        }
        if (this.manaAmount > getMaxMana() / 2) {
            return CellState.TYPES_FULL;
        }
        return CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    private long getMaxMana() {
        return this.cellType.getTotalBytes() * ManaKeyType.TYPE.getAmountPerByte();
    }

    protected long getTotalBytes() {
        return this.cellType.getTotalBytes();
    }

    protected long getUsedBytes() {
        return this.manaAmount / ManaKeyType.TYPE.getAmountPerByte();
    }

    protected void saveChanges() {
        this.isPersisted = false;
        if (this.container != null) {
            this.container.saveChanges();
        } else {
            // if there is no ISaveProvider, store to NBT immediately
            this.persist();
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0 || !(what instanceof ManaKey) || this.manaAmount == getMaxMana()) {
            return 0;
        }

        long remainingAmount = Math.max(0, getMaxMana() - this.manaAmount);
        if (amount > remainingAmount) {
            amount = remainingAmount;
        }
        if (mode == Actionable.MODULATE) {
            this.manaAmount += amount;
            saveChanges();
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        var extractAmount = Math.min(Integer.MAX_VALUE, amount);
        var currentAmount = this.manaAmount;

        if (this.manaAmount > 0 && Objects.equals(ManaKey.KEY, what)) {
            if (mode == Actionable.MODULATE) {
                this.manaAmount = Math.max(0, this.manaAmount - extractAmount);
                saveChanges();
            }
            return Math.min(extractAmount, currentAmount);
        }
        return 0;
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }

        if (this.manaAmount < 0) {
            this.getTag().remove(AMOUNT);
        } else {
            this.getTag().putLong(AMOUNT, this.manaAmount);
        }

        this.isPersisted = true;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.manaAmount > 0) {
            out.add(ManaKey.KEY, this.manaAmount);
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what instanceof ManaKey;
    }

    @Override
    public Component getDescription() {
        return this.i.getHoverName();
    }
}
