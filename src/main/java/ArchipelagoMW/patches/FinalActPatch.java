package ArchipelagoMW.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.Settings;

public class FinalActPatch {
    // Deal with shitty heart saving stuff
    @SpirePatch(clz = Settings.class, method="setFinalActAvailability")
    public static class StupidFixForHardCheckAgainstHeart {
        public static SpireReturn Prefix() {
            return SpireReturn.Return(null);
        }
    }
}
