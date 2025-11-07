package ArchipelagoMW.game.items.patches;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.items.ui.ArchipelagoRewardScreen;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.rewards.RewardItem;

import java.util.List;

public class LoadSavePatch {

    @SpirePatch(clz= CardCrawlGame.class, method="loadPlayerSave", paramtypez = AbstractPlayer.class)
    public static class AscensionDownPatch
    {
        public static void Postfix()
        {
            List<RewardItem> items = APContext.getContext().getAscensionManager().checkAndDecrementAscensions();
            if(!items.isEmpty())
            {
                ArchipelagoRewardScreen.rewardsQueued += items.size();
                ArchipelagoRewardScreen.rewards.addAll(items);
            }
        }
    }
}
