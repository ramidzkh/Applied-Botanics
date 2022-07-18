package appbot.block;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import appbot.ABBlocks;
import appbot.ae2.ManaKey;
import vazkii.botania.common.block.tile.mana.TilePool;

import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.hooks.ticking.TickHandler;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;

public class FluixPoolBlockEntity extends TilePool implements IInWorldGridNodeHost, IGridConnectedBlockEntity {

    static {
        /*
         * We need to defer actually unloading tile entities until the end of the tick, after the chunk has been saved
         * to disk. The CHUNK_UNLOAD event runs before the chunk has been saved, and if we disconnect nodes at that
         * point, the saved data will be missing information from the node (such as the player id).
         *
         * From DeferredBlockEntityUnloader
         */
        ServerChunkEvents.CHUNK_UNLOAD.register((serverWorld, worldChunk) -> {
            List<FluixPoolBlockEntity> entitiesToRemove = new ArrayList<>();

            for (var value : worldChunk.getBlockEntities().values()) {
                if (value instanceof FluixPoolBlockEntity fluixPoolBlockEntity) {
                    entitiesToRemove.add(fluixPoolBlockEntity);
                }
            }

            if (!entitiesToRemove.isEmpty()) {
                TickHandler.instance().addCallable(serverWorld, (world) -> {
                    for (var blockEntity : entitiesToRemove) {
                        blockEntity.onChunkUnloaded();
                    }
                });
            }
        });
    }

    private final Accessor mana = (Accessor) this;
    private final IManagedGridNode mainNode = GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(ABBlocks.FLUIX_MANA_POOL)
            .setInWorldNode(true)
            .setExposedOnSides(EnumSet.complementOf(EnumSet.of(Direction.UP)))
            .setTagName("proxy");
    private final IActionSource actionSource = IActionSource.ofMachine(mainNode::getNode);

    public FluixPoolBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isFull() {
        var grid = getMainNode().getGrid();

        if (grid == null || !getMainNode().isActive()) {
            return true;
        }

        return grid.getStorageService().getInventory().extract(ManaKey.KEY, 1, Actionable.SIMULATE, actionSource) == 0;
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

        if (grid == null) {
            return mana.getMana();
        }

        if (!getMainNode().isActive()) {
            return 0;
        }

        return (int) grid.getStorageService().getInventory().extract(ManaKey.KEY, Integer.MAX_VALUE,
                Actionable.SIMULATE, actionSource);
    }

    public void recalculateManaCap() {
        var grid = getMainNode().getGrid();

        if (grid == null) {
            return;
        }

        var oldMana = mana.getMana();
        var oldManaCap = manaCap;

        if (getMainNode().isActive()) {
            var storage = grid.getStorageService().getInventory();
            mana.setMana(
                    Ints.saturatedCast(
                            storage.extract(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, actionSource)));
            manaCap = Ints
                    .saturatedCast(storage.extract(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, actionSource)
                            + storage.insert(ManaKey.KEY, Integer.MAX_VALUE, Actionable.SIMULATE, actionSource));
        } else {
            mana.setMana(0);
            manaCap = 0;
        }

        if (oldMana != mana.getMana() || oldManaCap != manaCap) {
            setChanged();
            markDispatchable();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
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
        return node != null && node.isExposedOnSide(dir) ? node : null;
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
    public void securityBreak() {
        this.level.destroyBlock(this.worldPosition, true);
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

    public interface Accessor {
        int getMana();

        void setMana(int mana);
    }
}
