package appbot.fabric.data;

import java.util.Locale;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import appbot.AppliedBotanics;
import appbot.forge.ABBlocks;
import appbot.forge.ABItems;

import appeng.core.AppEng;

public class ModelProvider extends BlockStateProvider {

    private static final ResourceLocation P2P_TUNNEL_BASE_ITEM = AppEng.makeId("item/p2p_tunnel_base");
    private static final ResourceLocation P2P_TUNNEL_BASE_PART = AppEng.makeId("part/p2p/p2p_tunnel_base");
    private static final ResourceLocation STORAGE_CELL_LED = AppEng.makeId("item/storage_cell_led");
    private static final ResourceLocation PORTABLE_CELL_LED = AppEng.makeId("item/portable_cell_led");
    private static final ResourceLocation OSMIUM_BLOCK = new ResourceLocation("botania", "block/manasteel_block");

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
        existingFileHelper.trackGenerated(OSMIUM_BLOCK, TEXTURE);

        existingFileHelper.trackGenerated(AppEng.makeId("block/drive/drive_cell"), MODEL);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ABBlocks.FLUIX_MANA_POOL.get(),
                models().getExistingFile(AppliedBotanics.id("fluix_mana_pool")));

        itemModels().basicItem(ABItems.MANA_CELL_HOUSING.get());
        itemModels().basicItem(ABItems.MANA_CELL_CREATIVE.get());

        for (var tier : ABItems.Tier.values()) {
            var cell = ABItems.get(tier);
            var portableCell = ABItems.getPortable(tier);
            itemModels().basicItem(cell.get()).texture("layer1", STORAGE_CELL_LED);
            itemModels().basicItem(portableCell.get()).texture("layer1", PORTABLE_CELL_LED);

            var path = "block/drive/cells/mana_storage_cell" + tier.toString().toLowerCase(Locale.ROOT);
            models().singleTexture(path, AppEng.makeId("block/drive/drive_cell"), "cell", AppliedBotanics.id(path));
        }

        itemModels().withExistingParent("item/mana_p2p_tunnel", P2P_TUNNEL_BASE_ITEM)
                .texture("type", OSMIUM_BLOCK);
        itemModels().withExistingParent("part/mana_p2p_tunnel", P2P_TUNNEL_BASE_PART)
                .texture("type", OSMIUM_BLOCK);
    }
}
