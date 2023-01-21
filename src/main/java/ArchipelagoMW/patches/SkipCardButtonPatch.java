package ArchipelagoMW.patches;

import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.ui.buttons.SkipCardButton;
import javassist.CtBehavior;

public class SkipCardButtonPatch {

    @SpirePatch(clz = SkipCardButton.class, method = "update")
    public static class update {

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.MethodCallMatcher(AbstractDungeon.class,"closeCurrentScreen");
                return LineFinder.findAllInOrder(ctBehavior, match);
            }
        }

        @SpireInsertPatch(locator = Locator.class)
        public static void Insert() {
            if (ArchipelagoRewardScreen.APScreen) {
                AbstractDungeon.previousScreen = ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN;
            }
        }
    }

    @SpirePatch(clz = CardRewardScreen.class, method = "reopen")
    public static class reopen {
        public static void Postfix() {
            AbstractDungeon.overlayMenu.hideCombatPanels();
        }
    }
}
