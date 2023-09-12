package appbot.integration.emi;

import appbot.AppliedBotanics;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import vazkii.botania.fabric.integration.emi.BotaniaEmiPlugin;

public class ABEMIPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addWorkstation(BotaniaEmiPlugin.MANA_INFUSION,
                EmiStack.of(AppliedBotanics.getInstance().fluixManaPool()));
    }
}
