package appbot.forge;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import appbot.AppliedBotanics;
import appbot.ae2.*;
import appbot.botania.MECorporeaNode;
import appbot.forge.ae2.ManaP2PTunnelPart;
import appbot.forge.client.AppliedBotanicsClient;
import appbot.item.cell.CreativeManaCellHandler;
import appbot.item.cell.ManaCellHandler;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import appeng.capabilities.Capabilities;
import appeng.parts.automation.StackWorldBehaviors;

@Mod(AppliedBotanics.MOD_ID)
@SuppressWarnings("UnstableApiUsage")
public class AppliedBotanicsForge {

    public AppliedBotanicsForge() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        ABBlocks.initialize(bus);
        ABItems.initialize(bus);
        ABMenus.initialize(bus);

        bus.addListener((RegisterEvent event) -> {
            if (!event.getRegistryKey().equals(Registry.BLOCK_REGISTRY)) {
                return;
            }

            AEKeyTypes.register(ManaKeyType.TYPE);
        });

        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, (AttachCapabilitiesEvent<BlockEntity> event) -> {
            var blockEntity = event.getObject();

            event.addCapability(AppliedBotanics.id("generic_inv_wrapper"), new ICapabilityProvider() {
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

                    if (capability == BotaniaForgeCapabilities.MANA_RECEIVER
                            || capability == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
                        return blockEntity.getCapability(Capabilities.GENERIC_INTERNAL_INV, side)
                                .lazyMap(inventory -> new ManaGenericStackInvStorage(inventory, blockEntity.getLevel(),
                                        blockEntity.getBlockPos()))
                                .cast();
                    }

                    return LazyOptional.empty();
                }
            });
        });
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, (AttachCapabilitiesEvent<ItemStack> event) -> {
            var item = MEStorageManaItem.forItem(event.getObject());

            if (item != null) {
                event.addCapability(AppliedBotanics.id("mana_item"), new ICapabilityProvider() {
                    @Override
                    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                            @Nullable Direction arg) {
                        if (capability == BotaniaForgeCapabilities.MANA_ITEM) {
                            return LazyOptional.of(() -> item).cast();
                        }

                        return LazyOptional.empty();
                    }
                });
            }
        });

        StackWorldBehaviors.registerImportStrategy(ManaKeyType.TYPE, ManaStorageImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(ManaKeyType.TYPE, ManaStorageExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(ManaKeyType.TYPE, ManaExternalStorageStrategy::new);

        ContainerItemStrategy.register(ManaKeyType.TYPE, ManaKey.class, new ManaContainerItemStrategy());
        GenericSlotCapacities.register(ManaKeyType.TYPE, 500000L);

        StorageCells.addCellHandler(ManaCellHandler.INSTANCE);
        StorageCells.addCellHandler(new CreativeManaCellHandler());

        bus.addListener((FMLCommonSetupEvent event) -> {
            CorporeaNodeDetectors.register(MECorporeaNode::getNode);

            event.enqueueWork(() -> {
                P2PTunnelAttunement.registerAttunementTag(ABItems.MANA_P2P_TUNNEL.get());
            });
        });

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AppliedBotanicsClient::initialize);
    }
}
