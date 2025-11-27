package appbot.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.phys.BlockHitResult;

import appbot.ae2.ManaP2PTunnelPart;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.entity.ManaBurstEntity;

@Mixin(ManaBurstEntity.class)
public class ManaBurstEntityMixin {

    @WrapOperation(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lvazkii/botania/common/entity/ManaBurstEntity;onReceiverImpact(Lvazkii/botania/api/mana/ManaReceiver;)Z"))
    private boolean onReceiverImpact(ManaBurstEntity instance, ManaReceiver collector, Operation<Boolean> original,
            BlockHitResult hit) {
        if (collector instanceof ManaP2PTunnelPart p2p) {
            p2p.receiveManaFromBurst(instance, hit);
            return false;
        }

        return original.call(instance, collector);
    }
}
