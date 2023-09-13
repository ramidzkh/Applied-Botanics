package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import appbot.AppliedBotanics;
import appbot.Lookup;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;

@SuppressWarnings("UnstableApiUsage")
public class ManaExternalStorageStrategy implements ExternalStorageStrategy {

    private final Lookup<ManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaExternalStorageStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            // If receiver is absent, never query again until the next update.
            return null;
        }

        return new ManaStorageAdapter(receiver, injectOrExtractCallback);
    }

    private record ManaStorageAdapter(ManaReceiver receiver, Runnable injectOrExtractCallback) implements MEStorage {

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof ManaKey)) {
                return 0;
            }

            var inserted = (int) Math.min(amount,
                    ManaHelper.getCapacity(receiver) - receiver.getCurrentMana());

            if (inserted > 0 && mode == Actionable.MODULATE) {
                receiver.receiveMana(inserted);
                injectOrExtractCallback.run();
            }

            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof ManaKey)) {
                return 0;
            }

            var extracted = (int) Math.min(amount, receiver.getCurrentMana());

            if (extracted > 0 && mode == Actionable.MODULATE) {
                receiver.receiveMana(-extracted);
                injectOrExtractCallback.run();
            }

            return extracted;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            var currentMana = receiver.getCurrentMana();

            if (currentMana != 0) {
                out.add(ManaKey.KEY, currentMana);
            }
        }

        @Override
        public Component getDescription() {
            return GuiText.ExternalStorage.text(ManaKeyType.TYPE.getDescription());
        }
    }
}
