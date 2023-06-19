package ArchipelagoMW.patches;

import ArchipelagoMW.APClient;
import ArchipelagoMW.LocationTracker;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import com.megacrit.cardcrawl.ui.buttons.ReturnToMenuButton;
import gg.archipelago.client.ClientStatus;
import javassist.CtBehavior;

public class VictoryScreenPatch {

    @SpirePatch(clz = DeathScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class deathPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            // check if this is a post-act3 death which the "isVictory" flag tracks
            // if we should be finishing final act, this is not a victory regardless of act3 boss's death
            if (!GameOverScreen.isVictory || APClient.slotData.finalAct == 1)
                return;


            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
            LocationTracker.forfeit();
        }
    }

    @SpirePatch(clz = VictoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class VictoryPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            // victory screen called when this is a final act victory.
            if (!GameOverScreen.isVictory)
                return;


            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
            LocationTracker.forfeit();
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "update")
    public static class ReturnClicked {

        @SpireInsertPatch(locator = locator.class)
        public static void Clicked(DeathScreen __instance, ReturnToMenuButton ___returnButton) {
            if (___returnButton.hb.clicked || ___returnButton.show && CInputActionSet.select.isJustPressed()) {
                APClient.apClient.disconnect();
            }
        }

        private static class locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.MethodCallMatcher(ReturnToMenuButton.class, "update");
                return new int[] {LineFinder.findInOrder(ctBehavior, match)[0]+1};
            }
        }
    }
}
