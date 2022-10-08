package appbot.data;

import java.util.Locale;
import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;

import appbot.ABItems;
import appbot.AppliedBotanics;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {

    public RecipeProvider(DataGenerator p_125973_) {
        super(p_125973_);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> exporter) {
        ShapelessRecipeBuilder.shapeless(ABItems.FLUIX_MANA_POOL::get)
                .requires(ModBlocks.fabulousPool)
                .requires(AEBlocks.INTERFACE)
                .unlockedBy("has_interface", has(AEBlocks.INTERFACE))
                .save(exporter, AppliedBotanics.id("fluix_mana_pool"));

        ShapedRecipeBuilder.shaped(ABItems.MANA_CELL_HOUSING::get)
                .pattern("QSQ")
                .pattern("S S")
                .pattern("III")
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .define('S', ModItems.lifeEssence)
                .define('I', ModItems.manaSteel)
                .unlockedBy("has_life_essence", has(ModItems.lifeEssence))
                .save(exporter, AppliedBotanics.id("mana_cell_housing"));

        for (var tier : ABItems.Tier.values()) {
            var cellComponent = switch (tier) {
                case _1K -> AEItems.CELL_COMPONENT_1K;
                case _4K -> AEItems.CELL_COMPONENT_4K;
                case _16K -> AEItems.CELL_COMPONENT_16K;
                case _64K -> AEItems.CELL_COMPONENT_64K;
                case _256K -> AEItems.CELL_COMPONENT_256K;
            };

            var tierName = tier.toString().toLowerCase(Locale.ROOT);

            ShapelessRecipeBuilder.shapeless(ABItems.get(tier)::get)
                    .requires(ABItems.MANA_CELL_HOUSING::get)
                    .requires(cellComponent)
                    .unlockedBy("has_cell_component" + tierName, has(cellComponent))
                    .save(exporter);
            ShapelessRecipeBuilder.shapeless(ABItems.getPortableCell(tier)::get)
                    .requires(AEBlocks.CHEST)
                    .requires(cellComponent)
                    .requires(AEBlocks.ENERGY_CELL)
                    .requires(ABItems.MANA_CELL_HOUSING::get)
                    .unlockedBy("has_mana_cell_housing", has(ABItems.MANA_CELL_HOUSING::get))
                    .unlockedBy("has_energy_cell", has(AEBlocks.ENERGY_CELL))
                    .save(exporter);
        }
    }
}
