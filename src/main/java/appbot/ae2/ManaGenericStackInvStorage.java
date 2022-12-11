package appbot.ae2;

import com.google.common.base.Predicates;

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

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;

@SuppressWarnings("UnstableApiUsage")
public class ManaGenericStackInvStorage implements IManaReceiver, IManaPool, ISparkAttachable {

    private final Level level;
    private final BlockPos pos;
    private final GenericInternalInventory inv;

    public ManaGenericStackInvStorage(GenericInternalInventory inv, Level level, BlockPos pos) {
        this.inv = inv;
        this.level = level;
        this.pos = pos;
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
        return extract(Integer.MAX_VALUE, Actionable.SIMULATE);
    }

    @Override
    public boolean isFull() {
        return insert(1, Actionable.SIMULATE) != 0;
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
        return insert(Integer.MAX_VALUE, Actionable.SIMULATE);
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

    public int insert(int amount, Actionable actionable) {
        var inserted = 0;

        for (var i = 0; i < inv.size() && inserted < amount; ++i) {
            inserted += (int) inv.insert(i, ManaKey.KEY, amount - inserted, actionable);
        }

        return inserted;
    }

    private int extract(int amount, Actionable actionable) {
        var extracted = 0;

        for (var i = 0; i < inv.size() && extracted < amount; ++i) {
            extracted += (int) inv.extract(i, ManaKey.KEY, amount - extracted, actionable);
        }

        return extracted;
    }
}
