package ArchipelagoMW.game.start.patches;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.neow.NeowEvent;

public class NeowPatch {

    @SpirePatch(clz= NeowEvent.class, method="buttonEffect")
    public static class AlwaysFour
    {
        @SpirePrefixPatch
        public static void buttonEffect(NeowEvent __instance, int buttonPressed, @ByRef int[] ___bossCount)
        {
            ___bossCount[0] = 1;
        }
    }
}
