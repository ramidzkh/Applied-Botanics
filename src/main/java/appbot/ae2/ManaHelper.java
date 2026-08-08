package appbot.ae2;

import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;

public class ManaHelper {

    public static int getCapacity(ManaReceiver receiver) {
        if (receiver instanceof ManaPool pool) {
            return pool.getMaxMana();
        } else if (receiver instanceof ManaCollector collector) {
            return collector.getMaxMana();
        } else if (receiver instanceof ManaSparkAttachable ManaSparkAttachable) {
            return receiver.getCurrentMana() + ManaSparkAttachable.getAvailableSpaceForMana();
        } else if (!receiver.isFull()) {
            return receiver.getCurrentMana() + 1000;
        }

        return 0;
    }
}
