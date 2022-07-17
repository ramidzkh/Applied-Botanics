package appbot.client;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

import appbot.ABBlocks;
import appbot.ABItems;
import appbot.ABMenus;
import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.MathHelper;

import appeng.api.client.AEStackRendering;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.init.client.InitScreens;
import appeng.items.storage.BasicStorageCell;
import appeng.menu.me.common.MEStorageMenu;

public interface AppliedBotanicsClient {

    static void initialize() {
        AEStackRendering.register(ManaKeyType.TYPE, ManaKey.class, new ManaRenderer());

        for (var tier : ABItems.Tier.values()) {
            ColorProviderRegistry.ITEM.register(BasicStorageCell::getColor, ABItems.get(tier));
        }

        InitScreens.<MEStorageMenu, MEStorageScreen<MEStorageMenu>>register(ABMenus.PORTABLE_MANA_CELL_TYPE,
                MEStorageScreen::new, "/screens/terminals/portable_mana_cell.json");

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }

            int color = ColorHelper.getColorValue(DyeColor.WHITE);

            if (world != null && pos != null) {
                if (world.getBlockEntity(pos)instanceof TilePool pool) {
                    color = ColorHelper.getColorValue(pool.getColor());
                }
            }

            float time = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
            int fabulousColor = Mth.hsvToRgb(240 + 20 * Mth.sin(time / 200), 0.6F, 1F);
            return MathHelper.multiplyColor(fabulousColor, color);
        }, ABBlocks.FLUIX_MANA_POOL);

        ColorProviderRegistry.ITEM.register((itemStack, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }

            int color = ColorHelper.getColorValue(DyeColor.WHITE);
            int fabulousColor = Mth.hsvToRgb(250, 0.6F, 1F);
            return MathHelper.multiplyColor(fabulousColor, color);
        }, ABItems.FLUIX_MANA_POOL);
    }
}
