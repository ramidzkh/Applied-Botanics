package appbot;

import net.minecraft.resources.ResourceLocation;

import appbot.ae2.*;
import appbot.botania.MECorporeaNode;
import appbot.storage.Apis;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.inventories.PartApiLookup;
import appeng.api.stacks.AEKeyTypes;
import appeng.parts.automation.FabricExternalStorageStrategy;
import appeng.parts.automation.StackWorldBehaviors;

@SuppressWarnings("UnstableApiUsage")
public interface AppliedBotanics {

    String MOD_ID = "appbot";

    static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    static void initialize() {
        ABMenus.register();
        ABItems.register();

        AEKeyTypes.register(ManaKeyType.TYPE);
        PartApiLookup.register(Apis.BLOCK, (part, context) -> part.getExposedApi(), ManaP2PTunnelPart.class);

        StackWorldBehaviors.registerImportStrategy(ManaKeyType.TYPE, (level, fromPos, fromSide) -> Reflect
                .newStorageImportStrategy(Apis.BLOCK, ManaVariantConversion.INSTANCE, level, fromPos, fromSide));
        StackWorldBehaviors.registerExportStrategy(ManaKeyType.TYPE, (level, fromPos, fromSide) -> Reflect
                .newStorageExportStrategy(Apis.BLOCK, ManaVariantConversion.INSTANCE, level, fromPos, fromSide));
        StackWorldBehaviors.registerExternalStorageStrategy(ManaKeyType.TYPE,
                (level, fromPos, fromSide) -> new FabricExternalStorageStrategy<>(Apis.BLOCK,
                        ManaVariantConversion.INSTANCE, level, fromPos, fromSide));

        ContainerItemStrategy.register(ManaKeyType.TYPE, ManaKey.class, new ManaContainerItemStrategy());
        GenericSlotCapacities.register(ManaKeyType.TYPE, 500000L);

        CorporeaNodeDetectors.register(MECorporeaNode::getNode);

        P2PTunnelAttunement.registerAttunementTag(ABItems.MANA_P2P_TUNNEL);
    }
}
