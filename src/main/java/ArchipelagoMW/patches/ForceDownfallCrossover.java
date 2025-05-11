package ArchipelagoMW.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import downfall.downfallMod;

@SpirePatch(clz= downfallMod.class, method="loadConfigData", requiredModId = "downfall")
public class ForceDownfallCrossover {
    @SpirePostfixPatch
    public static void force()
    {
        downfallMod.crossoverCharacters = true;
        downfallMod.crossoverModCharacters = true;
    }
}
