package appbot;

import net.minecraft.resources.ResourceLocation;

import appbot.ae2.*;
import appbot.ae2.ManaExternalStorageStrategy;
import appbot.ae2.ManaGenericStackInvStorage;
import appbot.ae2.ManaStorageExportStrategy;
import appbot.ae2.ManaStorageImportStrategy;
import appbot.botania.MECorporeaNode;
import appbot.item.cell.CreativeManaCellHandler;
import appbot.item.cell.ManaCellHandler;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.inventories.PartApiLookup;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import appeng.parts.automation.StackWorldBehaviors;

@SuppressWarnings("UnstableApiUsage")
public interface AppliedBotanics {

    String MOD_ID = "appbot";

    static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

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
    }
}
