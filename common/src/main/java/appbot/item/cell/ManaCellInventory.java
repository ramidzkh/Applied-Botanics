package appbot.item.cell;

import org.jetbrains.annotations.Nullable;

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
    @Nullable
    private final ISaveProvider container;

    private long storedMana;
    private boolean isPersisted = true;

    public ManaCellInventory(IManaCellItem cellType, ItemStack o, @Nullable ISaveProvider container) {
        this.cellType = cellType;
        this.i = o;
        this.container = container;
        this.storedMana = getTag().getLong(AMOUNT);
    }

    private CompoundTag getTag() {
        return this.i.getOrCreateTag();
    }

    @Override
    public CellState getStatus() {
        if (this.storedMana == 0) {
            return CellState.EMPTY;
        }
        if (this.storedMana == getMaxMana()) {
            return CellState.FULL;
        }
        if (this.storedMana > getMaxMana() / 2) {
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
        var amountPerByte = ManaKeyType.TYPE.getAmountPerByte();
        return (this.storedMana + amountPerByte - 1) / amountPerByte;
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
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var inserted = Math.min(getMaxMana() - this.storedMana, amount);

        if (mode == Actionable.MODULATE) {
            this.storedMana += inserted;
            saveChanges();
        }

        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var extracted = Math.min(this.storedMana, amount);

        if (mode == Actionable.MODULATE) {
            this.storedMana -= extracted;
            saveChanges();
        }

        return extracted;
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }

        if (this.storedMana <= 0) {
            this.getTag().remove(AMOUNT);
        } else {
            this.getTag().putLong(AMOUNT, this.storedMana);
        }

        this.isPersisted = true;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.storedMana > 0) {
            out.add(ManaKey.KEY, this.storedMana);
        }
    }

    @Override
    public Component getDescription() {
        return this.i.getHoverName();
    }
}
