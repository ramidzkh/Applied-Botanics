package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import appbot.storage.ManaVariant;

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
}
