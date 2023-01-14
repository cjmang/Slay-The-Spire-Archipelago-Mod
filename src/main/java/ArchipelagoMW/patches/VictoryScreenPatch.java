package ArchipelagoMW.patches;

import ArchipelagoMW.APClient;
import ArchipelagoMW.LocationTracker;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import gg.archipelago.client.ClientStatus;

public class VictoryScreenPatch {

    @SpirePatch(clz = DeathScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class VictoryPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            if (!GameOverScreen.isVictory)
                return;
            // don't send victory if we are in the beyond victory screen and we are on a heart run.
            if (CardCrawlGame.dungeon instanceof TheBeyond && APClient.slotData.finalAct == 1)
                return;

            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
            LocationTracker.forfeit();

        }
    }

    @SpirePatch(clz = VictoryScreen.class, method = "<ctor>")
    public static class TrueVictoryPatch {

        @SpirePrefixPatch
        public static void Prefix() {
            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
            LocationTracker.forfeit();
        }
    }
}
