package appbot.ae2;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import appbot.AppliedBotanics;
import vazkii.botania.common.block.mana.ManaPoolBlock;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

public class ManaKeyType extends AEKeyType {

    public static final Component MANA = Component.translatable("gui." + AppliedBotanics.MOD_ID + ".mana");

    public static final AEKeyType TYPE = new ManaKeyType();
    private static final MapCodec<? extends AEKey> CODEC = MapCodec.unit(ManaKey.KEY);

    private ManaKeyType() {
        super(AppliedBotanics.id("mana"), ManaKey.class, MANA);
    }

    @Override
    public MapCodec<? extends AEKey> codec() {
        return CODEC;
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
    public @Nullable AEKey readFromPacket(RegistryFriendlyByteBuf input) {
        return ManaKey.KEY;
    }

    @Override
    public int getAmountPerUnit() {
        return ManaPoolBlock.MAX_MANA;
    }

    @Override
    public @Nullable String getUnitSymbol() {
        return "pool";
    }
}
