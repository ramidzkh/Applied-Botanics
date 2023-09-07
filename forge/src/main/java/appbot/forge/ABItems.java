package appbot.forge;

import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import appbot.AB;
import appbot.common.ae2.ManaKeyType;
import appbot.common.item.CreativeManaCellItem;
import appbot.common.item.ManaCellItem;
import appbot.common.item.PortableManaCellItem;
import appbot.forge.ae2.ManaP2PTunnelPart;
import vazkii.botania.common.item.BotaniaItems;

import appeng.api.client.StorageCellModels;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.parts.PartModels;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.upgrades.Upgrades;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;

public class ABItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            AB.MOD_ID);

    public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab("appbot.tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ABItems.MANA_P2P_TUNNEL.get());
        }
    };

    private static Item.Properties properties() {
        return new Item.Properties().tab(CREATIVE_TAB);
    }

    public static final RegistryObject<Item> FLUIX_MANA_POOL = ITEMS.register("fluix_mana_pool",
            () -> new BlockItem(ABBlocks.FLUIX_MANA_POOL.get(), BotaniaItems.defaultBuilder().tab(CREATIVE_TAB)));

    public static final RegistryObject<Item> MANA_CELL_HOUSING = ITEMS.register("mana_cell_housing",
            () -> new Item(properties()));

    public static final RegistryObject<Item> MANA_CELL_CREATIVE = ITEMS.register("creative_mana_cell",
            () -> new CreativeManaCellItem(properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> MANA_CELL_1K = ITEMS.register("mana_storage_cell_1k",
            () -> new ManaCellItem(properties().stacksTo(1), AEItems.CELL_COMPONENT_1K,
                    1, 0.5f));
    public static final RegistryObject<Item> MANA_CELL_4K = ITEMS.register("mana_storage_cell_4k",
            () -> new ManaCellItem(properties().stacksTo(1), AEItems.CELL_COMPONENT_4K,
                    4, 1.0f));
    public static final RegistryObject<Item> MANA_CELL_16K = ITEMS.register("mana_storage_cell_16k",
            () -> new ManaCellItem(properties().stacksTo(1), AEItems.CELL_COMPONENT_16K,
                    16, 1.5f));
    public static final RegistryObject<Item> MANA_CELL_64K = ITEMS.register("mana_storage_cell_64k",
            () -> new ManaCellItem(properties().stacksTo(1), AEItems.CELL_COMPONENT_64K,
                    64, 2.0f));
    public static final RegistryObject<Item> MANA_CELL_256K = ITEMS.register("mana_storage_cell_256k",
            () -> new ManaCellItem(properties().stacksTo(1), AEItems.CELL_COMPONENT_256K,
                    256, 2.5f));

    public static final RegistryObject<Item> PORTABLE_MANA_CELL_1K = ITEMS.register("portable_mana_storage_cell_1k",
            () -> new PortableManaCellItem(properties().stacksTo(1), 1, 0.5));
    public static final RegistryObject<Item> PORTABLE_MANA_CELL_4K = ITEMS.register("portable_mana_storage_cell_4k",
            () -> new PortableManaCellItem(properties().stacksTo(1), 4, 1.0));
    public static final RegistryObject<Item> PORTABLE_MANA_CELL_16K = ITEMS.register("portable_mana_storage_cell_16k",
            () -> new PortableManaCellItem(properties().stacksTo(1), 16, 1.5));
    public static final RegistryObject<Item> PORTABLE_MANA_CELL_64K = ITEMS.register("portable_mana_storage_cell_64k",
            () -> new PortableManaCellItem(properties().stacksTo(1), 64, 2.0));
    public static final RegistryObject<Item> PORTABLE_MANA_CELL_256K = ITEMS.register("portable_mana_storage_cell_256k",
            () -> new PortableManaCellItem(properties().stacksTo(1), 256, 2.5));

    public static final RegistryObject<PartItem<ManaP2PTunnelPart>> MANA_P2P_TUNNEL = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ManaP2PTunnelPart.class));
        return ITEMS.register("mana_p2p_tunnel",
                () -> new PartItem<>(properties(), ManaP2PTunnelPart.class, ManaP2PTunnelPart::new));
    });

    public static void initialize(IEventBus bus) {
        ITEMS.register(bus);

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

        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            StorageCellModels.registerModel(MANA_CELL_CREATIVE.get(), AppEng.makeId("block/drive/cells/creative_cell"));

            for (var tier : Tier.values()) {
                var cell = get(tier);
                var portable = getPortable(tier);

                Upgrades.add(AEItems.ENERGY_CARD, portable.get(), 2, GuiText.PortableCells.getTranslationKey());

                String path = "block/drive/cells/" + cell.getId().getPath();
                var id = AB.id(path);
                StorageCellModels.registerModel(cell.get(), id);
                StorageCellModels.registerModel(portable.get(), id);
            }
        }));
    }

    public static RegistryObject<Item> get(Tier tier) {
        return switch (tier) {
            case _1K -> MANA_CELL_1K;
            case _4K -> MANA_CELL_4K;
            case _16K -> MANA_CELL_16K;
            case _64K -> MANA_CELL_64K;
            case _256K -> MANA_CELL_256K;
        };
    }

    public static RegistryObject<Item> getPortable(Tier tier) {
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
