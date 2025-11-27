package appbot.ae2;

import java.util.List;

import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3d;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import appbot.AppliedBotanics;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AEConfig;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.InteractionUtil;

public class ManaP2PTunnelPart extends P2PTunnelPart<ManaP2PTunnelPart> implements ManaReceiver, SafeMana {

    private static final double MANA_RETAINED = 0.95;
    private static final int EXTRA_TICKS_EXISTED = 1;

    private static final P2PModels MODELS = new P2PModels(AppliedBotanics.id("part/mana_p2p_tunnel"));

    private byte spin;

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

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        spin = data.getByte("spin");
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putByte("spin", spin);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (InteractionUtil.canWrenchRotate(player.getInventory().getSelected())) {
            if (!isClientSide()) {
                this.spin = (byte) ((this.spin + 1) % 4);
                this.getHost().markForUpdate();
                this.getHost().markForSave();
            }
            return true;
        } else {
            return super.onUseWithoutItem(player, pos);
        }
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);

        if (getSide().getAxis() == Direction.Axis.Y) {
            this.spin = (byte) (Mth.floor(player.getYRot() * 4F / 360F + 2.5D) & 3);
        }
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

    @Override
    public int insert(int amount, Actionable mode) {
        return 0;
    }

    @Override
    public int extract(int amount, Actionable mode) {
        return 0;
    }
    // </editor-fold>

    public void receiveManaFromBurst(ManaBurstEntity burst, BlockHitResult hit) {
        var node = getMainNode().getNode();
        var mana = burst.getMana();

        if (node == null || isFull() || mana <= 0) {
            return;
        }

        var outputs = getOutputStream().filter(AEBasePart::isActive).toList();
        if (outputs.isEmpty())
            return;

        var input = this;
        var output = outputs.get(getLevel().getRandom().nextInt(outputs.size()));

        Vec3 moveTo, directionTo;
        float xrot, yrot;

        {
            var originInput = getFaceCentre(input.getBlockEntity().getBlockPos(), input.getSide());
            var originOutput = getFaceCentre(output.getBlockEntity().getBlockPos(), output.getSide());

            var r = onb(output.getSide(), output.getOrientation())
                    .mul(onb(input.getSide(), input.getOrientation()).transpose());

            moveTo = new Vec3(hit.getLocation().toVector3f().sub(originInput).mul(r).add(originOutput));
            directionTo = new Vec3(burst.getDeltaMovement().toVector3f().mul(r)).scale(-1);

            var xz = Math.sqrt(directionTo.x * directionTo.x + directionTo.z * directionTo.z);
            xrot = Mth.wrapDegrees((float) -(Mth.atan2(directionTo.y, xz) * Mth.RAD_TO_DEG));
            yrot = Mth.wrapDegrees((float) (Mth.atan2(directionTo.z, directionTo.x) * Mth.RAD_TO_DEG) - 90.0F);
        }

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

        var old = burst.getColor();
        var rainbow = Mth.hsvToRgb(level.getGameTime() * 2 % 360 / 360F, 1F, 1F);
        var newColor = FastColor.ARGB32.lerp(0.4f, old, rainbow);

        var newBurst = new ManaBurstEntity(BotaniaEntities.MANA_BURST, level);
        newBurst.moveTo(moveTo.x, moveTo.y, moveTo.z, yrot, xrot);
        newBurst.setDeltaMovement(directionTo);
        newBurst.setColor(newColor);
        newBurst.setMana(mana);
        newBurst.setStartingMana(burst.getStartingMana());
        newBurst.setMinManaLoss(burst.getMinManaLoss());
        newBurst.setManaLossPerTick(burst.getManaLossPerTick());
        newBurst.setGravity(burst.getBurstGravity());
        burst.getBurstSourcePosition().ifPresent(newBurst::setBurstSourcePosition);
        newBurst.setSourceLens(burst.getSourceLens());
        newBurst.setTicksExisted(burst.getTicksExisted() + ManaP2PTunnelPart.EXTRA_TICKS_EXISTED);
        newBurst.setShooterUUID(burst.getShooterUUID());

        level.addFreshEntity(newBurst);
        level.playSound(null, moveTo.x, moveTo.y, moveTo.z, BotaniaSounds.spreaderFire, SoundSource.BLOCKS, 0.05F,
                0.7F + 0.3F * (float) Math.random());
    }

    private static Vector3f getFaceCentre(BlockPos pos, Direction dir) {
        return new Vector3f(pos.getX() + (float) (1 + dir.getStepX()) / 2,
                pos.getY() + (float) (1 + dir.getStepY()) / 2, pos.getZ() + (float) (1 + dir.getStepZ()) / 2);
    }

    private static Matrix3d onb(Direction a, Direction b) {
        var m = new Matrix3d();
        var va = a.getNormal();
        var vb = b.getNormal();
        var vc = va.cross(vb);
        m.m00 = va.getX();
        m.m01 = va.getY();
        m.m02 = va.getZ();
        m.m10 = vb.getX();
        m.m11 = vb.getY();
        m.m12 = vb.getZ();
        m.m20 = vc.getX();
        m.m21 = vc.getY();
        m.m22 = vc.getZ();
        return m;
    }

    private Direction getOrientation() {
        return BlockOrientation.get(getSide(), spin).rotate(Direction.UP);
    }
}
