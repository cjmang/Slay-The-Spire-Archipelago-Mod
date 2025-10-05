package ArchipelagoMW.game.items.patches;


import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;

public class TransitionFixPatch {
    public static boolean isTransitioning = false;

    @SpirePatch2(clz= AbstractDungeon.class, method="nextRoomTransitionStart")
    public static class StartTransition
    {
        public static void Prefix()
        {
            isTransitioning = true;
        }
    }

    @SpirePatch2(clz= AbstractDungeon.class, method="nextRoomTransition", paramtypez = SaveFile.class)
    public static class Transition
    {
        public static void Postfix(SaveFile saveFile)
        {
            isTransitioning = false;
        }
    }
}
