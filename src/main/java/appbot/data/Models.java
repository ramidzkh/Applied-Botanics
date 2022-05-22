package appbot.data;

import java.io.IOException;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.Registry;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.item.Item;

import appbot.ABItems;

// fabric port of forge's model provider when pls
public class Models implements DataProvider {

    private final FabricDataGenerator dataGenerator;

    public Models(FabricDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void run(HashCache hashCache) throws IOException {
        for (var tier : ABItems.Tier.values()) {
            var path = "mana_storage_cell" + tier.toString().toLowerCase(Locale.ROOT);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("parent", "ae2:block/drive/drive_cell");

            JsonObject textures = new JsonObject();
            var x = "block/drive/cells/" + path;
            textures.addProperty("cell", "appbot:" + x);
            jsonObject.add("textures", textures);

            DataProvider.save(new Gson(), hashCache, jsonObject,
                    dataGenerator.getOutputFolder().resolve("assets/appbot/models/" + x + ".json"));
        }

        save(hashCache, ABItems.MANA_CELL_HOUSING, null);
        save(hashCache, ABItems.MANA_CELL_CREATIVE, null);

        for (var tier : ABItems.Tier.values()) {
            var cell = ABItems.get(tier);
            var portableCell = ABItems.getPortableCell(tier);

            save(hashCache, cell, "ae2:item/storage_cell_led");
            save(hashCache, portableCell, "ae2:item/portable_cell_led");
        }

        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("parent", "ae2:part/p2p/p2p_tunnel_base");

            JsonObject textures = new JsonObject();
            textures.addProperty("type", "botania:block/manasteel_block");
            jsonObject.add("textures", textures);

            DataProvider.save(new Gson(), hashCache, jsonObject,
                    dataGenerator.getOutputFolder().resolve("assets/appbot/models/part/mana_p2p_tunnel.json"));
        }

        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("parent", "ae2:item/p2p_tunnel_base");

            JsonObject textures = new JsonObject();
            textures.addProperty("type", "botania:block/manasteel_block");
            jsonObject.add("textures", textures);

            DataProvider.save(new Gson(), hashCache, jsonObject,
                    dataGenerator.getOutputFolder().resolve("assets/appbot/models/item/mana_p2p_tunnel.json"));
        }
    }

    private void save(HashCache hashCache, Item item, @Nullable String layer1) throws IOException {
        var key = Registry.ITEM.getKey(item);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("parent", "item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", key.getNamespace() + ":item/" + key.getPath());

        if (layer1 != null) {
            textures.addProperty("layer1", layer1);
        }

        jsonObject.add("textures", textures);

        DataProvider.save(new Gson(), hashCache, jsonObject, dataGenerator.getOutputFolder()
                .resolve("assets/" + key.getNamespace() + "/models/item/" + key.getPath() + ".json"));
    }

    @Override
    public String getName() {
        return "Models";
    }
}
