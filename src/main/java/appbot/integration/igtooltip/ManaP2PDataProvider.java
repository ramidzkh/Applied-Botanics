package appbot.integration.igtooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;

import appbot.ae2.ManaP2PTunnelPart;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.localization.InGameTooltip;
import appeng.util.Platform;

@SuppressWarnings("UnstableApiUsage")
public final class ManaP2PDataProvider
        implements BodyProvider<ManaP2PTunnelPart>, ServerDataProvider<ManaP2PTunnelPart> {

    public static final String TAG_P2P_FREQUENCY = "mp2pFrequency";
    public static final String TAG_P2P_OUTPUT = "mp2pOutput";
    public static final String TAG_P2P_PEERS = "mp2pPeers";
    public static final String TAG_P2P_FREQUENCY_NAME = "mp2pFrequencyName";

    @Override
    public void buildTooltip(ManaP2PTunnelPart object, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();
        var output = serverData.getBoolean(TAG_P2P_OUTPUT);
        var freq = serverData.getShort(TAG_P2P_FREQUENCY);
        var peers = serverData.getInt(TAG_P2P_PEERS);
        var nameTag = serverData.get(TAG_P2P_FREQUENCY_NAME);

        tooltip.addLine(Component.literal(output ? "Output + Input" : "Input"));

        if (freq != 0) {
            tooltip.addLine(Component.literal("Linked - %d peers".formatted(peers)));

            // Show the frequency and name of the frequency if it exists
            var freqTooltip = Platform.p2p().toColoredHexString(freq).withStyle(ChatFormatting.BOLD);

            if (nameTag != null) {
                var freqName = ComponentSerialization.CODEC
                        .parse(context.registries().createSerializationContext(NbtOps.INSTANCE), nameTag).getOrThrow();
                freqTooltip = freqName.copy().append(" (").append(freqTooltip).append(")");
            }

            tooltip.addLine(InGameTooltip.P2PFrequency.text(freqTooltip));
        } else if (nameTag != null) {
            var freqName = ComponentSerialization.CODEC
                    .parse(context.registries().createSerializationContext(NbtOps.INSTANCE), nameTag).getOrThrow();
            tooltip.addLine(Component.literal("Unlinked - ").append(freqName));
        }
    }

    @Override
    public void provideServerData(Player player, ManaP2PTunnelPart part, CompoundTag serverData) {
        serverData.putBoolean(TAG_P2P_OUTPUT, part.isOutput());
        serverData.putShort(TAG_P2P_FREQUENCY, part.getFrequency());

        if (part.getMainNode().isOnline() && part.getFrequency() != 0) {
            serverData.putInt(TAG_P2P_PEERS, part.getGroup().size());
        }

        var name = part.getCustomName();

        if (name != null) {
            serverData.put(TAG_P2P_FREQUENCY_NAME,
                    ComponentSerialization.CODEC
                            .encodeStart(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), name)
                            .getOrThrow());
        }
    }
}
