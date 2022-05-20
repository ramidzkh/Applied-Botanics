package appbot.ae2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import appbot.AppliedBotanics;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
import appeng.parts.p2p.P2PModels;

public class ManaP2PTunnelPart extends CapabilityP2PTunnelPart<ManaP2PTunnelPart, IManaReceiver> {

    private static final P2PModels MODELS = new P2PModels(AppliedBotanics.id("part/mana_p2p_tunnel"));
    private final ISparkAttachable sparkAttachable = new ISparkAttachable() {
        @Override
        public boolean canAttachSpark(ItemStack stack) {
            return true;
        }

        @Override
        public int getAvailableSpaceForMana() {
            int space = 0;

            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    var receiver = guard.get();

                    if (receiver instanceof ISparkAttachable sparkAttachable) {
                        space += sparkAttachable.getAvailableSpaceForMana();
                    } else if (receiver instanceof IManaCollector collector) {
                        space += collector.getMaxMana() - receiver.getCurrentMana();
                    } else if (!receiver.isFull()) {
                        // guess lol
                        space += 1000;
                    }
                }
            }

            return space;
        }

        @Override
        public IManaSpark getAttachedSpark() {
            var sparkPos = getHost().getLocation().getPos().above();
            var sparks = getLevel().getEntitiesOfClass(Entity.class, new AABB(sparkPos, sparkPos.offset(1, 1, 1)),
                    Predicates.instanceOf(IManaSpark.class));

            if (sparks.size() == 1) {
                return (IManaSpark) sparks.get(0);
            }

            return null;
        }

        @Override
        public boolean areIncomingTranfersDone() {
            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    var receiver = guard.get();

                    if (receiver.canReceiveManaFromBursts() && !receiver.isFull()) {
                        return true;
                    }
                }
            }

            return false;
        }
    };

    public ManaP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, BotaniaFabricCapabilities.MANA_RECEIVER);
        inputHandler = new InputHandler();
        outputHandler = emptyHandler = new EmptyHandler();
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nullable
    public ISparkAttachable getSparkAttachable() {
        return isOutput() ? null : sparkAttachable;
    }

    private class InputHandler implements IManaReceiver, IManaPool {
        @Override
        public Level getManaReceiverLevel() {
            return getLevel();
        }

        @Override
        public BlockPos getManaReceiverPos() {
            return getHost().getLocation().getPos();
        }

        @Override
        public int getCurrentMana() {
            return 0;
        }

        @Override
        public boolean isFull() {
            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    if (!guard.get().isFull()) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public void receiveMana(int mana) {
            var outputs = getOutputStream()
                    .filter(part -> {
                        try (var guard = part.getAdjacentCapability()) {
                            var receiver = guard.get();

                            return receiver.canReceiveManaFromBursts() && !receiver.isFull();
                        }
                    })
                    .collect(Collectors.toList());

            if (outputs.isEmpty()) {
                return;
            }

            Collections.shuffle(outputs);

            queueTunnelDrain(PowerUnits.AE, mana / 100D);
            var manaForEach = mana / outputs.size();
            var spill = mana % outputs.size();

            for (var output : outputs) {
                try (var guard = output.getAdjacentCapability()) {
                    guard.get().receiveMana(manaForEach + (spill-- > 0 ? 1 : 0));
                }
            }
        }

        @Override
        public boolean canReceiveManaFromBursts() {
            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    if (guard.get().canReceiveManaFromBursts()) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isOutputtingPower() {
            return false;
        }

        @Override
        public DyeColor getColor() {
            return DyeColor.PURPLE;
        }

        @Override
        public void setColor(DyeColor color) {
        }
    }

    private class EmptyHandler implements IManaReceiver {
        @Override
        public Level getManaReceiverLevel() {
            return getLevel();
        }

        @Override
        public BlockPos getManaReceiverPos() {
            return getHost().getLocation().getPos();
        }

        @Override
        public int getCurrentMana() {
            return 0;
        }

        @Override
        public boolean isFull() {
            return true;
        }

        @Override
        public void receiveMana(int mana) {
        }

        @Override
        public boolean canReceiveManaFromBursts() {
            return false;
        }
    }
}
