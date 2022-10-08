package appbot;

import static appbot.AppliedBotanics.id;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;

import appbot.ae2.ManaKeyType;
import appbot.ae2.ManaP2PTunnelPart;
import vazkii.botania.common.item.ModItems;

import appeng.api.client.StorageCellModels;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.parts.PartModels;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.items.storage.BasicStorageCell;
import appeng.items.storage.CreativeCellItem;
import appeng.items.tools.powered.PortableCellItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;

public class ABItems {

    public static final CreativeModeTab CREATIVE_TAB = FabricItemGroupBuilder.build(id("tab"),
            () -> new ItemStack(ABItems.MANA_P2P_TUNNEL));

    private static Item.Properties properties() {
        return new Item.Properties().tab(CREATIVE_TAB);
    }

    public static final Item FLUIX_MANA_POOL = Registry.register(Registry.ITEM, id("fluix_mana_pool"),
            new BlockItem(ABBlocks.FLUIX_MANA_POOL, ModItems.defaultBuilder().tab(CREATIVE_TAB)));

    public static final Item MANA_CELL_HOUSING = Registry.register(Registry.ITEM, id("mana_cell_housing"),
            new Item(properties()));

    public static final Item MANA_CELL_CREATIVE = Registry.register(Registry.ITEM, id("creative_mana_cell"),
            new CreativeCellItem(properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final Item MANA_CELL_1K = Registry.register(Registry.ITEM, id("mana_storage_cell_1k"),
            new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_1K, MANA_CELL_HOUSING, 0.5f, 1, 8, 1,
                    ManaKeyType.TYPE));
    public static final Item MANA_CELL_4K = Registry.register(Registry.ITEM, id("mana_storage_cell_4k"),
            new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_4K, MANA_CELL_HOUSING, 1.0f, 4, 32, 1,
                    ManaKeyType.TYPE));
    public static final Item MANA_CELL_16K = Registry.register(Registry.ITEM, id("mana_storage_cell_16k"),
            new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_16K, MANA_CELL_HOUSING, 1.5f, 16, 128,
                    1, ManaKeyType.TYPE));
    public static final Item MANA_CELL_64K = Registry.register(Registry.ITEM, id("mana_storage_cell_64k"),
            new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_64K, MANA_CELL_HOUSING, 2.0f, 64, 512,
                    1, ManaKeyType.TYPE));
    public static final Item MANA_CELL_256K = Registry.register(Registry.ITEM, id("mana_storage_cell_256k"),
            new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_256K, MANA_CELL_HOUSING, 2.5f, 256,
                    2048, 1, ManaKeyType.TYPE));

    public static final Item PORTABLE_MANA_CELL_1K = Registry.register(Registry.ITEM,
            id("portable_mana_storage_cell_1k"), new PortableCellItem(ManaKeyType.TYPE, ABMenus.PORTABLE_MANA_CELL_TYPE,
                    PortableCellItem.SIZE_1K, properties().stacksTo(1)));
    public static final Item PORTABLE_MANA_CELL_4K = Registry.register(Registry.ITEM,
            id("portable_mana_storage_cell_4k"), new PortableCellItem(ManaKeyType.TYPE, ABMenus.PORTABLE_MANA_CELL_TYPE,
                    PortableCellItem.SIZE_4K, properties().stacksTo(1)));
    public static final Item PORTABLE_MANA_CELL_16K = Registry.register(Registry.ITEM,
            id("portable_mana_storage_cell_16k"), new PortableCellItem(ManaKeyType.TYPE,
                    ABMenus.PORTABLE_MANA_CELL_TYPE, PortableCellItem.SIZE_16K, properties().stacksTo(1)));
    public static final Item PORTABLE_MANA_CELL_64K = Registry.register(Registry.ITEM,
            id("portable_mana_storage_cell_64k"), new PortableCellItem(ManaKeyType.TYPE,
                    ABMenus.PORTABLE_MANA_CELL_TYPE, PortableCellItem.SIZE_64K, properties().stacksTo(1)));
    public static final Item PORTABLE_MANA_CELL_256K = Registry.register(Registry.ITEM,
            id("portable_mana_storage_cell_256k"), new PortableCellItem(ManaKeyType.TYPE,
                    ABMenus.PORTABLE_MANA_CELL_TYPE, PortableCellItem.SIZE_256K, properties().stacksTo(1)));

    public static final PartItem<?> MANA_P2P_TUNNEL = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ManaP2PTunnelPart.class));
        return Registry.register(Registry.ITEM, id("mana_p2p_tunnel"),
                new PartItem<>(properties(), ManaP2PTunnelPart.class, ManaP2PTunnelPart::new));
    });

    public static void register() {
        StorageCells.addCellGuiHandler(new ICellGuiHandler() {
            @Override
            public boolean isSpecializedFor(ItemStack cell) {
                return cell.getItem()instanceof IBasicCellItem basicCellItem
                        && basicCellItem.getKeyType() == ManaKeyType.TYPE;
            }

            @Override
            public void openChestGui(Player player, IChestOrDrive chest, ICellHandler cellHandler, ItemStack cell) {
                chest.getUp();
                MenuOpener.open(MEStorageMenu.TYPE, player, MenuLocators.forBlockEntity((BlockEntity) chest));
            }
        });

        StorageCellModels.registerModel(MANA_CELL_CREATIVE, AppEng.makeId("block/drive/cells/creative_cell"));

        for (var tier : Tier.values()) {
            var cell = get(tier);
            StorageCellModels.registerModel(cell, id("block/drive/cells/" + Registry.ITEM.getKey(cell).getPath()));
        }
    }

    public static Item get(Tier tier) {
        return switch (tier) {
            case _1K -> MANA_CELL_1K;
            case _4K -> MANA_CELL_4K;
            case _16K -> MANA_CELL_16K;
            case _64K -> MANA_CELL_64K;
            case _256K -> MANA_CELL_256K;
        };
    }

    public static Item getPortableCell(Tier tier) {
        return switch (tier) {
            case _1K -> PORTABLE_MANA_CELL_1K;
            case _4K -> PORTABLE_MANA_CELL_4K;
            case _16K -> PORTABLE_MANA_CELL_16K;
            case _64K -> PORTABLE_MANA_CELL_64K;
            case _256K -> PORTABLE_MANA_CELL_256K;
        };
    }

    public enum Tier {
        _1K, _4K, _16K, _64K, _256K
    }
}
