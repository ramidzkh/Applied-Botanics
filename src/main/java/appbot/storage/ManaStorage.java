package appbot.storage;

import java.util.Iterator;

import com.google.common.collect.Iterators;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;

@SuppressWarnings("UnstableApiUsage")
public class ManaStorage extends SnapshotParticipant<Integer> implements Storage<ManaVariant> {

    private final IManaReceiver receiver;
    private int amount;

    public ManaStorage(IManaReceiver receiver) {
        this.receiver = receiver;
        this.amount = receiver.getCurrentMana();
    }

    private int getCapacity() {
        if (receiver instanceof IManaCollector collector) {
            return collector.getMaxMana();
        } else if (receiver instanceof ISparkAttachable sparkAttachable) {
            return receiver.getCurrentMana() + sparkAttachable.getAvailableSpaceForMana();
        } else if (!receiver.isFull()) {
            return 1000;
        }

        return 0;
    }

    @Override
    public long insert(ManaVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        long inserted = Math.min(maxAmount, getCapacity() - amount);

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
                return receiver.getCurrentMana();
            }

            @Override
            public long getCapacity() {
                return ManaStorage.this.getCapacity();
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
        System.out.println("commit delta=" + (amount - receiver.getCurrentMana()));
        receiver.receiveMana(amount - receiver.getCurrentMana());
    }
}
