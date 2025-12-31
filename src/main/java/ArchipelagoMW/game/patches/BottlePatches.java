package ArchipelagoMW.game.patches;

import ArchipelagoMW.game.TalkQueue;
import ArchipelagoMW.game.items.ui.ArchipelagoRewardScreen;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.BottledFlame;
import com.megacrit.cardcrawl.relics.BottledLightning;
import com.megacrit.cardcrawl.relics.BottledTornado;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class BottlePatches {

    @SpirePatch(clz= RewardItem.class, method="claimReward")
    public static class BanBottles
    {

        @SpirePrefixPatch
        public static SpireReturn<Boolean> banBottles(RewardItem __instance) {
            if(__instance.type == RewardItem.RewardType.RELIC &&
                    (__instance.relic instanceof BottledFlame) ||
                    (__instance.relic instanceof BottledLightning) ||
                    (__instance.relic instanceof BottledTornado))
            {
                if(AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE)
                {
                    TalkQueue.playerTalk("Complete this room first!");
                    return SpireReturn.Return(false);
                }
            }
            return SpireReturn.Continue();
        }
    }
}
