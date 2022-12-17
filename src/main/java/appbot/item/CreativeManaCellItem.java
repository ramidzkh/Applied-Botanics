package appbot.item;

import net.minecraft.world.item.Rarity;

import appeng.items.AEBaseItem;

public class CreativeManaCellItem extends AEBaseItem {
    public CreativeManaCellItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }
}
