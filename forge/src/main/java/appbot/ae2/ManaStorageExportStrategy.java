package appbot.ae2;

import com.google.common.primitives.Ints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appbot.AppliedBotanics;
import appbot.Lookup;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;

@SuppressWarnings("UnstableApiUsage")
public class ManaStorageExportStrategy implements StackExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManaStorageExportStrategy.class);

    private final Lookup<ManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaStorageExportStrategy(ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var receiver = SafeMana.conv(apiCache.find(fromSide));

        if (receiver == null) {
            return 0;
        }

        var inv = context.getInternalStorage();

        long extracted;
        long wasInserted;

        extracted = StorageHelper.poweredExtraction(
                context.getEnergySource(),
                inv.getInventory(),
                what,
                amount,
                context.getActionSource(),
                Actionable.SIMULATE);

        wasInserted = receiver.insert(Ints.saturatedCast(extracted), Actionable.SIMULATE);

        if (wasInserted > 0) {
            extracted = StorageHelper.poweredExtraction(
                    context.getEnergySource(),
                    inv.getInventory(),
                    what,
                    wasInserted,
                    context.getActionSource(),
                    Actionable.MODULATE);

            wasInserted = receiver.insert(Ints.saturatedCast(extracted), Actionable.MODULATE);

            if (wasInserted < extracted) {
                // Be nice and try to give the overflow back
                long leftover = extracted - wasInserted;
                leftover -= inv.getInventory().insert(what, leftover, Actionable.MODULATE, context.getActionSource());

                if (leftover > 0) {
                    LOGGER.error("Storage export: adjacent block unexpectedly refused insert, voided {} Mana",
                            leftover);
                }
            }
        }

        return wasInserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var receiver = SafeMana.conv(apiCache.find(fromSide));

        if (receiver == null) {
            return 0;
        }

        return receiver.insert(Ints.saturatedCast(amount), mode);
    }
}
