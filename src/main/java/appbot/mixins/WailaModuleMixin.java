package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appbot.block.FluixPoolBlockEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.TooltipPosition;

import appeng.integration.modules.waila.BlockEntityDataProvider;
import appeng.integration.modules.waila.WailaModule;

@Mixin(value = WailaModule.class, remap = false)
public class WailaModuleMixin {

    @Inject(method = "register", at = @At("RETURN"))
    private void register(IRegistrar registrar, CallbackInfo callbackInfo) {
        var blockEntityProvider = new BlockEntityDataProvider();
        registrar.addComponent(blockEntityProvider, TooltipPosition.BODY, FluixPoolBlockEntity.class);
        registrar.addBlockData(blockEntityProvider, FluixPoolBlockEntity.class);
    }
}
