package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appbot.AppliedBotanics;

import appeng.core.AppEngBase;

@Mixin(AppEngBase.class)
public class AppEngBaseMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postBaseInit(CallbackInfo callbackInfo) {
        AppliedBotanics.initialize();
    }
}
