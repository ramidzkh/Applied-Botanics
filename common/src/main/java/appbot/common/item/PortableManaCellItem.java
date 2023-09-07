package appbot.common.item;

import java.util.List;
import java.util.Objects;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appbot.AB;
import appbot.common.item.cell.IManaCellItem;

import appeng.api.upgrades.Upgrades;
import appeng.items.tools.powered.AbstractPortableCell;

public class PortableManaCellItem extends AbstractPortableCell implements IManaCellItem {

    private final int totalBytes;
    private final double idleDrain;

    public PortableManaCellItem(Properties props, int kilobytes, double idleDrain) {
        super(AB.getInstance().portableCellMenu(), props);
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
    public ResourceLocation getRecipeId() {
        String path = Objects.requireNonNull(getRegistryName()).getPath();
        return AB.id(path);
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
