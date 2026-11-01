package appbot.integration.igtooltip;

import appbot.ae2.ManaP2PTunnelPart;
import appbot.block.FluixPool;
import appbot.block.FluixPoolBlockEntity;

import appeng.api.integrations.igtooltip.BaseClassRegistration;
import appeng.api.integrations.igtooltip.PartTooltips;
import appeng.api.integrations.igtooltip.TooltipProvider;

@SuppressWarnings("UnstableApiUsage")
public class ABTooltipProvider implements TooltipProvider {

    static {
        PartTooltips.addBody(ManaP2PTunnelPart.class, new ManaP2PDataProvider());
        PartTooltips.addServerData(ManaP2PTunnelPart.class, new ManaP2PDataProvider());
    }

    @Override
    public void registerBlockEntityBaseClasses(BaseClassRegistration registration) {
        registration.addBaseBlockEntity(FluixPoolBlockEntity.class, FluixPool.class);
    }
}
