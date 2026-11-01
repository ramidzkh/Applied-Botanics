package appbot.ae2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3d;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import appbot.AppliedBotanics;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.client.render.cablebus.P2PTunnelFrequencyModelData;
import appeng.core.AEConfig;
import appeng.items.parts.PartModels;
import appeng.items.tools.MemoryCardItem;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.P2PModels;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public class ManaP2PTunnelPart extends AEBasePart implements ManaReceiver, SafeMana {

    private static final double MANA_RETAINED = 0.95;
    private static final int EXTRA_TICKS_EXISTED = 1;

    private static final P2PModels MODELS = new P2PModels(AppliedBotanics.id("part/mana_p2p_tunnel"));

    private short freq;
    private boolean output;
    private byte spin;

    public ManaP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(2.0f);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        freq = data.getShort("freq");
        output = data.getBoolean("output");
        spin = data.getByte("spin");
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putShort("freq", freq);
        data.putBoolean("output", output);
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
        } else if (InteractionUtil.isInAlternateUseMode(player)) {
            if (!isClientSide()) {
                this.output = !this.output;
                this.getHost().markForUpdate();
                this.getHost().markForSave();
            }

            player.playSound(SoundEvents.LEVER_CLICK);
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

    public boolean isOutput() {
        return output;
    }

    // a fake ManaReceiver so spreaders still fire at us
    // <editor-fold>
    @Override
    public int insert(int amount, Actionable mode) {
        return 0;
    }

    @Override
    public int extract(int amount, Actionable mode) {
        return 0;
    }

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
        for (var part : getGroup()) {
            if (part != ManaP2PTunnelPart.this && part.isOutput()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void receiveMana(int mana) {
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isFull();
    }
    // </editor-fold>

    public void receiveManaFromBurst(ManaBurstEntity burst, BlockHitResult hit) {
        var node = getMainNode().getNode();
        var mana = burst.getMana();

        if (node == null || mana <= 0) {
            return;
        }

        var outputs = new ArrayList<>(getGroup());
        outputs.removeIf(x -> !(x != this && x.isOutput()));
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
        level.playSound(null, moveTo.x, moveTo.y, moveTo.z, BotaniaSounds.SPREADER_FIRE, SoundSource.BLOCKS, 0.05F,
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

    public Collection<ManaP2PTunnelPart> getGroup() {
        if (this.getMainNode().isOnline()) {
            var grid = getMainNode().getGrid();

            if (grid != null) {
                return ManaP2PService.get(grid).getGroup(freq);
            }
        }

        return List.of();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(5, 5, 12, 11, 11, 13);
        bch.addBox(3, 3, 13, 13, 13, 14);
        bch.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        var changed = super.readFromStream(data);
        var oldFreq = this.freq;
        this.freq = data.readShort();
        return changed || oldFreq != this.freq;
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeShort(this.freq);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (isClientSide() || hand == InteractionHand.OFF_HAND) {
            return false;
        }

        // Prefer restoring from memory card
        if (heldItem.getItem() instanceof IMemoryCard mc) {
            if (InteractionUtil.isInAlternateUseMode(player)) {
                var oldFreq = freq;
                var newFreq = freq;

                var grid = getMainNode().getGrid();

                if (grid != null && oldFreq == 0) {
                    var p2p = ManaP2PService.get(grid);
                    newFreq = p2p.newFrequency();
                    p2p.updateFreq(this, newFreq);
                }

                MemoryCardItem.clearCard(heldItem);
                heldItem.set(AEComponents.EXPORTED_SETTINGS_SOURCE, getPartItem().asItem().getDescription());
                heldItem.applyComponents(exportSettings(SettingsFrom.MEMORY_CARD));

                mc.notifyUser(player,
                        oldFreq != newFreq ? MemoryCardMessages.SETTINGS_RESET : MemoryCardMessages.SETTINGS_SAVED);
                return true;
            } else {
                if (heldItem.get(AEComponents.EXPORTED_P2P_TYPE) != getPartItem().asItem()) {
                    mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                    return false;
                }

                importSettings(SettingsFrom.MEMORY_CARD, heldItem.getComponents(), player);
                mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                return true;
            }
        }

        return false;
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        var frequency = input.get(AEComponents.EXPORTED_P2P_FREQUENCY);

        if (frequency != null && frequency != this.freq) {
            var grid = getMainNode().getGrid();

            if (grid != null) {
                ManaP2PService.get(grid).updateFreq(this, frequency);
            } else {
                setFrequency(frequency); // Remember it for when we actually join the grid
            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        super.exportSettings(mode, builder);

        if (mode == SettingsFrom.MEMORY_CARD && freq != 0) {
            builder.set(AEComponents.EXPORTED_P2P_TYPE, getPartItem().asItem());
            builder.set(AEComponents.EXPORTED_P2P_FREQUENCY, freq);

            var colors = Platform.p2p().toColors(freq);
            // the P2P freq only has 4 colors, so we stretch em out a bit
            builder.set(AEComponents.MEMORY_CARD_COLORS, new MemoryCardColors(
                    colors[0], colors[0], colors[1], colors[1],
                    colors[2], colors[2], colors[3], colors[3]));
        }
    }

    public short getFrequency() {
        return freq;
    }

    public void setFrequency(short freq) {
        this.freq = freq;

        this.getHost().markForSave();
        this.getHost().markForUpdate();
    }

    @Override
    public ModelData getModelData() {
        var ret = Short.toUnsignedLong(this.freq);

        if (this.isActive() && this.isPowered()) {
            ret |= 0x10000L;
        }

        return ModelData.builder()
                .with(P2PTunnelFrequencyModelData.FREQUENCY, ret)
                .build();
    }
}
