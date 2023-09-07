package appbot.common.ae2;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.client.fx.WispParticleData;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

public class ManaKey extends AEKey {

    public static final AEKey KEY = new ManaKey();

    private static final ResourceLocation ID = new ResourceLocation("botania", "mana");

    private ManaKey() {
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
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var times = Math.min((amount + 999) / 1000, 10);

        for (var i = 0; i < times; i++) {
            var red = (float) i / times;
            var green = 0.5F;
            var blue = 0.2F;

            var data = WispParticleData.wisp((float) Math.random() / 3F, red, green, blue, 2F);
            serverLevel.sendParticles(data, pos.getX() + 0.3 + Math.random() * 0.5,
                    pos.getY() + 0.6 + Math.random() * 0.25, pos.getZ() + Math.random(), 8, 0.1, 0.1, 0.1, 0.04);
        }
    }

    @Override
    protected Component computeDisplayName() {
        return ManaKeyType.MANA;
    }
}
