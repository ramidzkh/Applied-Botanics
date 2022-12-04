package appbot.ae2;

import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;

public class ManaHelper {

    public static int getCapacity(IManaReceiver receiver) {
        if (receiver instanceof IManaCollector collector) {
            return collector.getMaxMana();
        } else if (receiver instanceof ISparkAttachable sparkAttachable) {
            return receiver.getCurrentMana() + sparkAttachable.getAvailableSpaceForMana();
        } else if (!receiver.isFull()) {
            return 1000;
        }

        return 0;
    }
}
