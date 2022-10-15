package appbot;

import net.minecraft.Util;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import appbot.ae2.ManaKeyType;
import appbot.ae2.ManaP2PTunnelPart;
import vazkii.botania.common.item.ModItems;

import appeng.api.parts.PartModels;
import appeng.core.definitions.AEItems;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.items.storage.BasicStorageCell;
import appeng.items.storage.CreativeCellItem;
import appeng.items.tools.powered.PortableCellItem;

public class ABItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            AppliedBotanics.MOD_ID);

    public static void initialize(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab("appbot.tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ABItems.MANA_P2P_TUNNEL::get);
        }
    };

    private static Item.Properties properties() {
        return new Item.Properties().tab(CREATIVE_TAB);
    }

    public static final RegistryObject<Item> FLUIX_MANA_POOL = ITEMS.register("fluix_mana_pool",
            () -> new BlockItem(ABBlocks.FLUIX_MANA_POOL.get(), ModItems.defaultBuilder().tab(CREATIVE_TAB)));

    public static final RegistryObject<Item> MANA_CELL_HOUSING = ITEMS.register("mana_cell_housing",
            () -> new Item(properties()));

    public static final RegistryObject<Item> MANA_CELL_CREATIVE = ITEMS.register("creative_mana_cell",
            () -> new CreativeCellItem(properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> MANA_CELL_1K = ITEMS.register("mana_storage_cell_1k",
            () -> new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_1K, MANA_CELL_HOUSING.get(),
                    0.5f, 1, 8, 1,
                    ManaKeyType.TYPE));
    public static final RegistryObject<Item> MANA_CELL_4K = ITEMS.register("mana_storage_cell_4k",
            () -> new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_4K, MANA_CELL_HOUSING.get(),
                    1.0f, 4, 32, 1,
                    ManaKeyType.TYPE));
    public static final RegistryObject<Item> MANA_CELL_16K = ITEMS.register("mana_storage_cell_16k",
            () -> new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_16K, MANA_CELL_HOUSING.get(),
                    1.5f, 16, 128,
                    1, ManaKeyType.TYPE));
    public static final RegistryObject<Item> MANA_CELL_64K = ITEMS.register("mana_storage_cell_64k",
            () -> new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_64K, MANA_CELL_HOUSING.get(),
                    2.0f, 64, 512,
                    1, ManaKeyType.TYPE));
    public static final RegistryObject<Item> MANA_CELL_256K = ITEMS.register("mana_storage_cell_256k",
            () -> new BasicStorageCell(properties().stacksTo(1), AEItems.CELL_COMPONENT_256K, MANA_CELL_HOUSING.get(),
                    2.5f, 256,
                    2048, 1, ManaKeyType.TYPE));

    public static final RegistryObject<Item> PORTABLE_MANA_CELL_1K = ITEMS.register("portable_mana_storage_cell_1k",
            () -> new PortableCellItem(ManaKeyType.TYPE, ABMenus.PORTABLE_MANA_CELL_TYPE,
                    PortableCellItem.SIZE_1K, properties().stacksTo(1)));
    public static final RegistryObject<Item> PORTABLE_MANA_CELL_4K = ITEMS.register("portable_mana_storage_cell_4k",
            () -> new PortableCellItem(ManaKeyType.TYPE, ABMenus.PORTABLE_MANA_CELL_TYPE,
                    PortableCellItem.SIZE_4K, properties().stacksTo(1)));
    public static final RegistryObject<Item> PORTABLE_MANA_CELL_16K = ITEMS.register("portable_mana_storage_cell_16k",
            () -> new PortableCellItem(ManaKeyType.TYPE,
                    ABMenus.PORTABLE_MANA_CELL_TYPE, PortableCellItem.SIZE_16K, properties().stacksTo(1)));
    public static final RegistryObject<Item> PORTABLE_MANA_CELL_64K = ITEMS.register("portable_mana_storage_cell_64k",
            () -> new PortableCellItem(ManaKeyType.TYPE,
                    ABMenus.PORTABLE_MANA_CELL_TYPE, PortableCellItem.SIZE_64K, properties().stacksTo(1)));
    public static final RegistryObject<Item> PORTABLE_MANA_CELL_256K = ITEMS.register("portable_mana_storage_cell_256k",
            () -> new PortableCellItem(ManaKeyType.TYPE,
                    ABMenus.PORTABLE_MANA_CELL_TYPE, PortableCellItem.SIZE_256K, properties().stacksTo(1)));

    public static final RegistryObject<PartItem<ManaP2PTunnelPart>> MANA_P2P_TUNNEL = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ManaP2PTunnelPart.class));
        return ITEMS.register("mana_p2p_tunnel",
                () -> new PartItem<>(properties(), ManaP2PTunnelPart.class, ManaP2PTunnelPart::new));
    });

    public static RegistryObject<Item> get(Tier tier) {
        return switch (tier) {
            case _1K -> MANA_CELL_1K;
            case _4K -> MANA_CELL_4K;
            case _16K -> MANA_CELL_16K;
            case _64K -> MANA_CELL_64K;
            case _256K -> MANA_CELL_256K;
        };
    }

    public static RegistryObject<Item> getPortableCell(Tier tier) {
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
