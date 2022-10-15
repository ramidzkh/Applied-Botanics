package appbot.data;

import org.jetbrains.annotations.Nullable;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import appbot.ABBlocks;
import appbot.AppliedBotanics;

public class BlockTagsProvider extends net.minecraft.data.tags.BlockTagsProvider {

    public BlockTagsProvider(DataGenerator p_126511_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126511_, AppliedBotanics.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ABBlocks.FLUIX_MANA_POOL.get());
    }
}
