package appbot.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ABDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        var blockTagsProvider = new BlockTagsProvider(dataGenerator);
        dataGenerator.addProvider(blockTagsProvider);
        dataGenerator.addProvider(new ItemTagsProvider(dataGenerator, blockTagsProvider));
        dataGenerator.addProvider(new RecipeProvider(dataGenerator));
        dataGenerator.addProvider(new BlockLootTableProvider(dataGenerator));

        dataGenerator.addProvider(new ModelProvider(dataGenerator));
    }
}
