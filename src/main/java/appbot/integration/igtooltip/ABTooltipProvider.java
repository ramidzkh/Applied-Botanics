package appbot.integration.igtooltip;

import net.minecraft.resources.ResourceLocation;

import appbot.block.FluixPool;
import appbot.block.FluixPoolBlockEntity;

import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipProvider;
import appeng.integration.modules.igtooltip.DebugProvider;
import appeng.integration.modules.igtooltip.TooltipIds;
import appeng.integration.modules.igtooltip.blocks.GridNodeStateDataProvider;
import appeng.integration.modules.igtooltip.blocks.PowerStorageDataProvider;

@SuppressWarnings("UnstableApiUsage")
public class ABTooltipProvider implements TooltipProvider {
    @Override
    public void registerCommon(CommonRegistration registration) {
        // Jade synchronizes server data providers by ID. These must not reuse
        // AE2's IDs for a different block entity class.
        registration.addBlockEntityData(ResourceLocation.fromNamespaceAndPath("appbot", "grid_node"),
                FluixPoolBlockEntity.class, new GridNodeStateDataProvider());
        registration.addBlockEntityData(ResourceLocation.fromNamespaceAndPath("appbot", "power_storage"),
                FluixPoolBlockEntity.class, new PowerStorageDataProvider());
        registration.addBlockEntityData(ResourceLocation.fromNamespaceAndPath("appbot", "debug"),
                FluixPoolBlockEntity.class, DebugProvider::provideBlockEntityData);
    }

    @Override
    public void registerClient(ClientRegistration registration) {
        registration.addBlockEntityBody(FluixPoolBlockEntity.class, FluixPool.class,
                TooltipIds.POWER_STORAGE, new PowerStorageDataProvider());
        registration.addBlockEntityBody(FluixPoolBlockEntity.class, FluixPool.class,
                TooltipIds.GRID_NODE_STATE, new GridNodeStateDataProvider());
        registration.addBlockEntityBody(FluixPoolBlockEntity.class, FluixPool.class,
                TooltipIds.DEBUG, DebugProvider::provideBlockEntityBody, 5000);
    }
}
