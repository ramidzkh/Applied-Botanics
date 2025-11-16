package appbot;

import net.minecraft.resources.ResourceLocation;

public interface AppliedBotanics {

    String MOD_ID = "appbot";

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
