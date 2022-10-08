package appbot.data;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import appbot.ABItems;
import vazkii.botania.common.lib.ModTags;

import appeng.api.features.P2PTunnelAttunement;

public class ItemTagsProvider extends FabricTagProvider.ItemTagProvider {

    public ItemTagsProvider(FabricDataGenerator dataGenerator, @Nullable BlockTagProvider blockTagProvider) {
        super(dataGenerator, blockTagProvider);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(P2PTunnelAttunement.getAttunementTag(ABItems.MANA_P2P_TUNNEL))
                .forceAddTag(ModTags.Items.PETALS)
                .forceAddTag(ModTags.Items.DUSTS_MANA);
    }
}
