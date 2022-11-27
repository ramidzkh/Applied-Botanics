package appbot.ae2;

import java.util.Set;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.common.block.tile.mana.TilePool;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.me.storage.ExternalStorageFacade;
import appeng.parts.automation.HandlerStrategy;

public class ManaHandlerStrategy extends HandlerStrategy<IManaReceiver, Object> {

    public static final ManaHandlerStrategy INSTANCE = new ManaHandlerStrategy();

    private ManaHandlerStrategy() {
        super(ManaKeyType.TYPE);
    }

    @Override
    public ExternalStorageFacade getFacade(IManaReceiver handler) {
        return new ExternalStorageFacade() {
            @Override
            public int getSlots() {
                return 1;
            }

            @Override
            public GenericStack getStackInSlot(int slot) {
                return new GenericStack(ManaKey.KEY, handler.getCurrentMana());
            }

            @Override
            public AEKeyType getKeyType() {
                return ManaKeyType.TYPE;
            }

            @Override
            protected int insertExternal(AEKey what, int amount, Actionable mode) {
                return Ints.saturatedCast(ManaHandlerStrategy.this.insert(handler, what, amount, mode));
            }

            @Override
            protected int extractExternal(AEKey what, int amount, Actionable mode) {
                if (!(what instanceof ManaKey)) {
                    return 0;
                }

                if (mode == Actionable.MODULATE) {
                    var before = handler.getCurrentMana();
                    handler.receiveMana(-Ints.saturatedCast(amount));
                    return Math.max(0, before - handler.getCurrentMana());
                }

                return Math.min(amount, handler.getCurrentMana());
            }

            @Override
            public boolean containsAnyFuzzy(Set<AEKey> keys) {
                if (handler.getCurrentMana() == 0) {
                    return false;
                }

                for (var key : keys) {
                    if (key instanceof ManaKey) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public void getAvailableStacks(KeyCounter out) {
                var currentMana = handler.getCurrentMana();

                if (currentMana != 0) {
                    out.add(ManaKey.KEY, currentMana);
                }
            }
        };
    }

    @Nullable
    @Override
    public Object getStack(AEKey what, long amount) {
        return null;
    }

    @Override
    public long insert(IManaReceiver handler, AEKey what, long amount, Actionable mode) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            var before = handler.getCurrentMana();
            handler.receiveMana(Ints.saturatedCast(amount));
            return Math.max(0, handler.getCurrentMana() - before);
        }

        var space = 0;

        if (handler instanceof TilePool pool) {
            space += pool.getAvailableSpaceForMana();
        } else if (handler instanceof ISparkAttachable sparkAttachable) {
            space += sparkAttachable.getAvailableSpaceForMana();
        } else if (handler instanceof IManaCollector collector) {
            space += collector.getMaxMana() - handler.getCurrentMana();
        } else if (!handler.isFull()) {
            // guess lol
            space += 1000;
        }

        return Math.min(amount, space);
    }
}
