package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appbot.storage.Apis;
import appbot.storage.ManaVariant;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;

@SuppressWarnings("UnstableApiUsage")
public class ManaContainerItemStrategy implements ContainerItemStrategy<ManaKey, Storage<ManaVariant>> {

    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        var content = StorageUtil.findExtractableContent(ContainerItemContext.withInitial(stack).find(Apis.ITEM), null);

        if (content != null) {
            return new GenericStack(ManaKey.KEY, content.amount());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Storage<ManaVariant> findCarriedContext(Player player, AbstractContainerMenu menu) {
        return ContainerItemContext.ofPlayerCursor(player, menu).find(Apis.ITEM);
    }

    @Override
    public long extract(Storage<ManaVariant> context, ManaKey what, long amount, Actionable mode) {
        try (var tx = Transaction.openOuter()) {
            var extracted = context.extract(ManaVariant.VARIANT, amount, tx);

            if (mode == Actionable.MODULATE) {
                tx.commit();
            }

            return extracted;
        }
    }

    @Override
    public long insert(Storage<ManaVariant> context, ManaKey what, long amount, Actionable mode) {
        try (var tx = Transaction.openOuter()) {
            var inserted = context.insert(ManaVariant.VARIANT, amount, tx);

            if (mode == Actionable.MODULATE) {
                tx.commit();
            }

            return inserted;
        }
    }

    @Override
    public void playFillSound(Player player, ManaKey what) {
    }

    @Override
    public void playEmptySound(Player player, ManaKey what) {
    }

    @Override
    public @Nullable GenericStack getExtractableContent(Storage<ManaVariant> context) {
        var resourceAmount = StorageUtil.findExtractableContent(context, null);

        if (resourceAmount == null) {
            return null;
        }

        return new GenericStack(ManaKey.KEY, resourceAmount.amount());
    }
}
