package appbot.block;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.nbt.CompoundTag;

import appbot.ae2.ManaKey;
import appbot.mixins.MultiCraftingTrackerAccessor;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.helpers.MultiCraftingTracker;
import appeng.util.Platform;

/**
 * Contains behavior for interface blocks and parts, which is independent of the storage channel.
 */
public class FluixPoolLogic implements ICraftingRequester {

    protected final FluixPoolBlockEntity host;
    protected final IManagedGridNode mainNode;
    protected final IActionSource actionSource;
    private final MultiCraftingTracker craftingTracker;

    /**
     * Work planned by {@link #updatePlan()} to be performed by {@link #usePlan}. Positive amounts mean restocking from
     * the network is required while negative amounts mean moving to the network is required.
     */
    private int plannedWork = 0;

    /**
     * Configures how much to stock in this inventory.
     */
    private int config;

    public FluixPoolLogic(IManagedGridNode gridNode, FluixPoolBlockEntity host) {
        this.host = host;
        this.config = 0;
        this.mainNode = gridNode.setFlags(GridFlags.REQUIRE_CHANNEL).addService(IGridTickable.class, new Ticker());
        this.actionSource = IActionSource.ofMachine(mainNode::getNode);

        gridNode.addService(ICraftingRequester.class, this);
        this.craftingTracker = new MultiCraftingTracker(this, 1);
    }

    private void readConfig() {
        updatePlan();
        this.notifyNeighbors();
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putInt("config", this.config);
        this.craftingTracker.writeToNBT(tag);
    }

    public void readFromNBT(CompoundTag tag) {
        this.config = tag.getInt("config");
        this.craftingTracker.readFromNBT(tag);
        this.readConfig();
    }

    public int getConfig() {
        return config;
    }

    public void setConfig(int config) {
        this.config = config;
        readConfig();
    }

    private class Ticker implements IGridTickable {

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(5, 120, !hasWorkToDo(), true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }

            boolean couldDoWork = updateStorage();
            return hasWorkToDo() ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                    : TickRateModulation.SLEEP;
        }
    }

    protected final boolean hasWorkToDo() {
        return this.plannedWork != 0;
    }

    public void notifyNeighbors() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }

        if (host.getLevel() != null) {
            Platform.notifyBlocksOfNeighbors(host.getLevel(), host.getBlockPos());
        }
    }

    public void gridChanged() {
        this.notifyNeighbors();
    }

    private boolean updateStorage() {
        if (plannedWork != 0) {
            return this.usePlan(plannedWork);
        }

        return false;
    }

    private boolean usePlan(int amount) {
        boolean changed = tryUsePlan(amount);

        if (changed) {
            this.updatePlanInner();
        }

        return changed;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        return insert(amount, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    @Nullable
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    /**
     * Check if there's any work to do to get into the state configured by {@link #config} and wake up the machine if
     * necessary.
     */
    public void updatePlan() {
        var hadWork = this.hasWorkToDo();
        this.updatePlanInner();
        var hasWork = this.hasWorkToDo();

        if (hadWork != hasWork) {
            mainNode.ifPresent((grid, node) -> {
                if (hasWork) {
                    grid.getTickManager().alertDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }
    }

    /**
     * Compute the delta between the desired state in {@link #config} and the current contents of the local storage and
     * make a plan on what needs to be changed in {@link #plannedWork}.
     */
    private void updatePlanInner() {
        if (this.config == 0 && this.host.getCurrentMana() != 0) {
            this.plannedWork = -this.host.getCurrentMana();
        } else if (this.config != 0 && this.host.getCurrentMana() != this.host.manaCap) {
            // Not enough stored, request from network
            this.plannedWork = this.config;
        } else {
            // Matches desired state
            this.plannedWork = 0;
        }
    }

    /**
     * Execute on plan made in {@link #updatePlanInner()}
     */
    private boolean tryUsePlan(int amount) {
        var grid = mainNode.getGrid();
        if (grid == null) {
            return false;
        }

        var networkInv = grid.getStorageService().getInventory();
        var energySrc = grid.getEnergyService();

        // Always move out unwanted items before handling crafting or restocking
        if (amount < 0) {
            // Move from interface to network storage
            amount = -amount;

            // Make sure the storage has enough items to execute the plan
            if (this.host.getCurrentMana() < amount) {
                return true; // Replan
            }

            var inserted = (int) StorageHelper.poweredInsert(energySrc, networkInv, ManaKey.KEY, amount,
                    this.actionSource);

            // Remove the items we just injected somewhere else into the network.
            if (inserted > 0) {
                host.receiveMana(-Math.min(host.getCurrentMana(), inserted));
            }

            return inserted > 0;
        }

        if (((MultiCraftingTrackerAccessor) this.craftingTracker).invokeIsBusy(0)) {
            // We are already waiting for a crafting result
            return this.handleCrafting(amount);
        } else if (amount > 0) {
            // Move from network into interface
            // Ensure the plan isn't outdated
            if (insert(amount, Actionable.SIMULATE) != amount) {
                return true;
            }

            // Try to pull the exact item
            if (acquireFromNetwork(energySrc, networkInv, amount)) {
                return true;
            }

            return this.handleCrafting(amount);
        }

        // else wtf?
        return false;
    }

    /**
     * @return true if something was acquired
     */
    private boolean acquireFromNetwork(IEnergyService energySrc, MEStorage networkInv, long amount) {
        var acquired = StorageHelper.poweredExtraction(energySrc, networkInv, ManaKey.KEY, amount, this.actionSource);
        if (acquired > 0) {
            var inserted = insert(acquired, Actionable.MODULATE);
            if (inserted < acquired) {
                throw new IllegalStateException("bad attempt at managing inventory. Voided items: " + inserted);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean handleCrafting(long amount) {
        var grid = mainNode.getGrid();
        if (grid != null) {
            return this.craftingTracker.handleCrafting(0, ManaKey.KEY, amount, this.host.getLevel(),
                    grid.getCraftingService(),
                    this.actionSource);
        }

        return false;
    }

    private long insert(long amount, Actionable actionable) {
        int canInsert = (int) Math.min(amount, host.manaCap - host.getCurrentMana());

        if (actionable == Actionable.MODULATE) {
            host.receiveMana(canInsert);
        }

        return canInsert;
    }
}
