package ArchipelagoMW.game.start.patches;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.neow.NeowEvent;
import com.megacrit.cardcrawl.neow.NeowRoom;

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

    @SpirePatch(clz= NeowRoom.class, method="update")
    public static class NoUpdateOnBossRelic {
        @SpirePrefixPatch
        public static SpireReturn<Void> blockOnBoss(NeowRoom __instance)
        {
            if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.BOSS_REWARD)
            {
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
}
