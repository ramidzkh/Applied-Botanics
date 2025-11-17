package appbot.data;

import java.util.List;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import appbot.ABBlocks;

public class BlockLootTableProvider extends BlockLootSubProvider {

    public BlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        dropSelf(ABBlocks.FLUIX_MANA_POOL.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(ABBlocks.FLUIX_MANA_POOL.get());
    }
}
