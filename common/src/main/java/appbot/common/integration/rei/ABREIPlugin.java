package appbot.common.integration.rei;

import appbot.AB;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import vazkii.botania.fabric.integration.rei.BotaniaREICategoryIdentifiers;

public class ABREIPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.addWorkstations(BotaniaREICategoryIdentifiers.MANA_INFUSION,
                EntryStacks.of(AB.getInstance().fluixManaPool()));
    }

    @Override
    public String getPluginProviderName() {
        return "Applied Botanics";
    }
}
