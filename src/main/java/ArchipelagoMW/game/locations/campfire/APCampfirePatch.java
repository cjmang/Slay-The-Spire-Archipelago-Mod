package ArchipelagoMW.game.locations.campfire;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.items.MiscItemTracker;
import ArchipelagoMW.game.locations.LocationTracker;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import com.megacrit.cardcrawl.ui.campfire.SmithOption;
import io.github.archipelagomw.flags.NetworkItem;
import io.github.archipelagomw.network.client.CreateAsHint;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class APCampfirePatch {

    @SpirePatch(clz = CampfireUI.class, method="initializeButtons")
    public static class AddAPLocationsPatch {

//        @SpireInsertPatch(rloc=117-92)
        @SpireInsertPatch(locator = Locator.class)
        public static void changeValidOptions(CampfireUI __instance, ArrayList<AbstractCampfireOption> ___buttons)
        {
            APContext ctx = APContext.getContext();
            if(ctx.getSlotData().campfireSanity == 0) {
                return;
            }

            MiscItemTracker itemTracker = ctx.getItemTracker();
            int restCount = itemTracker.getRestCount();
            int smithCount = itemTracker.getSmithCount();

            for (AbstractCampfireOption opt : ___buttons) {
                if (opt instanceof RestOption) {
                    if(restCount < Math.min(AbstractDungeon.actNum, 3)) {
                        opt.usable = false;
                    }
                } else if (opt instanceof SmithOption) {
                    if(smithCount < Math.min(AbstractDungeon.actNum, 3)) {
                        opt.usable = false;
                    }
                }
            }

            LocationTracker locationTracker = ctx.getLocationTracker();
            LocationTracker.CampfireLocations campfireLocs = locationTracker.getCampfireLocations();
            List<Long> remaining = campfireLocs.getLocationsForAct(AbstractDungeon.actNum);
            List<Long> sendMe = remaining.stream()
                    .filter(l -> (locationTracker.getScoutedItem(l).flags & NetworkItem.ADVANCEMENT) > 0)
                    .collect(Collectors.toList());
            if(!sendMe.isEmpty()) {
                ctx.getClient().createHints(sendMe);
            }
            for(Long rem : remaining) {
                ___buttons.add(new APCampfireButton(rem, locationTracker.getScoutedItem(rem)));
            }
        }

        public static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher matcher = new Matcher.FieldAccessMatcher(Settings.class, "isFinalActAvailable");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }

    }
}
