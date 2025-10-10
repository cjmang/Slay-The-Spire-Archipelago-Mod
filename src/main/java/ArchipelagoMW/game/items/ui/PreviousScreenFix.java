package ArchipelagoMW.game.items.ui;

import ArchipelagoMW.game.items.patches.RewardItemPatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import javassist.CtBehavior;

import java.util.ArrayList;

public class PreviousScreenFix {

    @SpirePatch(clz= AbstractDungeon.class, method="openPreviousScreen", paramtypez = AbstractDungeon.CurrentScreen.class)
    public static class RedirectAPReward {

        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn<Void> setRewardScreen(AbstractDungeon.CurrentScreen s)
        {
            if(RewardItemPatch.CustomFields.apReward.get(AbstractDungeon.cardRewardScreen.rItem))
            {
                AbstractDungeon.previousScreen = ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        public static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher matcher = new Matcher.FieldAccessMatcher(AbstractDungeon.CurrentScreen.class, "COMBAT_REWARD");
                return LineFinder.findInOrder(ctBehavior, new ArrayList<>(), matcher);
            }
        }
    }
}
