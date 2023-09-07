package appbot.fabric.data;

import java.util.Locale;
import java.util.Optional;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;

import appbot.AB;
import appbot.fabric.ABBlocks;
import appbot.fabric.ABItems;
import vazkii.botania.common.block.BotaniaBlocks;

import appeng.core.AppEng;

public class ModelProvider extends FabricModelProvider {

    private static final TextureSlot LAYER1 = TextureSlot.create("layer1");
    private static final TextureSlot TYPE = TextureSlot.create("type");
    private static final TextureSlot CELL = TextureSlot.create("cell");

    private static final ResourceLocation STORAGE_CELL_LED = AppEng.makeId("item/storage_cell_led");
    private static final ResourceLocation PORTABLE_CELL_LED = AppEng.makeId("item/portable_cell_led");

    private static final ModelTemplate GENERATED_1 = new ModelTemplate(
            Optional.of(new ResourceLocation("item/generated")), Optional.empty(), TextureSlot.LAYER0, LAYER1);
    private static final ModelTemplate P2P_TUNNEL_BASE_ITEM = new ModelTemplate(
            Optional.of(AppEng.makeId("item/p2p_tunnel_base")), Optional.empty(), TYPE);
    private static final ModelTemplate P2P_TUNNEL_BASE_PART = new ModelTemplate(
            Optional.of(AppEng.makeId("part/p2p/p2p_tunnel_base")), Optional.empty(), TYPE);
    private static final ModelTemplate DRIVE_CELL = new ModelTemplate(
            Optional.of(AppEng.makeId("block/drive/drive_cell")), Optional.empty(), CELL);

    public ModelProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator) {
        generator.createNonTemplateModelBlock(ABBlocks.FLUIX_MANA_POOL);
    }

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        generator.generateFlatItem(ABItems.MANA_CELL_HOUSING, ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(ABItems.MANA_CELL_CREATIVE, ModelTemplates.FLAT_ITEM);

        for (var tier : ABItems.Tier.values()) {
            var cell = ABItems.get(tier);
            var portableCell = ABItems.getPortable(tier);

            GENERATED_1.create(ModelLocationUtils.getModelLocation(cell),
                    TextureMapping.layer0(cell).put(LAYER1, STORAGE_CELL_LED), generator.output);
            GENERATED_1.create(ModelLocationUtils.getModelLocation(portableCell),
                    TextureMapping.layer0(portableCell).put(LAYER1, PORTABLE_CELL_LED), generator.output);

            var path = "mana_storage_cell" + tier.toString().toLowerCase(Locale.ROOT);
            DRIVE_CELL.create(AB.id("block/drive/cells/" + path),
                    new TextureMapping().put(CELL, AB.id("block/drive/cells/" + path)), generator.output);
        }

        P2P_TUNNEL_BASE_ITEM.create(AB.id("item/mana_p2p_tunnel"),
                new TextureMapping().put(TYPE, ModelLocationUtils.getModelLocation(BotaniaBlocks.manasteelBlock)),
                generator.output);
        P2P_TUNNEL_BASE_PART.create(AB.id("part/mana_p2p_tunnel"),
                new TextureMapping().put(TYPE, ModelLocationUtils.getModelLocation(BotaniaBlocks.manasteelBlock)),
                generator.output);
    }
}
