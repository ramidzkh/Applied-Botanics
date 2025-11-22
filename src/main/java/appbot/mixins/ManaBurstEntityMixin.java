package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appbot.ae2.ManaP2PTunnelPart;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.entity.ManaBurstEntity;

@Mixin(ManaBurstEntity.class)
public class ManaBurstEntityMixin {

    @Inject(method = "onReceiverImpact", at = @At("HEAD"), cancellable = true)
    private void onReceiverImpact(ManaReceiver receiver, CallbackInfoReturnable<Boolean> cir) {
        if (receiver instanceof ManaP2PTunnelPart p2p) {
            p2p.receiveManaFromBurst((ManaBurstEntity) (Object) this);
            cir.setReturnValue(false);
        }
    }
}
