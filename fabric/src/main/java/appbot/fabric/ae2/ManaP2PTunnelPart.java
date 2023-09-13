package appbot.fabric.ae2;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import appbot.ae2.ManaHelper;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
import appeng.parts.p2p.P2PModels;

public class ManaP2PTunnelPart extends CapabilityP2PTunnelPart<ManaP2PTunnelPart, ManaReceiver> {

    private static final P2PModels MODELS = new P2PModels(AppliedBotanics.id("part/mana_p2p_tunnel"));
    private final SparkAttachable sparkAttachable = new P2PSparkAttachable();

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
    public SparkAttachable getSparkAttachable() {
        return isOutput() ? null : sparkAttachable;
    }

    private class P2PSparkAttachable implements SparkAttachable {

        @Override
        public boolean canAttachSpark(ItemStack stack) {
            return true;
        }

        @Override
        public int getAvailableSpaceForMana() {
            var space = 0;

            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    var receiver = guard.get();
                    space += ManaHelper.getCapacity(receiver);
                }
            }

            return space;
        }

        @Override
        public ManaSpark getAttachedSpark() {
            var sparkPos = getHost().getLocation().getPos().above();
            var sparks = getLevel().getEntitiesOfClass(Entity.class, new AABB(sparkPos, sparkPos.offset(1, 1, 1)),
                    Predicates.instanceOf(ManaSpark.class));

            if (sparks.size() == 1) {
                return (ManaSpark) sparks.get(0);
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
    }

    private class InputHandler implements ManaReceiver, ManaPool {

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
            return getOutputStream()
                    .map(part -> {
                        try (var guard = part.getAdjacentCapability()) {
                            return guard.get().getCurrentMana();
                        }
                    })
                    .reduce(0, Integer::sum);
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
                    var result = guard.get();

                    if (result.canReceiveManaFromBursts()) {
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
        public int getMaxMana() {
            return getOutputStream()
                    .map(part -> {
                        try (var guard = part.getAdjacentCapability()) {
                            return ManaHelper.getCapacity(guard.get());
                        }
                    })
                    .reduce(0, Integer::sum);
        }

        @Override
        public Optional<DyeColor> getColor() {
            return Optional.of(DyeColor.PURPLE);
        }

        @Override
        public void setColor(Optional<DyeColor> color) {
        }
    }

    private class EmptyHandler implements ManaReceiver {

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
