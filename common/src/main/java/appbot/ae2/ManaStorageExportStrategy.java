package appbot.ae2;

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

        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return 0;
        }

        var insertable = (int) Math.min(amount,
                ManaHelper.getCapacity(receiver) - receiver.getCurrentMana());
        var extracted = (int) StorageHelper.poweredExtraction(context.getEnergySource(),
                context.getInternalStorage().getInventory(), ManaKey.KEY, insertable, context.getActionSource(),
                Actionable.MODULATE);

        if (extracted > 0) {
            receiver.receiveMana(extracted);
        }

        return extracted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return 0;
        }

        var inserted = (int) Math.min(amount, ManaHelper.getCapacity(receiver) - receiver.getCurrentMana());

        if (inserted > 0 && mode == Actionable.MODULATE) {
            receiver.receiveMana(inserted);
        }

        return inserted;
    }
}
