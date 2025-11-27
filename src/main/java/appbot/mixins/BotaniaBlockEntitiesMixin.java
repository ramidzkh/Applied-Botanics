package appbot.mixins;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appbot.ABBlocks;
import appbot.block.FluixPoolBlockEntity;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.lib.LibBlockNames;

@Mixin(BotaniaBlockEntities.class)
public abstract class BotaniaBlockEntitiesMixin {

    @ModifyVariable(method = "type(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType;", at = @At("HEAD"), index = 1, argsOnly = true)
    private static <T extends BlockEntity> BlockEntityType.BlockEntitySupplier<T> k(
            BlockEntityType.BlockEntitySupplier<T> factory, ResourceLocation id,
            BlockEntityType.BlockEntitySupplier<T> $factory, Block[] blocks) {
        if (BotaniaAPI.botaniaRL(LibBlockNames.POOL).equals(id)) {
            return (blockPos, blockState) -> {
                if (blockState.is(ABBlocks.FLUIX_MANA_POOL.get())) {
                    // noinspection unchecked
                    return (T) new FluixPoolBlockEntity(blockPos, blockState);
                } else {
                    return factory.create(blockPos, blockState);
                }
            };
        }

        return factory;
    }

    @ModifyVariable(method = "type(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType;", at = @At("HEAD"), index = 2, argsOnly = true)
    private static <T extends BlockEntity> Block[] t(Block[] blocks, ResourceLocation id,
            BlockEntityType.BlockEntitySupplier<T> $factory, Block[] $blocks) {
        if (BotaniaAPI.botaniaRL(LibBlockNames.POOL).equals(id)) {
            return ArrayUtils.add(blocks, ABBlocks.FLUIX_MANA_POOL.get());
        }

        return blocks;
    }
}
