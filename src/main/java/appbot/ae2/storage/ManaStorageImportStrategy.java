package appbot.ae2.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;
import appbot.ae2.ManaVariantConversion;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.mana.IManaReceiver;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;

public class ManaStorageImportStrategy implements StackImportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManaStorageImportStrategy.class);
    private final BlockApiCache<IManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaStorageImportStrategy(ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = BlockApiCache.create(BotaniaFabricCapabilities.MANA_RECEIVER, level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(ManaKeyType.TYPE)) {
            return false;
        }

        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return false;
        }

        var remainingTransferAmount = context.getOperationsRemaining()
                * (long) ManaKeyType.TYPE.getAmountPerOperation();

        var inv = context.getInternalStorage();

        var amount = (int) Math.min(remainingTransferAmount, receiver.getCurrentMana());

        if (amount > 0) {
            receiver.receiveMana(-amount);
        }

        var inserted = inv.getInventory().insert(ManaKey.KEY, amount, Actionable.MODULATE, context.getActionSource());

        if (inserted < amount) {
            var leftover = amount - inserted;
            var backfill = (int) Math.min(leftover,
                    ManaVariantConversion.getCapacity(receiver) - receiver.getCurrentMana());

            if (backfill > 0) {
                receiver.receiveMana(backfill);
            }

            if (leftover > backfill) {
                LOGGER.error("Storage import issue, voided {} mana", leftover - backfill);
            }
        }

        var opsUsed = Math.max(1, inserted / ManaKeyType.TYPE.getAmountPerOperation());
        context.reduceOperationsRemaining(opsUsed);

        return amount > 0;
    }
}
