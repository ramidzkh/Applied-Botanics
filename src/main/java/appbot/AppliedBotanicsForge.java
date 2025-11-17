package appbot;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import appbot.ae2.*;
import appbot.botania.MECorporeaNode;
import appbot.data.ABDataGenerator;
import appbot.item.cell.ManaCellHandler;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import appeng.items.tools.powered.AbstractPortableCell;
import appeng.parts.automation.StackWorldBehaviors;

@Mod(AppliedBotanics.MOD_ID)
@SuppressWarnings("UnstableApiUsage")
public class AppliedBotanicsForge {

    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, AppliedBotanics.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> MANA = DATA_COMPONENTS
            .registerComponentType("mana", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(StreamCodec.of(FriendlyByteBuf::writeVarLong, FriendlyByteBuf::readVarLong))
                    .cacheEncoding());

    public AppliedBotanicsForge(IEventBus bus) {
        ABBlocks.initialize(bus);
        ABItems.initialize(bus);
        ABMenus.initialize(bus);
        DATA_COMPONENTS.register(bus);

        bus.addListener(ABDataGenerator::onInitializeDataGenerator);

        bus.addListener((RegisterEvent event) -> {
            if (!event.getRegistryKey().equals(Registries.BLOCK)) {
                return;
            }

            AEKeyTypes.register(ManaKeyType.TYPE);
        });

        bus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlock(AECapabilities.IN_WORLD_GRID_NODE_HOST, ($1, $2, $3, be, $4) -> {
                if (be instanceof IInWorldGridNodeHost a) {
                    return a;
                } else {
                    return null;
                }
            }, ABBlocks.FLUIX_MANA_POOL.get());
        });
        bus.addListener(EventPriority.LOWEST, this::registerGenericAdapters);
        bus.addListener((RegisterPartCapabilitiesEvent event) -> {
            event.register(BotaniaForgeCapabilities.MANA_RECEIVER, (object, context) -> object.getExposedApi(),
                    ManaP2PTunnelPart.class);
            event.register(BotaniaForgeCapabilities.SPARK_ATTACHABLE, (object, context) -> object.getSparkAttachable(),
                    ManaP2PTunnelPart.class);
        });

        StackWorldBehaviors.registerImportStrategy(ManaKeyType.TYPE, ManaStorageImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(ManaKeyType.TYPE, ManaStorageExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(ManaKeyType.TYPE, ManaExternalStorageStrategy::new);

        ContainerItemStrategy.register(ManaKeyType.TYPE, ManaKey.class, new ManaContainerItemStrategy());
        GenericSlotCapacities.register(ManaKeyType.TYPE, (long) ManaPoolBlock.MAX_MANA_DILUTED);

        StorageCells.addCellHandler(ManaCellHandler.INSTANCE);

        bus.addListener((FMLCommonSetupEvent event) -> {
            CorporeaNodeDetectors.register(MECorporeaNode::getNode);

            event.enqueueWork(() -> {
                P2PTunnelAttunement.registerAttunementTag(ABItems.MANA_P2P_TUNNEL.get());
            });
        });
    }

    private void registerGenericAdapters(RegisterCapabilitiesEvent event) {
        for (var block : BuiltInRegistries.BLOCK) {
            if (!event.isBlockRegistered(AECapabilities.GENERIC_INTERNAL_INV, block)) {
                continue;
            }

            event.registerBlock(BotaniaForgeCapabilities.MANA_RECEIVER, (level, pos, state, blockEntity, context) -> {
                var genericInv = level.getCapability(AECapabilities.GENERIC_INTERNAL_INV, pos, state, blockEntity,
                        context);
                if (genericInv != null) {
                    return new ManaGenericStackInvStorage(genericInv, level, pos);
                }
                return null;
            }, block);
            event.registerBlock(BotaniaForgeCapabilities.SPARK_ATTACHABLE,
                    (level, pos, state, blockEntity, context) -> {
                        var genericInv = level.getCapability(AECapabilities.GENERIC_INTERNAL_INV, pos, state,
                                blockEntity, context);
                        if (genericInv != null) {
                            return new ManaGenericStackInvStorage(genericInv, level, pos);
                        }
                        return null;
                    }, block);
        }

        for (var item : BuiltInRegistries.ITEM) {
            if (!(item instanceof AbstractPortableCell)) {
                continue;
            }

            event.registerItem(BotaniaForgeCapabilities.MANA_ITEM, (object, context) -> {
                return MEStorageManaItem.forItem(object);
            }, item);
        }
    }
}
