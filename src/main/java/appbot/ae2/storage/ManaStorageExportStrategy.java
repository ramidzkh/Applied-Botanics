package appbot.ae2.storage;

import com.google.common.primitives.Ints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appbot.ae2.ManaVariantConversion;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.mana.IManaReceiver;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;

public class ManaStorageExportStrategy implements StackExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManaStorageExportStrategy.class);
    private final BlockApiCache<IManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaStorageExportStrategy(ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = BlockApiCache.create(BotaniaFabricCapabilities.MANA_RECEIVER, level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount, Actionable mode) {
        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return 0;
        }

        var extracted = StorageHelper.poweredExtraction(context.getEnergySource(),
                context.getInternalStorage().getInventory(), what, amount, context.getActionSource(), mode);
        var inserted = Ints.saturatedCast(
                Math.min(extracted, ManaVariantConversion.getCapacity(receiver) - receiver.getCurrentMana()));

        if (mode == Actionable.MODULATE) {
            if (inserted > 0) {
                receiver.receiveMana(inserted);
            }

            if (inserted < extracted) {
                LOGGER.error("Storage export issue, voided {}x{}", extracted - inserted, what);
            }
        }

        return inserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return 0;
        }

        var inserted = Ints
                .saturatedCast(
                        Math.min(amount, ManaVariantConversion.getCapacity(receiver) - receiver.getCurrentMana()));

        if (inserted > 0 && mode == Actionable.MODULATE) {
            receiver.receiveMana(inserted);
        }

        return inserted;
    }
}
