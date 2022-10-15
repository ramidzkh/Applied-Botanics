package appbot;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import appbot.block.FluixPool;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.mana.BlockPool;

public class ABBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            AppliedBotanics.MOD_ID);

    public static final RegistryObject<Block> FLUIX_MANA_POOL = BLOCKS.register("fluix_mana_pool",
            () -> new FluixPool(BlockPool.Variant.FABULOUS, BlockBehaviour.Properties.copy(ModBlocks.fabulousPool)));

    public static void initialize(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
