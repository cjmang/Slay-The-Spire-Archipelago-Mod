package ArchipelagoMW.game.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.Settings;

public class FinalActPatch {
    // Deal with shitty heart saving stuff
    @SpirePatch(clz = Settings.class, method = "setFinalActAvailability")
    public static class StupidFixForHardCheckAgainstHeart {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix() {
            return SpireReturn.Return(null);
        }
    }
}
