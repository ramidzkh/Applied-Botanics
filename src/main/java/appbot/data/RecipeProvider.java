package appbot.data;

import static appbot.AppliedBotanics.id;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;

import appbot.ABItems;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {

    public RecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ABItems.FLUIX_MANA_POOL.get())
                .requires(BotaniaBlocks.FABULOUS_MANA_POOL)
                .requires(AEBlocks.INTERFACE)
                .unlockedBy("has_interface", has(AEBlocks.INTERFACE))
                .save(recipeOutput, id("fluix_mana_pool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ABItems.MANA_CELL_HOUSING.get())
                .pattern("QSQ")
                .pattern("S S")
                .pattern("III")
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .define('S', BotaniaItems.GAIA_SPIRIT)
                .define('I', BotaniaItems.MANASTEEL_INGOT)
                .unlockedBy("has_life_essence", has(BotaniaItems.GAIA_SPIRIT))
                .save(recipeOutput, id("mana_cell_housing"));

        for (var tier : ABItems.Tier.values()) {
            var cellComponent = switch (tier) {
                case _1K -> AEItems.CELL_COMPONENT_1K;
                case _4K -> AEItems.CELL_COMPONENT_4K;
                case _16K -> AEItems.CELL_COMPONENT_16K;
                case _64K -> AEItems.CELL_COMPONENT_64K;
                case _256K -> AEItems.CELL_COMPONENT_256K;
            };

            var tierName = tier.toString().toLowerCase(Locale.ROOT);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ABItems.get(tier).get())
                    .requires(ABItems.MANA_CELL_HOUSING.get())
                    .requires(cellComponent)
                    .unlockedBy("has_cell_component" + tierName, has(cellComponent))
                    .save(recipeOutput);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ABItems.getPortableCell(tier).get())
                    .requires(AEBlocks.ME_CHEST)
                    .requires(cellComponent)
                    .requires(AEBlocks.ENERGY_CELL)
                    .requires(ABItems.MANA_CELL_HOUSING.get())
                    .unlockedBy("has_mana_cell_housing", has(ABItems.MANA_CELL_HOUSING.get()))
                    .unlockedBy("has_energy_cell", has(AEBlocks.ENERGY_CELL))
                    .save(recipeOutput);
        }
    }
}
