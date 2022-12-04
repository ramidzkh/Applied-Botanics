package appbot.storage;

import java.util.Iterator;

import com.google.common.collect.Iterators;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import appbot.ae2.ManaHelper;
import vazkii.botania.api.mana.IManaReceiver;

@SuppressWarnings("UnstableApiUsage")
public class ManaStorage extends SnapshotParticipant<Integer> implements Storage<ManaVariant> {

    private final IManaReceiver receiver;
    private int amount;

    public ManaStorage(IManaReceiver receiver) {
        this.receiver = receiver;
        this.amount = receiver.getCurrentMana();
    }

    @Override
    public long insert(ManaVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        var inserted = Math.min(maxAmount, ManaHelper.getCapacity(receiver) - amount);

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

        var extracted = Math.min(maxAmount, amount);

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
                return ManaStorage.this.extract(resource, maxAmount, transaction);
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
                return ManaHelper.getCapacity(receiver);
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
        receiver.receiveMana(amount - receiver.getCurrentMana());
    }
}
