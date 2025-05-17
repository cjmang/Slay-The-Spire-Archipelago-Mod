package ArchipelagoMW.game.locations.shop;

import ArchipelagoMW.client.APContext;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import dev.koifysh.archipelago.parts.NetworkItem;

/**
 * TODO:
 * 1. Create fake potion
 * 2. Add fake potion to shop when relevant
 * 3. Add price setup
 * 4. Add acquisition interception
 */
public class APFakePotion extends AbstractPotion implements APShopItem {


    private static PotionRarity mapRarity(int flags)
    {
        if((dev.koifysh.archipelago.flags.NetworkItem.ADVANCEMENT & flags) > 0)
        {
            return PotionRarity.RARE;
        }
        else if((dev.koifysh.archipelago.flags.NetworkItem.USEFUL & flags) > 0)
        {
            return PotionRarity.UNCOMMON;
        }
        return PotionRarity.COMMON;
    }

    private final long locationId;
    private final NetworkItem item;

    public APFakePotion(long locationId, NetworkItem item) {
        // TODO: huh? why do potions have 5 images associated with them?!
        super(item.itemName, item.itemName, mapRarity(item.flags), PotionSize.SPHERE, PotionColor.FRUIT);
        this.locationId = locationId;
        this.item = item;
    }

    @Override
    public long getLocationId() {
        return locationId;
    }

    @Override
    public NetworkItem getItem() {
        return item;
    }

    @Override
    public void use(AbstractCreature abstractCreature) {

    }

    @Override
    public int getPotency(int i) {
        return 0;
    }

    @Override
    public AbstractPotion makeCopy() {
        return null;
    }

    public int getPrice()
    {
        return APContext.getContext().getShopManager().getPotionPrice(this.rarity);
    }
}
