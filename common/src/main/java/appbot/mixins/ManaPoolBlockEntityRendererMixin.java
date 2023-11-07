package appbot.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import appbot.block.FluixPool;
import vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

@Mixin(value = ManaPoolBlockEntityRenderer.class, remap = false)
public class ManaPoolBlockEntityRendererMixin {
    @ModifyVariable(method = "render(Lvazkii/botania/common/block/block_entity/mana/ManaPoolBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("STORE"), remap = false, ordinal = 2)
    int modifyInsideUVStart(int variable, @Nullable ManaPoolBlockEntity pool) {
        boolean fluix = pool != null && pool.getBlockState().getBlock() instanceof FluixPool;
        return fluix ? 1 : variable;
    }
}
