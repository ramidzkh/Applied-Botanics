package appbot.data;

import java.util.Locale;

import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appbot.ABBlocks;
import appbot.ABItems;
import appbot.AppliedBotanics;
import vazkii.botania.common.block.BotaniaBlocks;

import appeng.core.AppEng;

public class ModelProvider extends BlockStateProvider {

    private static final ResourceLocation P2P_TUNNEL_BASE_ITEM = AppEng.makeId("item/p2p_tunnel_base");
    private static final ResourceLocation P2P_TUNNEL_BASE_PART = AppEng.makeId("part/p2p/p2p_tunnel_base");
    private static final ResourceLocation STORAGE_CELL_LED = AppEng.makeId("item/storage_cell_led");
    private static final ResourceLocation PORTABLE_CELL_LED = AppEng.makeId("item/portable_cell_led");
    private static final ResourceLocation MANASTEEL_BLOCK = ModelLocationUtils
            .getModelLocation(BotaniaBlocks.manasteelBlock);

    // copied from ItemModelProvider
    protected static final ExistingFileHelper.ResourceType TEXTURE = new ExistingFileHelper.ResourceType(
            PackType.CLIENT_RESOURCES, ".png", "textures");
    protected static final ExistingFileHelper.ResourceType MODEL = new ExistingFileHelper.ResourceType(
            PackType.CLIENT_RESOURCES, ".json", "models");

    public ModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AppliedBotanics.MOD_ID, existingFileHelper);

        existingFileHelper.trackGenerated(P2P_TUNNEL_BASE_ITEM, MODEL);
        existingFileHelper.trackGenerated(P2P_TUNNEL_BASE_PART, MODEL);
        existingFileHelper.trackGenerated(STORAGE_CELL_LED, TEXTURE);
        existingFileHelper.trackGenerated(PORTABLE_CELL_LED, TEXTURE);
        existingFileHelper.trackGenerated(MANASTEEL_BLOCK, TEXTURE);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ABBlocks.FLUIX_MANA_POOL.get(),
                models().getExistingFile(AppliedBotanics.id("fluix_mana_pool")));

        itemModels().basicItem(ABItems.MANA_CELL_HOUSING.get());

        for (var tier : ABItems.Tier.values()) {
            var cell = ABItems.get(tier);
            var portableCell = ABItems.getPortableCell(tier);
            itemModels().basicItem(cell.get()).texture("layer1", STORAGE_CELL_LED);
            itemModels().withExistingParent(portableCell.getId().getPath(), mcLoc("item/generated"))
                    .texture("layer0", "item/portable_mana_cell_housing")
                    .texture("layer1", PORTABLE_CELL_LED)
                    .texture("layer2", "item/portable_cell_screen")
                    .texture("layer3", "item/portable_mana_cell" + tier.toString().toLowerCase(Locale.ROOT));
        }

        itemModels().withExistingParent("item/mana_p2p_tunnel", P2P_TUNNEL_BASE_ITEM)
                .texture("type", MANASTEEL_BLOCK);
        itemModels().withExistingParent("part/mana_p2p_tunnel", P2P_TUNNEL_BASE_PART)
                .texture("type", MANASTEEL_BLOCK);
    }
}
