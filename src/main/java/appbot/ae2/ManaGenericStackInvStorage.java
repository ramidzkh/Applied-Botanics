package appbot.ae2;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.UnknownNullability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;

@SuppressWarnings("UnstableApiUsage")
public class ManaGenericStackInvStorage implements ManaReceiver, ManaPool, SparkAttachable, SafeMana {

    private final Level level;
    private final BlockPos pos;
    private final GenericInternalInventory inv;

    public ManaGenericStackInvStorage(GenericInternalInventory inv, Level level, BlockPos pos) {
        this.inv = inv;
        this.level = level;
        this.pos = pos;
    }

    @Override
    public @UnknownNullability Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return pos;
    }

    @Override
    public int getCurrentMana() {
        return extract(Integer.MAX_VALUE, Actionable.SIMULATE);
    }

    @Override
    public boolean isFull() {
        return insert(1, Actionable.SIMULATE) == 0;
    }

    @Override
    public void receiveMana(int mana) {
        if (mana > 0) {
            insert(mana, Actionable.MODULATE);
        } else if (mana < 0) {
            extract(-mana, Actionable.MODULATE);
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isFull();
    }

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @Override
    public int getMaxMana() {
        var slots = 0;

        for (var i = 0; i < inv.size(); i++) {
            var key = inv.getKey(i);

            if (key == null || key == ManaKey.KEY) {
                slots += 1;
            }
        }

        return Ints.saturatedCast(slots * inv.getMaxAmount(ManaKey.KEY));
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return insert(Integer.MAX_VALUE, Actionable.SIMULATE);
    }

    @Override
    public boolean areIncomingTransfersDone() {
        return !isFull();
    }

    @Override
    public int insert(int amount, Actionable actionable) {
        var inserted = 0L;

        for (var i = 0; i < inv.size() && inserted < amount; i++) {
            inserted += inv.insert(i, ManaKey.KEY, amount - inserted, actionable);
        }

        return (int) inserted;
    }

    @Override
    public int extract(int amount, Actionable actionable) {
        var extracted = 0L;

        for (var i = 0; i < inv.size() && extracted < amount; i++) {
            extracted += inv.extract(i, ManaKey.KEY, amount - extracted, actionable);
        }

        return (int) extracted;
    }
}
