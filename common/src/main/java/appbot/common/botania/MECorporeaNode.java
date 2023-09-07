package appbot.common.botania;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appbot.AB;
import vazkii.botania.api.corporea.CorporeaNode;
import vazkii.botania.api.corporea.CorporeaRequest;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.common.impl.corporea.AbstractCorporeaNode;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.MEStorage;

public class MECorporeaNode extends AbstractCorporeaNode {

    private final IStorageMonitorableAccessor accessor;

    public MECorporeaNode(Level level, BlockPos pos, CorporeaSpark spark, IStorageMonitorableAccessor accessor) {
        super(level, pos, spark);
        this.accessor = accessor;
    }

    @Nullable
    public static CorporeaNode getNode(Level level, CorporeaSpark spark) {
        var accessor = AB.getInstance().meStorage((ServerLevel) level, spark.getAttachPos()).find(Direction.UP);

        if (accessor != null) {
            var storage = accessor.getInventory(IActionSource.empty());

            if (storage != null) {
                return new MECorporeaNode(level, spark.getAttachPos(), spark, accessor);
            }
        }

        return null;
    }

    @Override
    public List<ItemStack> countItems(CorporeaRequest request) {
        return work(request, false);
    }

    @Override
    public List<ItemStack> extractItems(CorporeaRequest request) {
        return work(request, true);
    }

    protected List<ItemStack> work(CorporeaRequest request, boolean execute) {
        var list = new ArrayList<ItemStack>();
        MEStorage storage;
        IActionSource source;

        if (request.getEntity()instanceof Player player) {
            storage = accessor.getInventory(source = IActionSource.ofPlayer(player));
        } else {
            storage = accessor.getInventory(source = IActionSource.empty());
        }

        if (storage == null) {
            return list;
        }

        for (var entry : storage.getAvailableStacks()) {
            var amount = Ints.saturatedCast(entry.getLongValue());

            if (entry.getKey()instanceof AEItemKey itemKey) {
                var stack = itemKey.toStack();

                if (request.getMatcher().test(stack)) {
                    request.trackFound(amount);
                    var remainder = Math.min(amount,
                            request.getStillNeeded() == -1 ? amount : request.getStillNeeded());

                    if (remainder > 0) {
                        request.trackSatisfied(remainder);

                        if (execute) {
                            if (!getSpark().isCreative()) {
                                remainder = (int) storage.extract(entry.getKey(), remainder, Actionable.MODULATE,
                                        source);
                            }

                            getSpark().onItemExtracted(stack);
                            request.trackExtracted(remainder);
                        }

                        while (remainder > 0) {
                            var taken = Math.min(remainder, stack.getMaxStackSize());
                            remainder -= taken;
                            list.add(itemKey.toStack(taken));
                        }
                    }
                }
            }
        }

        return list;
    }
}
