package appbot.ae2.storage;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appbot.ae2.ManaKey;
import appbot.ae2.ManaVariantConversion;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.mana.IManaReceiver;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;

public class ManaStorageExportStrategy implements StackExportStrategy {

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

        var insertable = (int) Math.min(amount,
                ManaVariantConversion.getCapacity(receiver) - receiver.getCurrentMana());
        var extracted = (int) StorageHelper.poweredExtraction(context.getEnergySource(),
                context.getInternalStorage().getInventory(), ManaKey.KEY, insertable, context.getActionSource(), mode);

        if (extracted > 0 && mode == Actionable.MODULATE) {
            receiver.receiveMana(extracted);
        }

        return extracted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return 0;
        }

        var inserted = (int) Math.min(amount, ManaVariantConversion.getCapacity(receiver) - receiver.getCurrentMana());

        if (inserted > 0 && mode == Actionable.MODULATE) {
            receiver.receiveMana(inserted);
        }

        return inserted;
    }
}
