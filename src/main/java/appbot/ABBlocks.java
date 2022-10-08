package appbot;

import static appbot.AppliedBotanics.id;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import appbot.block.FluixPool;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.mana.BlockPool;

public class ABBlocks {

    public static final Block FLUIX_MANA_POOL = Registry.register(Registry.BLOCK, id("fluix_mana_pool"),
            new FluixPool(BlockPool.Variant.FABULOUS, BlockBehaviour.Properties.copy(ModBlocks.fabulousPool)));

    public static void register() {
    }
}
