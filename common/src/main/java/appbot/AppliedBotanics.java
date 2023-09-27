package appbot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.storage.IStorageMonitorableAccessor;

public interface AppliedBotanics {

    String MOD_ID = "appbot";

    static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @ExpectPlatform
    static AppliedBotanics getInstance() {
        return getInstance();
    }

    Lookup<IStorageMonitorableAccessor, Direction> meStorage(ServerLevel level, BlockPos pos);

    Lookup<ManaReceiver, Direction> manaReceiver(ServerLevel level, BlockPos pos);

    Block fluixManaPool();

    Item manaCellHousing();

    MenuType<?> portableCellMenu();
}
