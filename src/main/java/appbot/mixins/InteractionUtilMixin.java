package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.WandOfTheForestItem;

import appeng.util.InteractionUtil;

@Mixin(InteractionUtil.class)
public class InteractionUtilMixin {

    @Inject(method = "canWrenchDisassemble", at = @At("HEAD"), cancellable = true)
    private static void wandNotWrench(ItemStack tool, CallbackInfoReturnable<Boolean> cir) {
        // make binding mana spreaders to p2p easier
        if (WandOfTheForestItem.getBindMode(tool)) {
            cir.setReturnValue(false);
        }
    }
}
