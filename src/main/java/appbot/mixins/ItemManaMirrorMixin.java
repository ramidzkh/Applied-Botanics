package appbot.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.ItemManaMirror;

@Mixin(value = ItemManaMirror.class, remap = false)
public abstract class ItemManaMirrorMixin {

    @Unique
    private static final String MAX_MANA = "maxMana";

    @Inject(method = "inventoryTick", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = true)
    private void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected,
            CallbackInfo callbackInfo, IManaPool pool) {
        if (pool instanceof TilePool tilePool) {
            ItemNBTHelper.setInt(stack, MAX_MANA, tilePool.manaCap);
        } else {
            ItemNBTHelper.setInt(stack, MAX_MANA, -1);
        }
    }

    @Mixin(value = ItemManaMirror.ManaItem.class, remap = false)
    public static class ManaItemMixin {
        @Shadow
        @Final
        private ItemStack stack;

        @Inject(method = "getMaxMana", at = @At("HEAD"), cancellable = true)
        private void getMaxMana(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
            var cap = ItemNBTHelper.getInt(stack, MAX_MANA, -1);

            if (cap > 0) {
                callbackInfoReturnable.setReturnValue(cap);
            }
        }
    }
}
