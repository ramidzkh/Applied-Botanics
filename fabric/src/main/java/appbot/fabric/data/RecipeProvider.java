package appbot.fabric.data;

import static appbot.AppliedBotanics.id;

import java.util.Locale;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;

import appbot.fabric.ABItems;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class RecipeProvider extends FabricRecipeProvider {

    public RecipeProvider(FabricDataOutput dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> exporter) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ABItems.FLUIX_MANA_POOL)
                .requires(BotaniaBlocks.fabulousPool)
                .requires(AEBlocks.INTERFACE)
                .unlockedBy("has_interface", has(AEBlocks.INTERFACE))
                .save(exporter, id("fluix_mana_pool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ABItems.MANA_CELL_HOUSING)
                .pattern("QSQ")
                .pattern("S S")
                .pattern("III")
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .define('S', BotaniaItems.lifeEssence)
                .define('I', BotaniaItems.manaSteel)
                .unlockedBy("has_life_essence", has(BotaniaItems.lifeEssence))
                .save(exporter, id("mana_cell_housing"));

        for (var tier : ABItems.Tier.values()) {
            var cellComponent = switch (tier) {
                case _1K -> AEItems.CELL_COMPONENT_1K;
                case _4K -> AEItems.CELL_COMPONENT_4K;
                case _16K -> AEItems.CELL_COMPONENT_16K;
                case _64K -> AEItems.CELL_COMPONENT_64K;
                case _256K -> AEItems.CELL_COMPONENT_256K;
            };

            var tierName = tier.toString().toLowerCase(Locale.ROOT);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ABItems.get(tier))
                    .requires(ABItems.MANA_CELL_HOUSING)
                    .requires(cellComponent)
                    .unlockedBy("has_cell_component" + tierName, has(cellComponent))
                    .save(exporter);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ABItems.getPortable(tier))
                    .requires(AEBlocks.CHEST)
                    .requires(cellComponent)
                    .requires(AEBlocks.ENERGY_CELL)
                    .requires(ABItems.MANA_CELL_HOUSING)
                    .unlockedBy("has_mana_cell_housing", has(ABItems.MANA_CELL_HOUSING))
                    .unlockedBy("has_energy_cell", has(AEBlocks.ENERGY_CELL))
                    .save(exporter);
        }
    }
}
