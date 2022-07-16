package appbot.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appbot.ABBlocks;
import vazkii.botania.common.block.tile.mana.TilePool;

import appeng.api.networking.*;
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

            for (BlockEntity value : worldChunk.getBlockEntities().values()) {
                if (value instanceof FluixPoolBlockEntity fluixPoolBlockEntity) {
                    entitiesToRemove.add(fluixPoolBlockEntity);
                }
            }

            if (!entitiesToRemove.isEmpty()) {
                TickHandler.instance().addCallable(serverWorld, (world) -> {
                    for (var blockEntity : entitiesToRemove) {
                        blockEntity.getMainNode().destroy();
                    }
                });
            }
        });
    }

    private static final IGridNodeListener<FluixPoolBlockEntity> NODE_LISTENER = new BlockEntityNodeListener<>() {
        @Override
        public void onGridChanged(FluixPoolBlockEntity nodeOwner, IGridNode node) {
            // nodeOwner.gridChanged();
        }
    };

    private final IManagedGridNode mainNode = GridHelper.createManagedNode(this, NODE_LISTENER)
            .setVisualRepresentation(ABBlocks.FLUIX_MANA_POOL)
            .setInWorldNode(true)
            .setExposedOnSides(Set.of(Direction.DOWN))
            .setTagName("proxy");

    public FluixPoolBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        super(pos, state);
    }

    public void securityBreak() {
        this.level.destroyBlock(this.worldPosition, true);
    }

    private boolean setChangedQueued = false;

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

    private void setChangedAtEndOfTick(Level level) {
        this.setChanged();
        this.setChangedQueued = false;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.getMainNode().loadFromNBT(tag);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.getMainNode().saveToNBT(data);
    }

    public final IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Override
    public IGridNode getGridNode(Direction dir) {
        var node = this.getMainNode().getNode();

        // Check if the proxy exposes the node on this side
        if (node != null && node.isExposedOnSide(dir)) {
            return node;
        }

        return null;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.getMainNode().destroy();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        GridHelper.onFirstTick(this, self -> self.getMainNode().create(getLevel(), getBlockPos()));
    }
}
