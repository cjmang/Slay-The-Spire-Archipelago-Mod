package ArchipelagoMW.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class RewardScreenPatch {

    @SpirePatch(clz=AbstractDungeon.class, method="update")
    public static class Update
    {
        @SpirePostfixPatch
        public static void Postfix()
        {
            if (AbstractDungeon.screen == ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD) {
                ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen.update();
            }
        }
    }

    @SpirePatch(clz=AbstractDungeon.class, method="render")
    public static class Render
    {
        @SpireInsertPatch(rloc=2773-2658,localvars={})
        public static void Insert(AbstractDungeon __instance, SpriteBatch sb)
        {
            if (AbstractDungeon.screen == ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD) {
                ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen.render(sb);
            }
        }
    }
}
