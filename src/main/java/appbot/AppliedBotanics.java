package appbot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import appbot.ae2.ManaP2PTunnelPart;
import appbot.botania.MECorporeaNode;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartModels;
import appeng.core.CreativeTab;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;

@Mod(AppliedBotanics.MOD_ID)
public class AppliedBotanics {

    public static final String MOD_ID = "appbot";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            AppliedBotanics.MOD_ID);

    public static final RegistryObject<PartItem<ManaP2PTunnelPart>> MANA_P2P_TUNNEL = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ManaP2PTunnelPart.class));
        return ITEMS.register("mana_p2p_tunnel",
                () -> new PartItem<>(new Item.Properties().tab(CreativeTab.INSTANCE), ManaP2PTunnelPart.class,
                        ManaP2PTunnelPart::new));
    });

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public AppliedBotanics() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(bus);

        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            P2PTunnelAttunement.addItemByMod(BotaniaAPI.MODID, MANA_P2P_TUNNEL::get);
            CorporeaNodeDetectors.register(MECorporeaNode::getNode);
        }));

        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, (AttachCapabilitiesEvent<BlockEntity> event) -> {
            if (event.getObject()instanceof IPartHost host) {
                event.addCapability(id("mana_p2p"), new ICapabilityProvider() {
                    @NotNull
                    @Override
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE
                                && host.getPart(side)instanceof ManaP2PTunnelPart part) {
                            var attachable = part.getSparkAttachable();

                            if (attachable != null) {
                                return LazyOptional.of(() -> attachable).cast();
                            }
                        }

                        return LazyOptional.empty();
                    }
                });
            }
        });
    }
}
