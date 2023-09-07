package appbot.fabric;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

import appbot.common.ae2.*;
import appbot.common.block.FluixPoolBlockEntity;
import appbot.common.botania.MECorporeaNode;
import appbot.common.item.cell.CreativeManaCellHandler;
import appbot.common.item.cell.ManaCellHandler;
import appbot.fabric.ae2.ManaP2PTunnelPart;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.inventories.PartApiLookup;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import appeng.hooks.ticking.TickHandler;
import appeng.parts.automation.StackWorldBehaviors;

@SuppressWarnings("UnstableApiUsage")
public interface AppliedBotanics {

    static void initialize() {
        ABMenus.register();
        ABBlocks.register();
        ABItems.register();

        AEKeyTypes.register(ManaKeyType.TYPE);
        PartApiLookup.register(BotaniaFabricCapabilities.MANA_RECEIVER, (part, context) -> part.getExposedApi(),
                ManaP2PTunnelPart.class);
        PartApiLookup.register(BotaniaFabricCapabilities.SPARK_ATTACHABLE, (part, context) -> part.getSparkAttachable(),
                ManaP2PTunnelPart.class);

        BotaniaFabricCapabilities.MANA_RECEIVER.registerFallback((world, pos, state, blockEntity, context) -> {
            // Fall back to generic inv
            var genericInv = GenericInternalInventory.SIDED.find(world, pos, state, blockEntity, context);
            if (genericInv != null) {
                return new ManaGenericStackInvStorage(genericInv, world, pos);
            }
            return null;
        });

        BotaniaFabricCapabilities.SPARK_ATTACHABLE.registerFallback((world, pos, state, blockEntity, context) -> {
            // Fall back to generic inv
            var genericInv = GenericInternalInventory.SIDED.find(world, pos, state, blockEntity, context);
            if (genericInv != null) {
                return new ManaGenericStackInvStorage(genericInv, world, pos);
            }
            return null;
        });

        StackWorldBehaviors.registerImportStrategy(ManaKeyType.TYPE, ManaStorageImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(ManaKeyType.TYPE, ManaStorageExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(ManaKeyType.TYPE, ManaExternalStorageStrategy::new);

        ContainerItemStrategy.register(ManaKeyType.TYPE, ManaKey.class, new ManaContainerItemStrategy());
        GenericSlotCapacities.register(ManaKeyType.TYPE, 500000L);

        StorageCells.addCellHandler(ManaCellHandler.INSTANCE);
        StorageCells.addCellHandler(new CreativeManaCellHandler());

        CorporeaNodeDetectors.register(MECorporeaNode::getNode);

        P2PTunnelAttunement.registerAttunementTag(ABItems.MANA_P2P_TUNNEL);

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
                TickHandler.instance().addCallable(serverWorld, world -> {
                    for (var blockEntity : entitiesToRemove) {
                        blockEntity.onChunkUnloaded();
                    }
                });
            }
        });
    }
}
