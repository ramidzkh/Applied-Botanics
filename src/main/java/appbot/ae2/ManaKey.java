package appbot.ae2;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.client.fx.WispParticleData;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

public class ManaKey extends AEKey {

    public static final AEKey KEY = new ManaKey();

    private ManaKey() {
        super(ManaKeyType.MANA);
    }

    @Override
    public AEKeyType getType() {
        return ManaKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public CompoundTag toTag() {
        return new CompoundTag();
    }

    @Override
    public Object getPrimaryKey() {
        return this;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        float r = (float) amount / (amount + 1);
        float g = 0.5F;
        float b = 0.2F;

        float w = 0.15F;
        float h = 0.05F;
        double x = pos.getX() + 0.5 + (Math.random() - 0.5) * w;
        double y = pos.getY() + 0.25 + (Math.random() - 0.5) * h;
        double z = pos.getZ() + 0.5 + (Math.random() - 0.5) * w;

        float s = 0.2F + (float) Math.random() * 0.1F;
        float m = 0.03F + (float) Math.random() * 0.015F;

        WispParticleData data = WispParticleData.wisp(s, r, g, b, 1);
        level.addParticle(data, x, y, z, 0, m, 0);
    }
}
