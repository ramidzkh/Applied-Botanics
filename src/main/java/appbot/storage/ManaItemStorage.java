package appbot.storage;

import java.util.Iterator;

import com.google.common.collect.Iterators;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.util.Unit;

import vazkii.botania.api.BotaniaFabricCapabilities;

@SuppressWarnings("UnstableApiUsage")
public class ManaItemStorage extends SnapshotParticipant<ItemVariant> implements Storage<ManaVariant> {

    private final ContainerItemContext context;
    private ItemVariant item;

    public ManaItemStorage(ContainerItemContext context, ItemVariant item) {
        this.context = context;
        this.item = item;
    }

    @Override
    public long insert(ManaVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        updateSnapshots(transaction);

        try (var inner = transaction.openNested()) {
            if (context.extract(item, 1, transaction) != 0) {
                var stack = item.toStack(1);
                var item = BotaniaFabricCapabilities.MANA_ITEM.find(stack, Unit.INSTANCE);

                if (item == null) {
                    return 0;
                }

                var inserted = (int) Math.min(maxAmount, item.getMaxMana() - item.getMana());

                if (inserted > 0) {
                    item.addMana(inserted);
                    var variant = ItemVariant.of(stack);
                    context.insert(variant, 1, transaction);
                    this.item = variant;
                    inner.commit();
                    return inserted;
                }
            }
        }

        return 0;
    }

    @Override
    public long extract(ManaVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        updateSnapshots(transaction);

        try (var inner = transaction.openNested()) {
            if (context.extract(item, 1, transaction) != 0) {
                var stack = item.toStack(1);
                var item = BotaniaFabricCapabilities.MANA_ITEM.find(stack, Unit.INSTANCE);

                if (item == null || item.isNoExport()) {
                    return 0;
                }

                var extracted = (int) Math.min(maxAmount, item.getMaxMana());

                if (extracted > 0) {
                    item.addMana(-extracted);
                    var variant = ItemVariant.of(stack);
                    context.insert(variant, 1, transaction);
                    this.item = variant;
                    inner.commit();
                    return extracted;
                }
            }
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
                var item = BotaniaFabricCapabilities.MANA_ITEM.find(context.getItemVariant().toStack(), Unit.INSTANCE);
                return item == null ? 0 : item.getMana();
            }

            @Override
            public long getCapacity() {
                var item = BotaniaFabricCapabilities.MANA_ITEM.find(context.getItemVariant().toStack(), Unit.INSTANCE);
                return item == null ? 0 : item.getMaxMana();
            }
        });
    }

    @Override
    protected ItemVariant createSnapshot() {
        return item;
    }

    @Override
    protected void readSnapshot(ItemVariant snapshot) {
        item = snapshot;
    }
}
