package appbot.ae2;

import com.google.common.primitives.Ints;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appbot.AppliedBotanics;
import appbot.Lookup;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.core.AELog;

@SuppressWarnings("UnstableApiUsage")
public class ManaStorageImportStrategy implements StackImportStrategy {

    private final Lookup<ManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaStorageImportStrategy(ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(ManaKeyType.TYPE)) {
            return false;
        }

        if (context.isInFilter(ManaKey.KEY) == context.isInverted()) {
            return false;
        }

        var receiver = SafeMana.conv(apiCache.find(fromSide));

        if (receiver == null) {
            return false;
        }

        var amountPerOperation = ManaKeyType.TYPE.getAmountPerOperation();
        long maxTransfer = context.getOperationsRemaining()
                * (long) amountPerOperation;

        if (maxTransfer <= 0) {
            return false;
        }

        // see how much we could take
        var inventory = context.getInternalStorage().getInventory();
        var simulate = inventory.insert(ManaKey.KEY, maxTransfer, Actionable.SIMULATE, context.getActionSource());

        // take up to that much
        var extracted = receiver.extract(Ints.saturatedCast(simulate), Actionable.MODULATE);

        // insert to network
        var inserted = inventory.insert(ManaKey.KEY, extracted, Actionable.MODULATE, context.getActionSource());

        if (inserted < extracted) {
            // try to give back overflow
            var difference = extracted - inserted;
            difference -= receiver.insert(Ints.saturatedCast(difference), Actionable.MODULATE);

            if (difference != 0) {
                AELog.warn("Extracted %d Mana from adjacent storage and voided it because network refused insert");
            }
        }

        context.reduceOperationsRemaining((inserted + amountPerOperation - 1) / amountPerOperation);
        return false;
    }
}
