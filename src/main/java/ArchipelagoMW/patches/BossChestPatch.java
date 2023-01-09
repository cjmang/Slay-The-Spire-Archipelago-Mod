package ArchipelagoMW.patches;

import ArchipelagoMW.LocationTracker;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.chests.BossChest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BossChestPatch {
    private static final Logger logger = LogManager.getLogger(BossChestPatch.class.getName()); // This is our logger! It prints stuff out in the console.
    @SpirePatch(clz = BossChest.class, method = SpirePatch.CONSTRUCTOR)
    public static class ConstructorPatch {

        @SpireInsertPatch(rloc = 34 - 25)
        public static SpireReturn<BossChest> Insert(BossChest __instance) {
            LocationTracker.sendBossRelic(AbstractDungeon.actNum);
            return SpireReturn.Return(__instance);
        }

    }
    @SpirePatch(clz = BossChest.class, method = "open")
    public static class OpenPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> PrefixPatch(BossChest __instance, boolean bossChest) {
            logger.info("Don't open the boss chest!");
            return SpireReturn.Return();
        }
    }
}
