package appbot.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class CreativeManaCellItem extends Item {
    public CreativeManaCellItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }
}
