package appbot.fabric.data;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;

import appbot.fabric.ABItems;
import vazkii.botania.common.lib.BotaniaTags;

import appeng.api.features.P2PTunnelAttunement;

public class ItemTagsProvider extends FabricTagProvider.ItemTagProvider {

    public ItemTagsProvider(FabricDataOutput dataGenerator,
            CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(dataGenerator, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        getOrCreateTagBuilder(P2PTunnelAttunement.getAttunementTag(ABItems.MANA_P2P_TUNNEL))
                .forceAddTag(BotaniaTags.Items.PETALS)
                .forceAddTag(BotaniaTags.Items.DUSTS_MANA);
    }
}
