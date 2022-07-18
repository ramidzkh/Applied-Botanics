package appbot.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.tags.BlockTags;

import appbot.ABBlocks;

public class BlockTagsProvider extends FabricTagProvider.BlockTagProvider {

    public BlockTagsProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ABBlocks.FLUIX_MANA_POOL);
    }
}
