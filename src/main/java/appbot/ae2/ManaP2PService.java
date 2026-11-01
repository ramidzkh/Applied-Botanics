package appbot.ae2;

import java.util.Collection;
import java.util.Random;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.core.AELog;

public class ManaP2PService implements IGridService, IGridServiceProvider {

    public static ManaP2PService get(IGrid grid) {
        return grid.getService(ManaP2PService.class);
    }

    private final Multimap<Short, ManaP2PTunnelPart> groups = HashMultimap.create();
    private final Random frequencyGenerator;

    public ManaP2PService(IGrid g) {
        frequencyGenerator = new Random(g.hashCode());
    }

    @Override
    public void removeNode(IGridNode node) {
        if (node.getOwner() instanceof ManaP2PTunnelPart tunnel) {
            groups.remove(tunnel.getFrequency(), tunnel);
        }
    }

    @Override
    public void addNode(IGridNode node, @Nullable CompoundTag savedData) {
        if (node.getOwner() instanceof ManaP2PTunnelPart tunnel) {
            groups.put(tunnel.getFrequency(), tunnel);
        }
    }

    public void updateFreq(ManaP2PTunnelPart t, short newFrequency) {
        groups.remove(t.getFrequency(), t);
        t.setFrequency(newFrequency);
        groups.put(t.getFrequency(), t);
    }

    public short newFrequency() {
        short newFrequency;
        int cycles = 0;

        do {
            newFrequency = (short) frequencyGenerator.nextInt(1 << 16);
            cycles++;
        } while (newFrequency == 0 || this.groups.containsKey(newFrequency));

        if (cycles > 25) {
            AELog.debug("Generating a new mP2P frequency '%1$d' took %2$d cycles", newFrequency, cycles);
        }

        return newFrequency;
    }

    public Collection<ManaP2PTunnelPart> getGroup(short freq) {
        return this.groups.get(freq);
    }
}
