package appbot.ae2;

import java.util.List;

import org.jetbrains.annotations.UnknownNullability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import appbot.AppliedBotanics;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AEConfig;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.P2PTunnelPart;

public class ManaP2PTunnelPart extends P2PTunnelPart<ManaP2PTunnelPart> implements ManaReceiver {

    private static final double MANA_RETAINED = 0.95;
    private static final int EXTRA_TICKS_EXISTED = 1;

    private static final float[] ROTX, ROTY;
    private static final P2PModels MODELS = new P2PModels(AppliedBotanics.id("part/mana_p2p_tunnel"));

    static {
        ROTX = new float[6];
        ROTY = new float[6];

        ROTY[Direction.DOWN.get3DDataValue()] = -90;
        ROTY[Direction.UP.get3DDataValue()] = 90;

        ROTX[Direction.DOWN.get3DDataValue()] = -90;
        ROTX[Direction.UP.get3DDataValue()] = -90;
        ROTX[Direction.NORTH.get3DDataValue()] = 0;
        ROTX[Direction.SOUTH.get3DDataValue()] = -180;
        ROTX[Direction.WEST.get3DDataValue()] = -90;
        ROTX[Direction.EAST.get3DDataValue()] = -270;
    }

    public ManaP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
    }

    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    // <editor-fold desc="Fake ManaReceiver">
    @Override
    public @UnknownNullability Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockEntity().getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return 0;
    }

    @Override
    public boolean isFull() {
        return !isActive() || isOutput() || getOutputStream().noneMatch(AEBasePart::isActive);
    }

    @Override
    public void receiveMana(int mana) {
        // voids mana, we want bursts only
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isFull();
    }
    // </editor-fold>

    public void receiveManaFromBurst(ManaBurstEntity burst) {
        var node = getMainNode().getNode();
        int mana = burst.getMana();

        if (node == null || isFull() || mana <= 0) {
            return;
        }

        var outputs = getOutputStream().filter(AEBasePart::isActive).toList();
        if (outputs.isEmpty())
            return;

        var output = outputs.get(getLevel().getRandom().nextInt(outputs.size()));

        var costFactor = AEConfig.instance().getP2PTunnelTransportTax();

        if (costFactor > 0) {
            var energised = node.getGrid().getEnergyService().extractAEPower(mana * costFactor, Actionable.MODULATE,
                    PowerMultiplier.ONE) / costFactor;
            mana = (int) (energised * MANA_RETAINED);
        } else {
            mana = (int) (mana * MANA_RETAINED);
        }

        if (mana <= 0) {
            return;
        }

        var level = output.getLevel();
        var dir = output.getSide();
        var pos = output.getBlockEntity().getBlockPos();

        int old = burst.getColor();
        int rainbow = Mth.hsvToRgb(level.getGameTime() * 2 % 360 / 360F, 1F, 1F);
        int newColor = FastColor.ARGB32.lerp(0.4f, old, rainbow);

        var newBurst = new ManaBurstEntity(BotaniaEntities.MANA_BURST, level);
        var at = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
        newBurst.moveTo(at.x, at.y, at.z, ROTX[dir.get3DDataValue()], ROTY[dir.get3DDataValue()]);
        newBurst.setDeltaMovement(ManaBurstEntity.calculateBurstVelocity(newBurst.getXRot(), newBurst.getYRot()));
        newBurst.setColor(newColor);
        newBurst.setMana(mana);
        newBurst.setStartingMana(burst.getStartingMana());
        newBurst.setMinManaLoss(burst.getMinManaLoss());
        newBurst.setManaLossPerTick(burst.getManaLossPerTick());
        newBurst.setGravity(burst.getBurstGravity());
        burst.getBurstSourcePosition().ifPresent(newBurst::setBurstSourcePosition);
        newBurst.setSourceLens(burst.getSourceLens());
        newBurst.setTicksExisted(burst.getTicksExisted() + ManaP2PTunnelPart.EXTRA_TICKS_EXISTED);

        level.addFreshEntity(newBurst);
        level.playSound(null, pos, BotaniaSounds.spreaderFire, SoundSource.BLOCKS, 0.05F,
                0.7F + 0.3F * (float) Math.random());
    }
}
