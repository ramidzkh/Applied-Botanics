package appbot.fabric.data;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import appbot.fabric.ABItems;
import vazkii.botania.common.lib.BotaniaTags;

import appeng.api.features.P2PTunnelAttunement;

public class ItemTagsProvider extends FabricTagProvider.ItemTagProvider {

    public ItemTagsProvider(FabricDataGenerator dataGenerator, @Nullable BlockTagProvider blockTagProvider) {
        super(dataGenerator, blockTagProvider);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(P2PTunnelAttunement.getAttunementTag(ABItems.MANA_P2P_TUNNEL))
                .forceAddTag(BotaniaTags.Items.PETALS)
                .forceAddTag(BotaniaTags.Items.DUSTS_MANA);
    }
}
