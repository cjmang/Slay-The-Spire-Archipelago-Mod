package ArchipelagoMW.game.locations.shop;

import ArchipelagoMW.mod.Archipelago;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import io.github.archipelagomw.parts.NetworkItem;

public class APFakeCard extends CustomCard implements APShopItem {

    private final long locationId;
    private final NetworkItem item;

    public APFakeCard(long id, NetworkItem item, CardType type, CardColor color)
    {
        super(item.itemName, item.playerName + "_" + id, Archipelago.getModID() + "Resources/images/ui/APCampfire.png", 42, item.itemName, type, color, mapRarity(item.flags), CardTarget.NONE);
        this.locationId = id;
        this.item = item;
    }

    @Override
    public void upgrade() {

    }

    @Override
    public void use(AbstractPlayer abstractPlayer, AbstractMonster abstractMonster) {

    }

    @Override
    public AbstractCard makeCopy() {
        return new APFakeCard(locationId, item, type, color);
    }

    @Override
    public long getLocationId() {
        return locationId;
    }

    @Override
    public NetworkItem getItem() {
        return item;
    }

    private static CardRarity mapRarity(int flags)
    {
        if((flags & io.github.archipelagomw.flags.NetworkItem.ADVANCEMENT)  > 0)
        {
            return CardRarity.RARE;
        }
        if((flags & io.github.archipelagomw.flags.NetworkItem.USEFUL)  > 0)
        {
            return CardRarity.UNCOMMON;
        }
        return CardRarity.COMMON;
    }
}
