package appbot.data;

import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class ABDataGenerator {

    public static void onGatherData(GatherDataEvent event) {
        var dataGenerator = event.getGenerator();
        var existingFileHelper = event.getExistingFileHelper();

        var blockTagsProvider = new BlockTagsProvider(dataGenerator, existingFileHelper);
        dataGenerator.addProvider(blockTagsProvider);
        dataGenerator.addProvider(new ItemTagsProvider(dataGenerator, blockTagsProvider, existingFileHelper));
        dataGenerator.addProvider(new RecipeProvider(dataGenerator));

        dataGenerator.addProvider(new ItemModelProvider(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(new BlockModelProvider(dataGenerator, existingFileHelper));

        // dropSelf pool
    }
}
