package appbot.ae2;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;

public class ManaP2PTunnelPart extends CapabilityP2PTunnelPart<ManaP2PTunnelPart, IManaReceiver> {

    private static final P2PModels MODELS = new P2PModels("part/mana_p2p_tunnel");

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
                    if (!get(guard).isFull()) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public void receiveMana(int mana) {
            var outputs = getOutputs();

            if (outputs.isEmpty()) {
                return;
            }

            var manaForEach = mana / outputs.size();

            for (var output : outputs) {
                try (var guard = output.getAdjacentCapability()) {
                    get(guard).receiveMana(manaForEach);
                }
            }
        }

        @Override
        public boolean canReceiveManaFromBursts() {
            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    if (get(guard).canReceiveManaFromBursts()) {
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

    private static final MethodHandle GET = Util.make(() -> {
        try {
            var get = CapabilityGuard.class.getDeclaredMethod("get");
            get.setAccessible(true);
            return MethodHandles.lookup().unreflect(get);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    });

    private IManaReceiver get(CapabilityGuard guard) {
        try {
            return (IManaReceiver) (Object) GET.invokeExact(guard);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
