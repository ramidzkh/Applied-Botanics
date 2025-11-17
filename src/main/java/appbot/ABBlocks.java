package appbot;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import appbot.block.FluixPool;

public class ABBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AppliedBotanics.MOD_ID);

    public static final DeferredBlock<Block> FLUIX_MANA_POOL = BLOCKS.register("fluix_mana_pool", FluixPool::new);

    public static void initialize(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
