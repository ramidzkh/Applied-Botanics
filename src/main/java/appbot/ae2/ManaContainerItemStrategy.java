package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.handler.BotaniaSounds;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;

@SuppressWarnings("UnstableApiUsage")
public class ManaContainerItemStrategy implements ContainerItemStrategy<ManaKey, ManaItem> {

    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        var item = BotaniaFabricCapabilities.MANA_ITEM.find(stack, Unit.INSTANCE);

        if (item != null) {
            return new GenericStack(ManaKey.KEY, item.getMana());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ManaItem findCarriedContext(Player player, AbstractContainerMenu menu) {
        return BotaniaFabricCapabilities.MANA_ITEM.find(menu.getCarried(), Unit.INSTANCE);
    }

    @Override
    public @Nullable ManaItem findPlayerSlotContext(Player player, int slot) {
        return BotaniaFabricCapabilities.MANA_ITEM.find(player.getInventory().getItem(slot), Unit.INSTANCE);
    }

    @Override
    public long extract(ManaItem item, ManaKey what, long amount, Actionable mode) {
        var extracted = (int) Math.min(amount, item.getMana());

        if (extracted > 0 && mode == Actionable.MODULATE) {
            item.addMana(-extracted);
        }

        return extracted;
    }

    @Override
    public long insert(ManaItem item, ManaKey what, long amount, Actionable mode) {
        var inserted = (int) Math.min(amount, item.getMaxMana() - item.getMana());

        if (inserted > 0 && mode == Actionable.MODULATE) {
            item.addMana(inserted);
        }

        return inserted;
    }

    @Override
    public void playFillSound(Player player, ManaKey what) {
        player.playNotifySound(BotaniaSounds.manaPoolCraft, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void playEmptySound(Player player, ManaKey what) {
        player.playNotifySound(BotaniaSounds.blackLotus, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public @Nullable GenericStack getExtractableContent(ManaItem item) {
        return new GenericStack(ManaKey.KEY, item.getMana());
    }
}
