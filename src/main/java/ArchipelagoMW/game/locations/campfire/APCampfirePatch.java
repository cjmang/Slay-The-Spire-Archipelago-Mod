package ArchipelagoMW.game.locations.campfire;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.items.MiscItemTracker;
import ArchipelagoMW.game.locations.LocationTracker;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.RecallOption;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import com.megacrit.cardcrawl.ui.campfire.SmithOption;
import io.github.archipelagomw.parts.NetworkItem;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
            boolean campireSanity = ctx.getSlotData().campfireSanity != 0;
            CharacterConfig charConfig = ctx.getCharacterManager().getCurrentCharacterConfig();

            MiscItemTracker itemTracker = ctx.getItemTracker();
            int restCount = itemTracker.getRestCount();
            int smithCount = itemTracker.getSmithCount();
            Iterator<AbstractCampfireOption> itr = ___buttons.iterator();
            while(itr.hasNext()) {
                AbstractCampfireOption opt = itr.next();
                if (opt instanceof RestOption) {
                    if(campireSanity && restCount < Math.min(AbstractDungeon.actNum, 3)) {
                        opt.usable = false;
                    }
                } else if (opt instanceof SmithOption) {
                    if(campireSanity && smithCount < Math.min(AbstractDungeon.actNum, 3)) {
                        opt.usable = false;
                    }
                } else if (opt instanceof RecallOption)
                {
                    if(charConfig.finalAct && charConfig.keySanity)
                    {
                        itr.remove();
                    }
                }
            }

            List<Long> remaining = new ArrayList<>();
            List<Long> sendMe = new ArrayList<>();
            LocationTracker locationTracker = ctx.getLocationTracker();
            if(campireSanity) {
                LocationTracker.CampfireLocations campfireLocs = locationTracker.getCampfireLocations();
                remaining = campfireLocs.getLocationsForAct(AbstractDungeon.actNum);
                sendMe = remaining.stream()
                        .filter(l -> (locationTracker.getScoutedItem(l).flags & io.github.archipelagomw.flags.NetworkItem.ADVANCEMENT) > 0)
                        .collect(Collectors.toList());
            }

            if(charConfig.finalAct && charConfig.keySanity && ctx.getLocationManager().getMissingLocations().contains(95L +(200L * charConfig.charOffset))) {
                NetworkItem rubyItem = locationTracker.getScoutedItem(95L + (200L * charConfig.charOffset));
                if ((rubyItem.flags & io.github.archipelagomw.flags.NetworkItem.ADVANCEMENT) > 0) {
                    sendMe.add(rubyItem.locationID);
                }
                remaining.add(rubyItem.locationID);
            }
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
//                Matcher matcher = new Matcher.FieldAccessMatcher(Settings.class, "isFinalActAvailable");
                Matcher matcher = new Matcher.FieldAccessMatcher(CampfireUI.class, "buttons");
                List<Matcher> prereqs = Arrays.asList(
                        new Matcher.FieldAccessMatcher(CampfireUI.class, "buttons"),
                        new Matcher.FieldAccessMatcher(CampfireUI.class, "buttons"),
                        new Matcher.FieldAccessMatcher(CampfireUI.class, "buttons"),
                        new Matcher.FieldAccessMatcher(CampfireUI.class, "buttons"),
                        new Matcher.FieldAccessMatcher(CampfireUI.class, "buttons")
                );
                return LineFinder.findInOrder(ctBehavior, prereqs, matcher);
            }
        }

    }
}
