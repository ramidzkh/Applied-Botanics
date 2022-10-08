package appbot.data;

import org.jetbrains.annotations.Nullable;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import appbot.ABItems;
import appbot.AppliedBotanics;
import vazkii.botania.common.lib.ModTags;

import appeng.api.features.P2PTunnelAttunement;

public class ItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider {

    public ItemTagsProvider(DataGenerator p_126530_, BlockTagsProvider p_126531_,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126530_, p_126531_, AppliedBotanics.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(P2PTunnelAttunement.getAttunementTag(ABItems.MANA_P2P_TUNNEL::get))
                .addOptionalTag(ModTags.Items.PETALS.location())
                .addOptionalTag(ModTags.Items.DUSTS_MANA.location());
    }
}
