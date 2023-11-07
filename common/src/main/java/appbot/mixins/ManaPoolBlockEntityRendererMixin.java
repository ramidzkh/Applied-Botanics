package appbot.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import appbot.block.FluixPoolBlockEntity;
import vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

@Mixin(value = ManaPoolBlockEntityRenderer.class, remap = false)
public class ManaPoolBlockEntityRendererMixin {

    @ModifyVariable(method = "render(Lvazkii/botania/common/block/block_entity/mana/ManaPoolBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("STORE"), ordinal = 2, require = 0)
    private int modifyInsideUVStart(int variable, @Nullable ManaPoolBlockEntity pool) {
        return pool instanceof FluixPoolBlockEntity ? 1 : variable;
    }
}
