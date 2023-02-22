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
            // check if this is a post-act3 death which the "isVictory" flag tracks
            // if we should be finishing final act, this is not a victory regardless of act3 boss's death
            if (!GameOverScreen.isVictory || APClient.slotData.finalAct == 1)
                return;


            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
            LocationTracker.forfeit();
            APClient.apClient.disconnect();
        }
    }

    @SpirePatch(clz = VictoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class TrueVictoryPatch {

        @SpirePrefixPatch
        public static void Prefix() {
            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
            LocationTracker.forfeit();
            APClient.apClient.disconnect();
        }
    }
}
