package ArchipelagoMW.patches;

import ArchipelagoMW.Archipelago;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.ui.buttons.SingingBowlButton;

public class SingingBowlPatch {

    @SpirePatch(clz = SingingBowlButton.class, method = "onClick")
    public static class onClick {
        @SpirePostfixPatch
        public static void PostFix(SingingBowlButton __instance, RewardItem ___rItem) {
            ArchipelagoRewardScreen.rewards.remove(___rItem);
        }
    }

}
