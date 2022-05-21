package appbot.storage;

import com.google.common.base.Predicates;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;

import appeng.util.Platform;

@SuppressWarnings("UnstableApiUsage")
public class ManaReceiver implements IManaReceiver, IManaPool, ISparkAttachable {

    private final Level level;
    private final BlockPos pos;
    private final Storage<ManaVariant> storage;

    public ManaReceiver(Level level, BlockPos pos, Storage<ManaVariant> storage) {
        this.level = level;
        this.pos = pos;
        this.storage = storage;
    }

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return pos;
    }

    @Override
    public int getCurrentMana() {
        return (int) storage.simulateExtract(ManaVariant.VARIANT, Integer.MAX_VALUE, Transaction.getCurrentUnsafe());
    }

    @Override
    public boolean isFull() {
        return (int) storage.simulateInsert(ManaVariant.VARIANT, 1, Transaction.getCurrentUnsafe()) == 0;
    }

    @Override
    public void receiveMana(int mana) {
        if (mana > 0) {
            try (var transaction = Platform.openOrJoinTx()) {
                storage.insert(ManaVariant.VARIANT, mana, transaction);
                transaction.commit();
            }
        } else if (mana < 0) {
            try (var transaction = Platform.openOrJoinTx()) {
                storage.extract(ManaVariant.VARIANT, -mana, transaction);
                transaction.commit();
            }
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
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public void setColor(DyeColor color) {
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return (int) (Integer.MAX_VALUE
                - storage.simulateInsert(ManaVariant.VARIANT, Integer.MAX_VALUE, Transaction.getCurrentUnsafe()));
    }

    @Override
    public IManaSpark getAttachedSpark() {
        var sparkPos = pos.above();
        var sparks = level.getEntitiesOfClass(Entity.class, new AABB(sparkPos, sparkPos.offset(1, 1, 1)),
                Predicates.instanceOf(IManaSpark.class));

        if (sparks.size() == 1) {
            return (IManaSpark) sparks.get(0);
        }

        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return !isFull();
    }
}
