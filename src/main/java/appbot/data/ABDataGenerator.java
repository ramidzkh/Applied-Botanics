package appbot.data;

import java.util.List;
import java.util.Set;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;

public class ABDataGenerator {

    public static void onInitializeDataGenerator(GatherDataEvent event) {
        var gen = event.getGenerator();
        var output = gen.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        var blockTagsProvider = gen.addProvider(event.includeServer(),
                new BlockTagsProvider(output, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(),
                new ItemTagsProvider(output, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        gen.addProvider(event.includeServer(), new RecipeProvider(output));
        gen.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), List
                .of(new LootTableProvider.SubProviderEntry(BlockLootTableProvider::new, LootContextParamSets.BLOCK))));

        gen.addProvider(event.includeClient(), new ModelProvider(output, existingFileHelper));
    }
}
