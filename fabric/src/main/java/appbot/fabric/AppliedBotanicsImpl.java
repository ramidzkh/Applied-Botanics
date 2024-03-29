package appbot.fabric;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import appbot.AppliedBotanics;
import appbot.Lookup;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.storage.MEStorage;

public enum AppliedBotanicsImpl implements AppliedBotanics {

    INSTANCE;

    public static AppliedBotanics getInstance() {
        return INSTANCE;
    }

    @Override
    public Lookup<MEStorage, Direction> meStorage(ServerLevel level, BlockPos pos) {
        return context -> MEStorage.SIDED.find(level, pos, context);
    }

    @Override
    public Lookup<ManaReceiver, Direction> manaReceiver(ServerLevel level, BlockPos pos) {
        return BlockApiCache.create(BotaniaFabricCapabilities.MANA_RECEIVER, level, pos)::find;
    }

    @Override
    public Block fluixManaPool() {
        return ABBlocks.FLUIX_MANA_POOL;
    }

    @Override
    public Item manaCellHousing() {
        return ABItems.MANA_CELL_HOUSING;
    }

    @Override
    public MenuType<?> portableCellMenu() {
        return ABMenus.PORTABLE_MANA_CELL_TYPE;
    }
}
