package appbot.ae2;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

import appeng.api.config.Actionable;

public interface SafeMana {

    int insert(int amount, Actionable mode);

    int extract(int amount, Actionable mode);

    static <R extends ManaReceiver & SparkAttachable> int insertExcess(R be, int amount, Actionable mode) {
        amount = Math.min(amount, be.getAvailableSpaceForMana());

        if (mode == Actionable.MODULATE) {
            be.receiveMana(amount);
        }

        return amount;
    }

    @Contract("null->null; !null->!null")
    static @Nullable SafeMana conv(@Nullable ManaReceiver be) {
        if (be == null) {
            return null;
        }

        if (be instanceof SafeMana m) {
            return m;
        }

        return Fail.make(be);
    }
}

class Fail {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeMana.class);
    private static final Set<String> KNOWN = new HashSet<>();

    static SafeMana make(ManaReceiver be) {
        if (KNOWN.add(be.getClass().getName())) {
            LOGGER.error(
                    "Applied Botanics does not know how to insert and extract Mana out of {} ({}). No extraction will be permitted, and insertions could be widely lossy.",
                    be,
                    be.getClass().getName());
        }

        return new SafeMana() {
            @Override
            public int insert(int amount, Actionable mode) {
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
        };
    }
}
