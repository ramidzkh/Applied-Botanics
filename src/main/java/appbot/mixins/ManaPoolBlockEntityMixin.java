package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import appbot.block.FluixPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

@Mixin(value = ManaPoolBlockEntity.class, remap = false)
public class ManaPoolBlockEntityMixin implements FluixPoolBlockEntity.Accessor {

    @Shadow
    private int mana;

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = mana;
    }
}
