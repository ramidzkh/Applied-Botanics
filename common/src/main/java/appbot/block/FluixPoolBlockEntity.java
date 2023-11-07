package appbot.block;

import java.util.EnumSet;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import appbot.AppliedBotanics;
import appbot.ae2.ManaKey;
import appbot.mixins.ManaPoolBlockEntityAccessor;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.hooks.ticking.TickHandler;
import appeng.me.InWorldGridNode;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;

public class FluixPoolBlockEntity extends ManaPoolBlockEntity
        implements IInWorldGridNodeHost, IGridConnectedBlockEntity {

    private final ManaPoolBlockEntityAccessor mana = (ManaPoolBlockEntityAccessor) this;
    private final IManagedGridNode mainNode = GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(AppliedBotanics.getInstance().fluixManaPool())
            .setInWorldNode(true)
            .setExposedOnSides(EnumSet.complementOf(EnumSet.of(Direction.UP)))
            .setTagName("proxy");
    private final IActionSource actionSource = IActionSource.ofMachine(mainNode::getNode);

    // work-around for saveAdditional querying the grid
    private boolean saving;

    public FluixPoolBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isFull() {
        var grid = getMainNode().getGrid();

        if (grid == null || !getMainNode().isActive()) {
            return true;
        }

        return grid.getStorageService().getInventory().insert(ManaKey.KEY, 1, Actionable.SIMULATE, actionSource) == 0;
    }

    @Override
    public void receiveMana(int mana) {
        var grid = getMainNode().getGrid();

        if (grid == null || !getMainNode().isActive()) {
            return;
        }

        var storage = grid.getStorageService().getInventory();
        var changed = false;

        if (mana > 0) {
            changed = StorageHelper.poweredInsert(grid.getEnergyService(), storage, ManaKey.KEY, mana,
                    actionSource) != 0;
        } else if (mana < 0) {
            changed = StorageHelper.poweredExtraction(grid.getEnergyService(), storage, ManaKey.KEY, -mana,
                    actionSource) != 0;
        }

        if (changed) {
            setChanged();
            markDispatchable();
        }
    }

    @Override
    public int getCurrentMana() {
        var grid = getMainNode().getGrid();

        if (grid == null || saving) {
            return mana.getMana();
        }

        if (!getMainNode().isActive()) {
            return 0;
        }

        return (int) grid.getStorageService().getInventory().extract(ManaKey.KEY, Integer.MAX_VALUE,
                Actionable.SIMULATE, actionSource);
    }

    @Override
    public int getMaxMana() {
        var grid = getMainNode().getGrid();

        if (grid == null || saving) {
            return super.getMaxMana();
        }

        var oldMana = mana.getMana();
        var oldManaCap = super.getMaxMana();
        long manaCap;

        if (getMainNode().isActive()) {
            var storage = grid.getStorageService().getInventory();
            mana.setMana((int) storage.extract(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, actionSource));
            manaCap = (storage.extract(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, actionSource)
                    + storage.insert(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, actionSource));
        } else {
            mana.setMana(0);
            manaCap = 0;
        }

        if (oldMana != mana.getMana() || oldManaCap != manaCap) {
            setChanged();
            markDispatchable();
        }

        return Ints.saturatedCast(manaCap);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        try {
            saving = true;
            super.saveAdditional(tag);
        } finally {
            saving = false;
        }

        this.getMainNode().saveToNBT(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.getMainNode().loadFromNBT(tag);
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction dir) {
        var node = this.getMainNode().getNode();
        return node instanceof InWorldGridNode inWorldNode && inWorldNode.isExposedOnSide(dir) ? node : null;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return mainNode;
    }

    @Override
    public void saveChanges() {
        if (this.level == null) {
            return;
        }

        // Clientside is marked immediately as dirty as there is no queue processing
        // Serverside is only queued once per tick to avoid costly operations
        // TODO: Evaluate if this is still necessary
        if (this.level.isClientSide) {
            this.setChanged();
        } else {
            this.level.blockEntityChanged(this.worldPosition);
            if (!this.setChangedQueued) {
                TickHandler.instance().addCallable(null, this::setChangedAtEndOfTick);
                this.setChangedQueued = true;
            }
        }
    }

    private boolean setChangedQueued = false;

    private void setChangedAtEndOfTick() {
        this.setChanged();
        this.setChangedQueued = false;
    }

    public void onChunkUnloaded() {
        this.getMainNode().destroy();
    }

    public void onReady() {
        this.getMainNode().create(getLevel(), getBlockPos());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.getMainNode().destroy();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        GridHelper.onFirstTick(this, FluixPoolBlockEntity::onReady);
    }

    public int calculateComparatorLevel() {
        var grid = getMainNode().getGrid();
        long currentMana;
        long freeMana;

        if (grid == null) {
            currentMana = mana.getMana();
            freeMana = super.getMaxMana() - currentMana;
        } else if (!getMainNode().isActive()) {
            currentMana = 0;
            freeMana = 0;
        } else {
            var storage = grid.getStorageService().getInventory();
            currentMana = storage.extract(ManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
            freeMana = storage.insert(ManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
        }

        if (currentMana == 0) {
            return 0;
        }

        // currentMana / (currentMana + freeMana) * 15
        return (int) Math.ceil(1 / (1 + (double) freeMana / currentMana) * 15.0);
    }
}
