package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaFabricCapabilities;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;

@SuppressWarnings("UnstableApiUsage")
public class ManaContainerItemStrategy implements ContainerItemStrategy<ManaKey, AbstractContainerMenu> {

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
    public @Nullable AbstractContainerMenu findCarriedContext(Player player, AbstractContainerMenu menu) {
        return menu;
    }

    @Override
    public long extract(AbstractContainerMenu context, ManaKey what, long amount, Actionable mode) {
        var item = BotaniaFabricCapabilities.MANA_ITEM.find(context.getCarried(), Unit.INSTANCE);

        if (item != null) {
            var extracted = (int) Math.min(amount, item.getMana());

            if (extracted > 0 && mode == Actionable.MODULATE) {
                item.addMana(-extracted);
            }

            return extracted;
        }

        return 0;
    }

    @Override
    public long insert(AbstractContainerMenu context, ManaKey what, long amount, Actionable mode) {
        var item = BotaniaFabricCapabilities.MANA_ITEM.find(context.getCarried(), Unit.INSTANCE);

        if (item != null) {
            var inserted = (int) Math.min(amount, item.getMaxMana() - item.getMana());

            if (inserted > 0 && mode == Actionable.MODULATE) {
                item.addMana(inserted);
            }

            return inserted;
        }

        return 0;
    }

    @Override
    public void playFillSound(Player player, ManaKey what) {
    }

    @Override
    public void playEmptySound(Player player, ManaKey what) {
    }

    @Override
    public @Nullable GenericStack getExtractableContent(AbstractContainerMenu context) {
        return getContainedStack(context.getCarried());
    }
}
