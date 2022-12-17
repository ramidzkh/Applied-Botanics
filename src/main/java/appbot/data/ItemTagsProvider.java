package appbot.data;

import org.jetbrains.annotations.Nullable;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import appbot.ABItems;
import appbot.AppliedBotanics;
import vazkii.botania.common.lib.BotaniaTags;

import appeng.api.features.P2PTunnelAttunement;

public class ItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider {

    public ItemTagsProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagsProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagsProvider, AppliedBotanics.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(P2PTunnelAttunement.getAttunementTag(ABItems.MANA_P2P_TUNNEL.get()))
                .addOptionalTag(BotaniaTags.Items.PETALS.location())
                .addOptionalTag(BotaniaTags.Items.DUSTS_MANA.location());
    }
}
