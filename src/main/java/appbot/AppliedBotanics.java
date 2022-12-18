package appbot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appbot.ae2.*;
import appbot.ae2.MEManaReceiver;
import appbot.client.AppliedBotanicsClient;
import appbot.data.ABDataGenerator;
import vazkii.botania.api.BotaniaForgeCapabilities;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.client.StorageCellModels;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.upgrades.Upgrades;
import appeng.capabilities.Capabilities;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.parts.automation.ForgeExternalStorageStrategy;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.parts.automation.StorageExportStrategy;
import appeng.parts.automation.StorageImportStrategy;

@Mod(AppliedBotanics.MOD_ID)
@SuppressWarnings("UnstableApiUsage")
public class AppliedBotanics {

    public static final String MOD_ID = "appbot";

    public AppliedBotanics() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        ABBlocks.initialize(bus);
        ABItems.initialize(bus);
        ABMenus.initialize(bus);

        bus.addListener(ABDataGenerator::onGatherData);

        bus.addGenericListener(AEKeyType.class, (RegistryEvent.Register<AEKeyType> event) -> {
            AEKeyTypes.register(ManaKeyType.TYPE);
        });

        StackWorldBehaviors.registerImportStrategy(ManaKeyType.TYPE,
                (level, fromPos, fromSide) -> new StorageImportStrategy<>(BotaniaForgeCapabilities.MANA_RECEIVER,
                        ManaHandlerStrategy.INSTANCE, level, fromPos, fromSide));
        StackWorldBehaviors.registerExportStrategy(ManaKeyType.TYPE,
                (level, fromPos, fromSide) -> new StorageExportStrategy<>(BotaniaForgeCapabilities.MANA_RECEIVER,
                        ManaHandlerStrategy.INSTANCE, level, fromPos, fromSide) {
                });
        StackWorldBehaviors.registerExternalStorageStrategy(ManaKeyType.TYPE,
                (level, fromPos, fromSide) -> new ForgeExternalStorageStrategy<>(BotaniaForgeCapabilities.MANA_RECEIVER,
                        ManaHandlerStrategy.INSTANCE, level, fromPos, fromSide));

        ContainerItemStrategy.register(ManaKeyType.TYPE, ManaKey.class, new ManaContainerItemStrategy());
        GenericSlotCapacities.register(ManaKeyType.TYPE, GenericSlotCapacities.getMap().get(AEKeyType.fluids()));

        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::initializeCapabilities);

        bus.addListener((FMLCommonSetupEvent event) -> {
            event.enqueueWork(this::initializeModels);
            event.enqueueWork(this::initializeUpgrades);
            event.enqueueWork(this::initializeAttunement);
        });

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AppliedBotanicsClient::initialize);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void initializeCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        var blockEntity = event.getObject();

        event.addCapability(id("generic_inv_wrapper"), new ICapabilityProvider() {
            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                if (capability == BotaniaForgeCapabilities.SPARK_ATTACHABLE && blockEntity instanceof IPartHost host
                        && host.getPart(side)instanceof ManaP2PTunnelPart p2p) {
                    var sparkAttachable = p2p.getSparkAttachable();

                    if (sparkAttachable != null) {
                        return LazyOptional.of(() -> sparkAttachable).cast();
                    }
                }

                if (capability == BotaniaForgeCapabilities.MANA_RECEIVER) {
                    return blockEntity.getCapability(Capabilities.GENERIC_INTERNAL_INV, side)
                            .lazyMap(inventory -> new MEManaReceiver(inventory, blockEntity.getLevel(),
                                    blockEntity.getBlockPos()))
                            .cast();
                } else if (capability == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
                    return blockEntity.getCapability(Capabilities.GENERIC_INTERNAL_INV, side)
                            .lazyMap(inventory -> new MEManaReceiver(inventory, blockEntity.getLevel(),
                                    blockEntity.getBlockPos()))
                            .cast();
                }

                return LazyOptional.empty();
            }
        });
    }

    private void initializeModels() {
        StorageCells.addCellGuiHandler(new ICellGuiHandler() {
            @Override
            public boolean isSpecializedFor(ItemStack cell) {
                return cell.getItem()instanceof IBasicCellItem basicCellItem
                        && basicCellItem.getKeyType() == ManaKeyType.TYPE;
            }

            @Override
            public void openChestGui(Player player, IChestOrDrive chest, ICellHandler cellHandler, ItemStack cell) {
                chest.getUp();
                MenuOpener.open(MEStorageMenu.TYPE, player,
                        MenuLocators.forBlockEntity((BlockEntity) chest));
            }
        });

        StorageCellModels.registerModel(ABItems.MANA_CELL_CREATIVE::get,
                AppEng.makeId("block/drive/cells/creative_cell"));

        for (var tier : ABItems.Tier.values()) {
            var cell = ABItems.get(tier);
            var portable = ABItems.getPortableCell(tier);

            registerCell(cell::get, portable::get, cell.getId().getPath());
        }
    }

    private void registerCell(ItemLike cell, ItemLike portableCell, String path) {
        StorageCellModels.registerModel(cell, id("block/drive/cells/" + path));
        StorageCellModels.registerModel(portableCell, id("block/drive/cells/" + path));
    }

    private void initializeUpgrades() {
        var storageCellGroup = GuiText.StorageCells.getTranslationKey();
        var portableStorageCellGroup = GuiText.PortableCells.getTranslationKey();

        for (var tier : ABItems.Tier.values()) {
            var cell = ABItems.get(tier);
            var portableCell = ABItems.getPortableCell(tier);

            Upgrades.add(AEItems.INVERTER_CARD, cell::get, 1, storageCellGroup);
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, cell::get, 1, storageCellGroup);
            Upgrades.add(AEItems.VOID_CARD, cell::get, 1, storageCellGroup);

            Upgrades.add(AEItems.INVERTER_CARD, portableCell::get, 1, portableStorageCellGroup);
            Upgrades.add(AEItems.ENERGY_CARD, portableCell::get, 2, portableStorageCellGroup);
        }
    }

    private void initializeAttunement() {
        P2PTunnelAttunement.registerAttunementTag(ABItems.MANA_P2P_TUNNEL::get);
    }
}
