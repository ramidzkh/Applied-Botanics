package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import appbot.AppliedBotanics;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

public class ManaKeyType extends AEKeyType {

    public static final Component MANA = new TranslatableComponent("gui." + AppliedBotanics.MOD_ID + ".mana");

    public static final AEKeyType TYPE = new ManaKeyType();

    private ManaKeyType() {
        super(AppliedBotanics.id("mana"), ManaKey.class, MANA);
    }

    @Nullable
    @Override
    public AEKey readFromPacket(FriendlyByteBuf input) {
        return ManaKey.KEY;
    }

    @Nullable
    @Override
    public AEKey loadKeyFromTag(CompoundTag tag) {
        return ManaKey.KEY;
    }

    @Override
    public int getAmountPerOperation() {
        return 500;
    }

    @Override
    public int getAmountPerByte() {
        return 500;
    }

    @Override
    public int getAmountPerUnit() {
        return 1;
    }

    @Override
    public String getUnitSymbol() {
        return " mana";
    }
}
