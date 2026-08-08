package appbot.botania;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;

public final class BotaniaCapabilityLookup {

    public static final BlockCapability<ManaReceiver, Direction> MANA_RECEIVER = BlockCapability
            .create(ManaReceiver.ID, ManaReceiver.class, Direction.class);
    public static final BlockCapability<ManaSparkAttachable, Void> MANA_SPARK_ATTACHABLE = BlockCapability
            .create(ManaSparkAttachable.ID, ManaSparkAttachable.class, Void.class);
    public static final ItemCapability<ManaItem, Void> MANA_ITEM = ItemCapability
            .create(ManaItem.ID, ManaItem.class, Void.class);

    private BotaniaCapabilityLookup() {
    }
}
