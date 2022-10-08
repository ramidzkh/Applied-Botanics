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
public record MEManaReceiver(GenericInternalInventory inventory, Level level,
        BlockPos pos) implements IManaReceiver, IManaPool, ISparkAttachable {

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
        var accumulator = 0;

        for (var i = 0; i < inventory.size(); i++) {
            accumulator += inventory.extract(i, ManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE);
        }

        return accumulator;
    }

    @Override
    public boolean isFull() {
        for (var i = 0; i < inventory.size(); i++) {
            if (inventory.insert(i, ManaKey.KEY, 1, Actionable.SIMULATE) != 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void receiveMana(int mana) {
        if (mana > 0) {
            for (var i = 0; i < inventory.size(); i++) {
                mana -= inventory.insert(i, ManaKey.KEY, mana, Actionable.MODULATE);
            }
        } else if (mana < 0) {
            for (var i = 0; i < inventory.size(); i++) {
                mana += inventory.extract(i, ManaKey.KEY, -mana, Actionable.MODULATE);
            }
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isFull();
    }

    @Override
    public boolean canAttachSpark(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        var free = 0;

        for (var i = 0; i < inventory.size(); i++) {
            free += inventory.insert(i, ManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE);
        }

        return free;
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

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public void setColor(DyeColor dyeColor) {
    }
}
