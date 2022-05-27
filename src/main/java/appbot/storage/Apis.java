package appbot.storage;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;

import appbot.AppliedBotanics;
import vazkii.botania.api.BotaniaFabricCapabilities;

@SuppressWarnings("UnstableApiUsage")
public class Apis {

    public static final BlockApiLookup<Storage<ManaVariant>, Direction> BLOCK = BlockApiLookup
            .get(AppliedBotanics.id("mana"), Storage.asClass(), Direction.class);

    public static final ItemApiLookup<Storage<ManaVariant>, ContainerItemContext> ITEM = ItemApiLookup
            .get(AppliedBotanics.id("mana"), Storage.asClass(), ContainerItemContext.class);

    static {
        var lock = new Object();
        var lockSafety = new ThreadLocal<>();

        BLOCK.registerFallback((world, pos, state, blockEntity, context) -> {
            if (lockSafety.get() == null) {
                lockSafety.set(lock);
                var receiver = BotaniaFabricCapabilities.MANA_RECEIVER.find(world, pos, state, blockEntity, context);
                lockSafety.set(null);

                if (receiver != null) {
                    return new ManaStorage(receiver);
                }
            }

            return null;
        });

        ITEM.registerFallback((itemStack, context) -> {
            if (lockSafety.get() == null) {
                lockSafety.set(lock);
                var item = BotaniaFabricCapabilities.MANA_ITEM.find(itemStack, Unit.INSTANCE);
                lockSafety.set(null);

                if (item != null) {
                    return new ManaItemStorage(context, ItemVariant.of(itemStack));
                }
            }

            return null;
        });

        BotaniaFabricCapabilities.MANA_RECEIVER.registerFallback((world, pos, state, blockEntity, context) -> {
            if (lockSafety.get() == null) {
                lockSafety.set(lock);
                var storage = BLOCK.find(world, pos, state, blockEntity, context);
                lockSafety.set(null);

                if (storage != null) {
                    return new ManaReceiver(world, pos, storage);
                }
            }

            return null;
        });

        BotaniaFabricCapabilities.SPARK_ATTACHABLE.registerFallback((world, pos, state, blockEntity, context) -> {
            if (lockSafety.get() == null) {
                lockSafety.set(lock);
                var storage = BLOCK.find(world, pos, state, blockEntity, context);
                lockSafety.set(null);

                if (storage != null) {
                    return new ManaReceiver(world, pos, storage);
                }
            }

            return null;
        });
    }
}
