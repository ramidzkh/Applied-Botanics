package appbot.integration.rei;

import appbot.ABItems;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import vazkii.botania.fabric.integration.rei.BotaniaREICategoryIdentifiers;

public class ABREIPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.addWorkstations(BotaniaREICategoryIdentifiers.MANA_INFUSION, EntryStacks.of(ABItems.FLUIX_MANA_POOL));
    }

    @Override
    public String getPluginProviderName() {
        return "Applied Botanics";
    }
}
