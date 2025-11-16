package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

@Mixin(value = ManaPoolBlockEntity.class, remap = false)
public interface ManaPoolBlockEntityAccessor {

    @Accessor
    int getMana();

    @Accessor
    void setMana(int mana);
}
