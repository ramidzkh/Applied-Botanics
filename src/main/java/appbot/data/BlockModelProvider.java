package appbot.data;

import java.util.Locale;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import appbot.ABItems;
import appbot.AppliedBotanics;

import appeng.core.AppEng;

public class BlockModelProvider extends net.minecraftforge.client.model.generators.BlockModelProvider {

    private static final ResourceLocation DRIVE_CELL = AppEng.makeId("block/drive/drive_cell");

    public BlockModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, AppliedBotanics.MOD_ID, existingFileHelper);

        existingFileHelper.trackGenerated(DRIVE_CELL, MODEL);
    }

    @Override
    protected void registerModels() {
        // generator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ABBlocks.FLUIX_MANA_POOL,
        // id("block/fluix_mana_pool")));

        for (var tier : ABItems.Tier.values()) {
            cell("mana_storage_cell" + tier.toString().toLowerCase(Locale.ROOT));
        }
    }

    private void cell(String path) {
        withExistingParent("block/drive/cells/" + path, DRIVE_CELL).texture("cell", "block/drive/cells/" + path);
    }
}
