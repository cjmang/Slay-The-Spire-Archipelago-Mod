package ArchipelagoMW.game.locations.patches;

import ArchipelagoMW.game.ui.Components.APChest;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TreasureRoomBossPatch {
    private static final Logger logger = LogManager.getLogger(TreasureRoomBossPatch.class.getName()); // This is our logger! It prints stuff out in the console.

    @SpirePatch(clz = TreasureRoomBoss.class, method = "onPlayerEntry")
    public static class playerEntry {

        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn Insert(TreasureRoomBoss __instance) {
            __instance.chest = new APChest();
            return SpireReturn.Return();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(TreasureRoomBoss.class, "chest");
                return LineFinder.findInOrder(ctBehavior, match);
            }
        }
    }
}
