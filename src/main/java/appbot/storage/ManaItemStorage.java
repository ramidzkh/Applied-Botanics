package appbot.storage;

import java.util.Iterator;

import com.google.common.collect.Iterators;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import vazkii.botania.api.mana.IManaItem;

@SuppressWarnings("UnstableApiUsage")
public class ManaItemStorage extends SnapshotParticipant<Integer> implements Storage<ManaVariant> {

    private final IManaItem item;
    private int amount;

    public ManaItemStorage(IManaItem item) {
        this.item = item;
        this.amount = item.getMana();
    }

    @Override
    public long insert(ManaVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        long inserted = Math.min(maxAmount, item.getMaxMana() - amount);

        if (inserted > 0) {
            updateSnapshots(transaction);
            amount += inserted;
            return inserted;
        }

        return 0;
    }

    @Override
    public long extract(ManaVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        if (item.isNoExport()) {
            return 0;
        }

        long extracted = Math.min(maxAmount, amount);

        if (extracted > 0) {
            updateSnapshots(transaction);
            amount -= extracted;
            return extracted;
        }

        return 0;
    }

    @Override
    public Iterator<? extends StorageView<ManaVariant>> iterator(TransactionContext transaction) {
        return Iterators.singletonIterator(new StorageView<ManaVariant>() {
            @Override
            public long extract(ManaVariant resource, long maxAmount, TransactionContext transaction) {
                return ManaItemStorage.this.extract(resource, maxAmount, transaction);
            }

            @Override
            public boolean isResourceBlank() {
                return false;
            }

            @Override
            public ManaVariant getResource() {
                return ManaVariant.VARIANT;
            }

            @Override
            public long getAmount() {
                return amount;
            }

            @Override
            public long getCapacity() {
                return item.getMaxMana();
            }
        });
    }

    @Override
    protected Integer createSnapshot() {
        return amount;
    }

    @Override
    protected void readSnapshot(Integer snapshot) {
        amount = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        item.addMana(amount - item.getMana());
    }
}
