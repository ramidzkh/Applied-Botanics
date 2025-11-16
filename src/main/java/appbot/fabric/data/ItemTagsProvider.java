package appbot.fabric.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagManager;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import appbot.AppliedBotanics;
import appbot.forge.ABItems;
import vazkii.botania.common.lib.BotaniaTags;

import appeng.api.features.P2PTunnelAttunement;

public class ItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider {

    public ItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries,
            CompletableFuture<TagLookup<Block>> blockTagsProvider, ExistingFileHelper existingFileHelper) {
        super(packOutput, registries, blockTagsProvider, AppliedBotanics.MOD_ID, existingFileHelper);

        existingFileHelper.trackGenerated(BotaniaTags.Items.PETALS.location(), new ExistingFileHelper.ResourceType(
                PackType.SERVER_DATA, ".json", TagManager.getTagDir(Registries.ITEM)));
        existingFileHelper.trackGenerated(BotaniaTags.Items.DUSTS_MANA.location(), new ExistingFileHelper.ResourceType(
                PackType.SERVER_DATA, ".json", TagManager.getTagDir(Registries.ITEM)));
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        tag(P2PTunnelAttunement.getAttunementTag(ABItems.MANA_P2P_TUNNEL.get()))
                .addTag(BotaniaTags.Items.PETALS)
                .addTag(BotaniaTags.Items.DUSTS_MANA);
    }
}
