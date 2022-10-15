package appbot.ae2;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.ItemManaTablet;
import vazkii.botania.xplat.IXplatAbstractions;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;

@SuppressWarnings("UnstableApiUsage")
public class ManaContainerItemStrategy implements ContainerItemStrategy<ManaKey, ManaContainerItemStrategy.Context> {

    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        var item = IXplatAbstractions.INSTANCE.findManaItem(stack);

        if (item != null) {
            return new GenericStack(ManaKey.KEY, item.getMana());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Context findCarriedContext(Player player, AbstractContainerMenu menu) {
        if (IXplatAbstractions.INSTANCE.findManaItem(menu.getCarried()) != null) {
            return new Context(player, menu);
        }

        return null;
    }

    @Override
    public long extract(Context context, ManaKey what, long amount, Actionable mode) {
        var held = context.menu.getCarried();
        var copy = held.copy();
        var item = IXplatAbstractions.INSTANCE.findManaItem(copy);

        if (item == null) {
            return 0;
        }

        var before = item.getMana();
        item.addMana(-Ints.saturatedCast(amount));

        if (mode == Actionable.MODULATE) {
            held.shrink(1);

            if (held.isEmpty()) {
                context.menu.setCarried(copy);
            } else {
                context.player.getInventory().placeItemBackInInventory(copy);
            }
        }

        if (ItemManaTablet.isStackCreative(copy)) {
            return amount;
        } else {
            return before - item.getMana();
        }
    }

    @Override
    public long insert(Context context, ManaKey what, long amount, Actionable mode) {
        var held = context.menu.getCarried();
        var copy = held.copy();
        var item = IXplatAbstractions.INSTANCE.findManaItem(copy);

        if (item == null) {
            return 0;
        }

        var before = item.getMana();
        item.addMana(Ints.saturatedCast(amount));

        if (mode == Actionable.MODULATE) {
            held.shrink(1);

            if (held.isEmpty()) {
                context.menu.setCarried(copy);
            } else {
                context.player.getInventory().placeItemBackInInventory(copy);
            }
        }

        return item.getMana() - before;
    }

    @Override
    public void playFillSound(Player player, ManaKey what) {
    }

    @Override
    public void playEmptySound(Player player, ManaKey what) {
    }

    @Override
    public @Nullable GenericStack getExtractableContent(Context context) {
        return getContainedStack(context.menu.getCarried());
    }

    record Context(Player player, AbstractContainerMenu menu) {
    }
}
