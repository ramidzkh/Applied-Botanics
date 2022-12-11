package appbot.item;

import java.util.List;
import java.util.Objects;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appbot.ABMenus;
import appbot.AppliedBotanics;
import appbot.item.cell.IManaCellItem;

import appeng.api.upgrades.Upgrades;
import appeng.items.tools.powered.AbstractPortableCell;

public class PortableManaCellItem extends AbstractPortableCell implements IManaCellItem {

    private final int totalBytes;
    private final double idleDrain;

    public PortableManaCellItem(Properties props, int kilobytes, double idleDrain) {
        super(ABMenus.PORTABLE_MANA_CELL_TYPE, props);
        this.totalBytes = kilobytes * 1024;
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
    public ResourceLocation getRecipeId() {
        return AppliedBotanics.id(Objects.requireNonNull(getRegistryName()).getPath());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80d + 80d * Upgrades.getEnergyCardMultiplier(getUpgrades(stack));
    }
}
