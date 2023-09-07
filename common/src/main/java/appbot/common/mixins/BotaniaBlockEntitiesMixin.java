package appbot.common.mixins;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

import java.util.function.BiFunction;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appbot.AB;
import appbot.common.block.FluixPoolBlockEntity;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.lib.LibBlockNames;

@Mixin(value = BotaniaBlockEntities.class, remap = false)
public class BotaniaBlockEntitiesMixin {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lvazkii/botania/common/block/block_entity/BotaniaBlockEntities;type(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/BiFunction;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType;", remap = true), index = 1)
    private static <T extends BlockEntity> BiFunction<BlockPos, BlockState, T> injectConstructor(ResourceLocation id,
            BiFunction<BlockPos, BlockState, T> func,
            Block... blocks) {
        if (id.equals(prefix(LibBlockNames.POOL))) {
            return (blockPos, blockState) -> {
                if (blockState.is(AB.getInstance().fluixManaPool())) {
                    // noinspection unchecked
                    return (T) new FluixPoolBlockEntity(blockPos, blockState);
                } else {
                    return func.apply(blockPos, blockState);
                }
            };
        }

        return func;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lvazkii/botania/common/block/block_entity/BotaniaBlockEntities;type(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/BiFunction;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType;", remap = true), index = 2)
    private static <T extends BlockEntity> Block[] add(ResourceLocation id, BiFunction<BlockPos, BlockState, T> func,
            Block... blocks) {
        if (id.equals(prefix(LibBlockNames.POOL))) {
            blocks = ArrayUtils.add(blocks, AB.getInstance().fluixManaPool());
        }

        return blocks;
    }
}
