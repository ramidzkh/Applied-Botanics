package appbot.ae2;

import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

public class ManaHelper {

    public static int getCapacity(ManaReceiver receiver) {
        if (receiver instanceof ManaPool pool) {
            return pool.getMaxMana();
        } else if (receiver instanceof ManaCollector collector) {
            return collector.getMaxMana();
        } else if (receiver instanceof SparkAttachable sparkAttachable) {
            return receiver.getCurrentMana() + sparkAttachable.getAvailableSpaceForMana();
        } else if (!receiver.isFull()) {
            return 1000;
        }

        return 0;
    }
}
