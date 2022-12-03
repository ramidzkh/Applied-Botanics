package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import appbot.storage.ManaVariant;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.IVariantConversion;

@SuppressWarnings("UnstableApiUsage")
public enum ManaVariantConversion implements IVariantConversion<ManaVariant> {

    INSTANCE;

    @Override
    public AEKeyType getKeyType() {
        return ManaKeyType.TYPE;
    }

    @Override
    public ManaVariant getVariant(@Nullable AEKey key) {
        return key != null && key.getType() == getKeyType() ? ManaVariant.VARIANT : ManaVariant.BLANK;
    }

    @Nullable
    @Override
    public AEKey getKey(ManaVariant variant) {
        return variant.isBlank() ? null : ManaKey.KEY;
    }

    @Override
    public long getBaseSlotSize(ManaVariant variant) {
        return 500000;
    }

    public static int getCapacity(IManaReceiver receiver) {
        if (receiver instanceof IManaCollector collector) {
            return collector.getMaxMana();
        } else if (receiver instanceof ISparkAttachable sparkAttachable) {
            return receiver.getCurrentMana() + sparkAttachable.getAvailableSpaceForMana();
        } else if (!receiver.isFull()) {
            return 1000;
        }

        return 0;
    }
}
