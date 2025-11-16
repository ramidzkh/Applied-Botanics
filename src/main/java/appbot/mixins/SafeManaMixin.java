package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;

import appbot.ae2.ManaHelper;
import appbot.ae2.SafeMana;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.block.block_entity.*;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSplitterBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import vazkii.botania.common.block.block_entity.mana.PowerGeneratorBlockEntity;
import vazkii.botania.common.block.mana.ManaVoidBlock;

import appeng.api.config.Actionable;

@Mixin(ManaPoolBlockEntity.class)
class Pool implements SafeMana {
    @Override
    public int insert(int amount, Actionable mode) {
        var be = (ManaPoolBlockEntity) (Object) this;
        return SafeMana.insertExcess(be, amount, mode);
    }

    @Override
    public int extract(int amount, Actionable mode) {
        var be = (ManaPoolBlockEntity) (Object) this;

        if (mode == Actionable.SIMULATE) {
            return Math.min(amount, be.getCurrentMana());
        }

        var old = be.getCurrentMana();
        be.receiveMana(-amount);
        return old - be.getCurrentMana();
    }
}

@Mixin(TerrestrialAgglomerationPlateBlockEntity.class)
class TAPBE implements SafeMana {
    @Override
    public int insert(int amount, Actionable mode) {
        var be = (TerrestrialAgglomerationPlateBlockEntity) (Object) this;
        return SafeMana.insertExcess(be, amount, mode);
    }

    @Override
    public int extract(int amount, Actionable mode) {
        // be.receiveMana(-Math.min(amount, old));
        return 0;
    }
}

@Mixin({ ManaSpreaderBlockEntity.class, PowerGeneratorBlockEntity.class, LifeImbuerBlockEntity.class,
        RunicAltarBlockEntity.class, AvatarBlockEntity.class, BreweryBlockEntity.class,
        ManaEnchanterBlockEntity.class })
class ClampedAboveInsertOnly implements SafeMana {
    @Override
    public int insert(int amount, Actionable mode) {
        var be = (ManaReceiver) this;

        if (mode == Actionable.SIMULATE) {
            return Math.min(amount, ManaHelper.getCapacity(be) - be.getCurrentMana());
        }

        var old = be.getCurrentMana();
        be.receiveMana(amount);
        return be.getCurrentMana() - old;
    }

    @Override
    public int extract(int amount, Actionable mode) {
        return 0;
    }
}

@Mixin({ ManaSplitterBlockEntity.class, ManaVoidBlock.ManaReceiverImpl.class })
class FullPassthroughInsertOnly implements SafeMana {
    @Override
    public int insert(int amount, Actionable mode) {
        var be = (ManaReceiver) this;

        if (mode == Actionable.SIMULATE) {
            return Math.min(amount, ManaHelper.getCapacity(be) - be.getCurrentMana());
        }

        if (be.isFull()) {
            return 0;
        }

        be.receiveMana(amount);
        return amount;
    }

    @Override
    public int extract(int amount, Actionable mode) {
        return 0;
    }
}
