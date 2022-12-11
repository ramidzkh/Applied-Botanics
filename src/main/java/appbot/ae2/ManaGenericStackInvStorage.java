package appbot.ae2;

import java.util.Optional;

import com.google.common.base.Predicates;
import com.google.common.primitives.Ints;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;

@SuppressWarnings("UnstableApiUsage")
public class ManaGenericStackInvStorage implements ManaReceiver, ManaPool, SparkAttachable {

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
    public int getMaxMana() {
        var slots = 0;

        for (int i = 0; i < inv.size(); i++) {
            var key = inv.getKey(i);

            if (key == null || key == ManaKey.KEY) {
                slots += 1;
            }
        }

        return Ints.saturatedCast(slots * inv.getMaxAmount(ManaKey.KEY));
    }

    @Override
    public Optional<DyeColor> getColor() {
        return Optional.of(DyeColor.PURPLE);
    }

    @Override
    public void setColor(Optional<DyeColor> color) {
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
    public ManaSpark getAttachedSpark() {
        var sparkPos = pos.above();
        var sparks = level.getEntitiesOfClass(Entity.class, new AABB(sparkPos, sparkPos.offset(1, 1, 1)),
                Predicates.instanceOf(ManaSpark.class));

        if (sparks.size() == 1) {
            return (ManaSpark) sparks.get(0);
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
