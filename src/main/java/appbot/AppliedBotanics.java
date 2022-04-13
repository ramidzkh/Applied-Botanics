package appbot;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import appbot.ae2.ManaP2PTunnelPart;
import appbot.botania.MECorporeaNode;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.inventories.PartApiLookup;
import appeng.api.parts.PartModels;
import appeng.core.CreativeTab;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;

public interface AppliedBotanics {

    String MOD_ID = "applied-botanics";

    PartItem<?> MANA_P2P_TUNNEL = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ManaP2PTunnelPart.class));
        return Registry.register(Registry.ITEM, id("mana_p2p_tunnel"),
                new PartItem<>(new Item.Properties().tab(CreativeTab.INSTANCE), ManaP2PTunnelPart.class,
                        ManaP2PTunnelPart::new));
    });

    static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    static void initialize() {
        PartApiLookup.register(BotaniaFabricCapabilities.MANA_RECEIVER, (part, context) -> part.getExposedApi(),
                ManaP2PTunnelPart.class);
        PartApiLookup.register(BotaniaFabricCapabilities.SPARK_ATTACHABLE, (part, context) -> part.getSparkAttachable(),
                ManaP2PTunnelPart.class);
        CorporeaNodeDetectors.register(MECorporeaNode::getNode);
        P2PTunnelAttunement.addItemByMod(BotaniaAPI.MODID, MANA_P2P_TUNNEL);
    }
}
