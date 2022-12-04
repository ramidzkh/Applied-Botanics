package appbot.storage;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

@SuppressWarnings("UnstableApiUsage")
public class ManaVariant implements TransferVariant<ManaVariant> {

    public static final ManaVariant VARIANT = new ManaVariant(false);

    private final boolean blank;

    private ManaVariant(boolean blank) {
        this.blank = blank;
    }

    @Override
    public boolean isBlank() {
        return blank;
    }

    @Override
    public ManaVariant getObject() {
        return this;
    }

    @Override
    public @Nullable CompoundTag getNbt() {
        return null;
    }

    @Override
    public CompoundTag toNbt() {
        return new CompoundTag();
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
    }
}
