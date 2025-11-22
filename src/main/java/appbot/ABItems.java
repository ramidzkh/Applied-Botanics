package appbot;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import appbot.ae2.ManaP2PTunnelPart;
import appbot.item.ManaCellItem;
import appbot.item.PortableManaCellItem;
import vazkii.botania.common.item.BotaniaItems;

import appeng.api.config.Actionable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.parts.PartModels;
import appeng.core.definitions.AEItems;
import appeng.items.parts.PartItem;

public class ABItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AppliedBotanics.MOD_ID);

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            AppliedBotanics.MOD_ID);

    public static final DeferredItem<Item> FLUIX_MANA_POOL = ITEMS.register("fluix_mana_pool",
            () -> new BlockItem(ABBlocks.FLUIX_MANA_POOL.get(), BotaniaItems.defaultBuilder()));

    public static final DeferredItem<Item> MANA_CELL_HOUSING = ITEMS.register("mana_cell_housing",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> MANA_CELL_1K = ITEMS.register("mana_cell_1k",
            () -> new ManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    AEItems.CELL_COMPONENT_1K,
                    1, 0.5f));
    public static final DeferredItem<Item> MANA_CELL_4K = ITEMS.register("mana_cell_4k",
            () -> new ManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    AEItems.CELL_COMPONENT_4K,
                    4, 1.0f));
    public static final DeferredItem<Item> MANA_CELL_16K = ITEMS.register("mana_cell_16k",
            () -> new ManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    AEItems.CELL_COMPONENT_16K,
                    16, 1.5f));
    public static final DeferredItem<Item> MANA_CELL_64K = ITEMS.register("mana_cell_64k",
            () -> new ManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    AEItems.CELL_COMPONENT_64K,
                    64, 2.0f));
    public static final DeferredItem<Item> MANA_CELL_256K = ITEMS.register("mana_cell_256k",
            () -> new ManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    AEItems.CELL_COMPONENT_256K,
                    256, 2.5f));

    public static final DeferredItem<Item> PORTABLE_MANA_CELL_1K = ITEMS.register("portable_mana_cell_1k",
            () -> new PortableManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    1, 0.5));
    public static final DeferredItem<Item> PORTABLE_MANA_CELL_4K = ITEMS.register("portable_mana_cell_4k",
            () -> new PortableManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    4, 1.0));
    public static final DeferredItem<Item> PORTABLE_MANA_CELL_16K = ITEMS.register("portable_mana_cell_16k",
            () -> new PortableManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    16, 1.5));
    public static final DeferredItem<Item> PORTABLE_MANA_CELL_64K = ITEMS.register("portable_mana_cell_64k",
            () -> new PortableManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    64, 2.0));
    public static final DeferredItem<Item> PORTABLE_MANA_CELL_256K = ITEMS.register("portable_mana_cell_256k",
            () -> new PortableManaCellItem(new Item.Properties().stacksTo(1).component(AppliedBotanicsForge.MANA, 0L),
                    256, 2.5));

    public static final DeferredItem<PartItem<ManaP2PTunnelPart>> MANA_P2P_TUNNEL = Util.make(() -> {
        PartModels.registerModels(ManaP2PTunnelPart.getModels().stream().flatMap(x -> x.getModels().stream()).toList());
        return ITEMS.register("mana_p2p_tunnel",
                () -> new PartItem<>(new Item.Properties(), ManaP2PTunnelPart.class, ManaP2PTunnelPart::new));
    });

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register(
            "tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.appbot.tab"))
                    .icon(() -> new ItemStack(ABItems.FLUIX_MANA_POOL.get()))
                    .displayItems((context, entries) -> {
                        for (var holder : ITEMS.getEntries()) {
                            var item = holder.get();
                            entries.accept(item);

                            if (item instanceof IAEItemPowerStorage storage) {
                                var stack = new ItemStack(item);
                                storage.injectAEPower(stack, storage.getAEMaxPower(stack), Actionable.MODULATE);
                                entries.accept(stack);
                            }
                        }
                    })
                    .build());

    public static void initialize(IEventBus bus) {
        ITEMS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }

    public static DeferredItem<Item> get(Tier tier) {
        return switch (tier) {
            case _1K -> MANA_CELL_1K;
            case _4K -> MANA_CELL_4K;
            case _16K -> MANA_CELL_16K;
            case _64K -> MANA_CELL_64K;
            case _256K -> MANA_CELL_256K;
        };
    }

    public static DeferredItem<Item> getPortableCell(Tier tier) {
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
