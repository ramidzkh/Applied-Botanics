package appbot.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appbot.ABBlocks;
import appbot.AppliedBotanics;

public class BlockTagsProvider extends net.neoforged.neoforge.common.data.BlockTagsProvider {

    public BlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, AppliedBotanics.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ABBlocks.FLUIX_MANA_POOL.get());
    }
}
