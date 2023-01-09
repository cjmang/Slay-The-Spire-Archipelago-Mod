package ArchipelagoMW.patches;

import ArchipelagoMW.LocationTracker;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;

import java.util.ArrayList;
import java.util.Iterator;

public class CombatRewardScreenPatch {

    @SpirePatch(clz = CombatRewardScreen.class, method = "open", paramtypez = {})
    public static class openPatch {

        @SpireInsertPatch(rloc = 371 - 357, localvars = {})
        public static void Insert(CombatRewardScreen __instance, ArrayList<RewardItem> ___rewards) {
            Iterator<RewardItem> rewardItemIterator = ___rewards.iterator();
            ArrayList<RewardItem> toAdd = new ArrayList<>();
            while (rewardItemIterator.hasNext()) {
                RewardItem reward = rewardItemIterator.next();
                String locationName = "";
                switch (reward.type) {
                    case CARD:
                        locationName = LocationTracker.sendCardDraw(reward);
                        break;
                    case RELIC:
                        locationName = LocationTracker.sendRelic();
                        break;
                }

                if (!locationName.isEmpty()) {
                    rewardItemIterator.remove();
                    RewardItem rewardItem = new RewardItem(1);
                    rewardItem.goldAmt = 0;
                    rewardItem.text = locationName;
                    rewardItem.type = RewardItemPatch.RewardType.ARCHIPELAGO_LOCATION;
                    toAdd.add(rewardItem);
                }
            }
            ___rewards.addAll(toAdd);
        }

    }
}
