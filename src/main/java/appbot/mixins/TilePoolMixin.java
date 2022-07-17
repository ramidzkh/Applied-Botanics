package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appbot.block.FluixPoolBlockEntity;
import vazkii.botania.common.block.tile.mana.TilePool;

@Mixin(TilePool.class)
public class TilePoolMixin implements FluixPoolBlockEntity.Accessor {

    @Shadow
    private int mana;

    @Inject(method = "initManaCapAndNetwork", at = @At(value = "FIELD", target = "Lvazkii/botania/common/block/tile/mana/TilePool;manaCap:I", shift = At.Shift.AFTER))
    private void recalculateManaCap(CallbackInfo callbackInfo) {
        if ((Object) this instanceof FluixPoolBlockEntity fluixPoolBlockEntity) {
            fluixPoolBlockEntity.recalculateManaCap();
        }
    }

    @Redirect(method = "writePacketNBT", at = @At(value = "FIELD", target = "Lvazkii/botania/common/block/tile/mana/TilePool;mana:I"))
    private int getMana(TilePool instance) {
        return instance.getCurrentMana();
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = mana;
    }
}
