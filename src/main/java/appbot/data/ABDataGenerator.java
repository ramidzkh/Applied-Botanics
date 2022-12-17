package appbot.data;

import net.minecraftforge.data.event.GatherDataEvent;

public class ABDataGenerator {

    public static void onGatherData(GatherDataEvent event) {
        var dataGenerator = event.getGenerator();
        var existingFileHelper = event.getExistingFileHelper();

        var blockTagsProvider = new BlockTagsProvider(dataGenerator, existingFileHelper);
        dataGenerator.addProvider(true, blockTagsProvider);
        dataGenerator.addProvider(true, new ItemTagsProvider(dataGenerator, blockTagsProvider, existingFileHelper));
        dataGenerator.addProvider(true, new RecipeProvider(dataGenerator));

        dataGenerator.addProvider(true, new ItemModelProvider(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(true, new BlockModelProvider(dataGenerator, existingFileHelper));

        dataGenerator.addProvider(true, new BlockLootTableProvider(dataGenerator.getOutputFolder()));
    }
}
