package appbot.data;

import static vazkii.botania.data.BlockLootProvider.getPath;

import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import appbot.ABBlocks;

public class BlockLootTableProvider extends BlockLoot implements DataProvider {

    private final Path outputFolder;

    public BlockLootTableProvider(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public void run(CachedOutput cache) throws IOException {
        DataProvider.saveStable(cache, LootTables.serialize(LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ABBlocks.FLUIX_MANA_POOL.get()))
                        .when(ExplosionCondition.survivesExplosion()))
                .setParamSet(LootContextParamSets.BLOCK)
                .build()),
                getPath(outputFolder, ABBlocks.FLUIX_MANA_POOL.getId()));
    }

    @Override
    public String getName() {
        return "Applied Botanics Block Drops";
    }
}
