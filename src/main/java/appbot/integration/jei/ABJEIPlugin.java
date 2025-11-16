package appbot.integration.jei;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appbot.AppliedBotanics;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import vazkii.botania.client.integration.jei.ManaPoolRecipeCategory;

@JeiPlugin
public class ABJEIPlugin implements IModPlugin {

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(AppliedBotanics.getInstance().fluixManaPool()),
                ManaPoolRecipeCategory.TYPE);
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return AppliedBotanics.id("main");
    }
}
