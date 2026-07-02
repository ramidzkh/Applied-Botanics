package appbot.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import vazkii.botania.common.block.mana.ManaPoolBlock;

public class FluixPool extends ManaPoolBlock {

    public FluixPool() {
        super(ManaPoolBlock.MAX_MANA, true, false, ManaPoolBlock.NORMAL_SHAPE_VARIANT, null,
                BlockBehaviour.Properties.ofFullCopy(botaniaBlock("livingrock", "LIVINGROCK", "livingrock")));
    }

    private static Block botaniaBlock(String path, String... fieldNames) {
        // Avoid direct BotaniaBlocks field references; snapshot builds have renamed these fields.
        for (var fieldName : fieldNames) {
            try {
                var field = Class.forName("vazkii.botania.common.block.BotaniaBlocks").getField(fieldName);
                if (field.get(null) instanceof Block block) {
                    return block;
                }
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
            }
        }

        var id = ResourceLocation.fromNamespaceAndPath("botania", path);
        var block = BuiltInRegistries.BLOCK.get(id);
        if (!BuiltInRegistries.BLOCK.getKey(block).equals(id)) {
            throw new IllegalStateException("Missing Botania block: " + id);
        }
        return block;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluixPoolBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity,
            ItemStack itemStack) {
        if (livingEntity instanceof Player player
                && level.getBlockEntity(blockPos) instanceof FluixPoolBlockEntity blockEntity) {
            blockEntity.getMainNode().setOwningPlayer(player);
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        var pool = (FluixPoolBlockEntity) world.getBlockEntity(pos);
        return pool != null ? pool.calculateComparatorLevel() : 0;
    }

    @Override
    public VoxelShape getInnerShape(BlockState state) {
        return box(1, 1, 1, 15, 6, 15);
    }
}
