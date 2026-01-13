package ArchipelagoMW.game.locations.patches;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.locations.LocationTracker;
import ArchipelagoMW.game.items.patches.RewardItemPatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.rooms.TreasureRoom;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import io.github.archipelagomw.LocationManager;
import io.github.archipelagomw.parts.NetworkItem;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CombatRewardScreenPatch {
//    private static final Logger logger = LogManager.getLogger(CombatRewardScreenPatch.class);
    private static Set<String> RELIC_BLACKLIST = new HashSet<>(Arrays.asList(
            "Red Mask",
            "Necronomicon",
            "Enchiridion",
            "Nilry's Codex"
    ));

    private static final void replaceRewards(CombatRewardScreen __instance, ArrayList<RewardItem> ___rewards)
    {
        Iterator<RewardItem> rewardItemIterator = ___rewards.iterator();
        ArrayList<RewardItem> toAdd = new ArrayList<>();
        LocationTracker locationTracker = APContext.getContext().getLocationTracker();
        LocationManager locationManager = APContext.getContext().getLocationManager();
        CharacterConfig charConfig = APContext.getContext().getCharacterManager().getCurrentCharacterConfig();
        Set<Long> checkedLocations = new HashSet<>(locationManager.getCheckedLocations());
        boolean goldSanity = APContext.getContext().getSlotData().goldSanity != 0;
        boolean potionSanity = APContext.getContext().getSlotData().potionSanity != 0;
        while (rewardItemIterator.hasNext()) {
            RewardItem reward = rewardItemIterator.next();
//            logger.info("Got reward {}", reward.type);
            NetworkItem item = null;
            switch (reward.type) {
                case CARD:
                    item = locationTracker.sendCardDraw(reward);
                    break;
                case RELIC:
                    if(!RELIC_BLACKLIST.contains(reward.relic.name)) {
                        item = locationTracker.sendRelic();
                    }
                    break;
                case GOLD:
                    if(goldSanity)
                    {
                        item = locationTracker.sendGoldReward();
//                        logger.info("Replacing gold with item: {}", item.itemName);
                    }
                    break;
                case POTION:
                    if(potionSanity)
                    {
                        item = locationTracker.sendPotion();
                    }
                    break;
                case EMERALD_KEY:
                case SAPPHIRE_KEY:
                    if(charConfig.finalAct && charConfig.keySanity)
                    {
                        rewardItemIterator.remove();
                    }
                    break;
            }

            if (item != null) {
                rewardItemIterator.remove();
                // Don't show items already picked up
                if(!checkedLocations.contains(item.locationID)) {
                    RewardItem replacementReward = new RewardItem(1);
                    replacementReward.goldAmt = 0;
                    replacementReward.text = item.itemName + " NL " + item.playerName;
                    replacementReward.type = RewardItemPatch.RewardType.ARCHIPELAGO_LOCATION;
                    toAdd.add(replacementReward);
                }
            }
        }
        if(charConfig.finalAct && charConfig.keySanity) {
            NetworkItem item = null;
            if(AbstractDungeon.getCurrRoom() instanceof TreasureRoom) {
                item = locationTracker.sendSapphireKey();
            } else if (AbstractDungeon.getCurrMapNode().hasEmeraldKey) {
                item = locationTracker.sendEmeraldKey();
            }
            if (null != item && !checkedLocations.contains(item.locationID)) {
                RewardItem replacementReward = new RewardItem(1);
                replacementReward.goldAmt = 0;
                replacementReward.text = item.itemName + " NL " + item.playerName;
                replacementReward.type = RewardItemPatch.RewardType.ARCHIPELAGO_LOCATION;
                toAdd.add(replacementReward);
            }
        }
        ___rewards.addAll(toAdd);
    }

    @SpirePatch(clz = CombatRewardScreen.class, method = "open", paramtypez = {})
    public static class openPatch {

        @SpireInsertPatch(locator = Locator.class, localvars = {})
        public static void Insert(CombatRewardScreen __instance, ArrayList<RewardItem> ___rewards) {
            replaceRewards(__instance, ___rewards);
        }

        private static class Locator extends SpireInsertLocator {

            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher reward = new Matcher.MethodCallMatcher(CombatRewardScreen.class, "setupItemReward");
                int[] match = LineFinder.findInOrder(ctBehavior, reward);
                for (int i = 0; i < match.length; i++) {
                    match[i]++;
                }
                return match;
            }
        }
    }

    @SpirePatch(clz = CombatRewardScreen.class, method = "openCombat", paramtypez = {String.class, boolean.class})
    public static class openCombatPatch {

        @SpireInsertPatch(locator = Locator.class, localvars = {})
        public static void Insert(CombatRewardScreen __instance, String __label, boolean __smoked, ArrayList<RewardItem> ___rewards) {
            replaceRewards(__instance, ___rewards);
        }

        private static class Locator extends SpireInsertLocator {

            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher reward = new Matcher.MethodCallMatcher(CombatRewardScreen.class, "setupItemReward");
                int[] match = LineFinder.findInOrder(ctBehavior, reward);
                for (int i = 0; i < match.length; i++) {
                    match[i]++;
                }
                return match;
            }
        }
    }
}
