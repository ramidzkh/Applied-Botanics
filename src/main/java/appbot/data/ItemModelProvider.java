package appbot.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import appbot.ABItems;
import appbot.AppliedBotanics;

import appeng.core.AppEng;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {

    private static final ResourceLocation P2P_TUNNEL_BASE_ITEM = AppEng.makeId("item/p2p_tunnel_base");
    private static final ResourceLocation P2P_TUNNEL_BASE_PART = AppEng.makeId("part/p2p/p2p_tunnel_base");
    private static final ResourceLocation STORAGE_CELL_LED = AppEng.makeId("item/storage_cell_led");
    private static final ResourceLocation PORTABLE_CELL_LED = AppEng.makeId("item/portable_cell_led");
    private static final ResourceLocation MANASTEEL_BLOCK = new ResourceLocation("botania", "block/manasteel_block");

    public ItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, AppliedBotanics.MOD_ID, existingFileHelper);

        existingFileHelper.trackGenerated(P2P_TUNNEL_BASE_ITEM, MODEL);
        existingFileHelper.trackGenerated(P2P_TUNNEL_BASE_PART, MODEL);
        existingFileHelper.trackGenerated(STORAGE_CELL_LED, TEXTURE);
        existingFileHelper.trackGenerated(PORTABLE_CELL_LED, TEXTURE);
        existingFileHelper.trackGenerated(MANASTEEL_BLOCK, TEXTURE);
    }

    @Override
    protected void registerModels() {
        var housing = ABItems.MANA_CELL_HOUSING;
        flatSingleLayer(housing, "item/" + housing.getId().getPath());

        var creative = ABItems.MANA_CELL_CREATIVE;
        flatSingleLayer(creative, "item/" + creative.getId().getPath());

        for (var tier : ABItems.Tier.values()) {
            var cell = ABItems.get(tier);
            var portableCell = ABItems.getPortable(tier);
            cell(cell, "item/" + cell.getId().getPath());
            portableCell(portableCell, "item/portable_" + cell.getId().getPath());
        }

        withExistingParent("item/mana_p2p_tunnel", P2P_TUNNEL_BASE_ITEM)
                .texture("type", MANASTEEL_BLOCK);
        withExistingParent("part/mana_p2p_tunnel", P2P_TUNNEL_BASE_PART)
                .texture("type", MANASTEEL_BLOCK);
    }

    private void cell(RegistryObject<Item> cell, String background) {
        singleTexture(cell.getId().getPath(), mcLoc("item/generated"), "layer0", AppliedBotanics.id(background))
                .texture("layer1", STORAGE_CELL_LED);
    }

    private void portableCell(RegistryObject<Item> portable, String background) {
        singleTexture(portable.getId().getPath(), mcLoc("item/generated"), "layer0", AppliedBotanics.id(background))
                .texture("layer1", PORTABLE_CELL_LED);
    }

    private void flatSingleLayer(RegistryObject<Item> item, String texture) {
        singleTexture(item.getId().getPath(), mcLoc("item/generated"), "layer0", AppliedBotanics.id(texture));
    }
}
