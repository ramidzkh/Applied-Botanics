package appbot.botania;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.corporea.ICorporeaNode;
import vazkii.botania.api.corporea.ICorporeaRequest;
import vazkii.botania.api.corporea.ICorporeaSpark;
import vazkii.botania.common.impl.corporea.AbstractCorporeaNode;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import appeng.me.helpers.BaseActionSource;

public class MECorporeaNode extends AbstractCorporeaNode {

    private final MEStorage storage;
    private final BaseActionSource source;

    public MECorporeaNode(Level level, BlockPos pos, ICorporeaSpark spark, MEStorage storage, BaseActionSource source) {
        super(level, pos, spark);
        this.storage = storage;
        this.source = source;
    }

    @Nullable
    public static ICorporeaNode getNode(Level level, ICorporeaSpark spark) {
        var blockEntity = level.getBlockEntity(spark.getAttachPos());

        if (blockEntity == null) {
            return null;
        }

        var accessor = blockEntity.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, Direction.UP).orElse(null);

        if (accessor != null) {
            var source = new BaseActionSource();
            var storage = accessor.getInventory(source);

            if (storage != null) {
                return new MECorporeaNode(level, spark.getAttachPos(), spark, storage, source);
            }
        }

        return null;
    }

    @Override
    public List<ItemStack> countItems(ICorporeaRequest request) {
        return work(request, false);
    }

    @Override
    public List<ItemStack> extractItems(ICorporeaRequest request) {
        return work(request, true);
    }

    protected List<ItemStack> work(ICorporeaRequest request, boolean execute) {
        var list = new ArrayList<ItemStack>();

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

                        itemKey.addDrops(remainder, list, null, null);
                    }
                }
            }
        }

        return list;
    }
}
