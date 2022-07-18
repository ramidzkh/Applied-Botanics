package appbot.block;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.block.mana.BlockPool;

public class FluixPool extends BlockPool {

    public FluixPool(Variant v, Properties builder) {
        super(v, builder);
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FluixPoolBlockEntity(pos, state);
    }
}
