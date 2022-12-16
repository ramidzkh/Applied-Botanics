package appbot.item;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import appbot.ABItems;
import appbot.item.cell.IManaCellItem;

import appeng.api.storage.StorageCells;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;

public class ManaCellItem extends AEBaseItem implements IManaCellItem, AEToolItem {

    private final ItemLike coreItem;
    private final int totalBytes;
    private final double idleDrain;

    public ManaCellItem(Properties properties, ItemLike coreItem, int kilobytes, double idleDrain) {
        super(properties.stacksTo(1));
        this.coreItem = coreItem;
        this.totalBytes = kilobytes * 1000;
        this.idleDrain = idleDrain;
    }

    @Override
    public long getTotalBytes() {
        return this.totalBytes;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        this.disassembleDrive(player.getItemInHand(hand), level, player);
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    private boolean disassembleDrive(ItemStack stack, Level level, Player player) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (level.isClientSide()) {
                return false;
            }

            var playerInventory = player.getInventory();
            var inv = StorageCells.getCellInventory(stack, null);

            if (inv != null && playerInventory.getSelected() == stack) {
                var list = inv.getAvailableStacks();
                if (list.isEmpty()) {
                    playerInventory.setItem(playerInventory.selected, ItemStack.EMPTY);
                    playerInventory.placeItemBackInInventory(new ItemStack(coreItem));
                    playerInventory.placeItemBackInInventory(new ItemStack(ABItems.MANA_CELL_HOUSING));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.disassembleDrive(stack, context.getLevel(), context.getPlayer())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide())
                : InteractionResult.PASS;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack is, @Nullable Level level, List<Component> lines, TooltipFlag tooltipFlag) {
        addCellInformationToTooltip(is, lines);
    }
}
