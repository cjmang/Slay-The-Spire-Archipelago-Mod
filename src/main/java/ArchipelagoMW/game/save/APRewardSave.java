package ArchipelagoMW.game.save;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardSave;

import java.util.ArrayList;
import java.util.List;

public class APRewardSave extends RewardSave {
    public ArrayList<String> cardIDs;
    public ArrayList<String> relicIds;
    public APRewardSave(String type, String id, int amount, int bonusGold) {
        super(type, id, amount, bonusGold);
    }

    public APRewardSave(String type, String id) {
        this(type, id, 0, 0);
    }


    public APRewardSave(String type, ArrayList<AbstractCard> cards) {
        super(type, null, 0, 0);
        cardIDs = new ArrayList<>();
        for (AbstractCard card : cards) {
            cardIDs.add(card.cardID);
        }
    }

    public APRewardSave(ArrayList<AbstractRelic> bossRelics, String type)
    {
        super(type, null, 0, 0);
        relicIds = new ArrayList<>();
        for(AbstractRelic relic : bossRelics)
        {
            relicIds.add(relic.relicId);
        }
    }
}
