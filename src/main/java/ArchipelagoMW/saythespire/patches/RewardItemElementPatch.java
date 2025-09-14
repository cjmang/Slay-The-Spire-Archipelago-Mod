package ArchipelagoMW.saythespire.patches;

import ArchipelagoMW.game.items.patches.RewardItemPatch;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.rewards.RewardItem;
import sayTheSpire.ui.elements.ButtonElement;
import sayTheSpire.ui.elements.RewardItemElement;
import sayTheSpire.ui.elements.UIElement;

public class RewardItemElementPatch {

    @SpirePatch(cls="sayTheSpire.ui.elements.RewardItemElement", method="setupUIElement", requiredModId = "Say_the_Spire")
    public static class AddAPRewardText
    {

        public static SpireReturn<Void> Prefix(RewardItemElement __instance, RewardItem ___reward)
        {
            if(!RewardItemPatch.CustomFields.apReward.get(___reward))
            {
                return SpireReturn.Continue();
            }
            UIElement sayElement = null;
            RewardItem.RewardType type = ___reward.type;

            if(type == RewardItem.RewardType.CARD)
            {
                if(ReflectionHacks.getPrivate(___reward, RewardItem.class, "isBoss"))
                {

                    sayElement = new ButtonElement("rare card reward");
                }
                else {
                    sayElement = new ButtonElement("card reward");
                }
            }
            else if(type == RewardItemPatch.RewardType.BOSS_RELIC)
            {
                sayElement = new ButtonElement("boss relic");
            }
            if(sayElement == null) {
                return SpireReturn.Continue();
            }
            ReflectionHacks.setPrivate(__instance, RewardItemElement.class, "rewardUIElement", sayElement);
            return SpireReturn.Return();
        }
    }
}
