package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import appeng.helpers.MultiCraftingTracker;

@Mixin(MultiCraftingTracker.class)
public interface MultiCraftingTrackerAccessor {

    @Invoker
    boolean invokeIsBusy(int slot);
}
