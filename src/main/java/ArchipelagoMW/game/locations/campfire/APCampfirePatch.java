package ArchipelagoMW.game.locations.campfire;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.items.MiscItemTracker;
import ArchipelagoMW.game.locations.LocationTracker;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import com.megacrit.cardcrawl.ui.campfire.SmithOption;
import dev.koifysh.archipelago.network.client.CreateAsHint;

import java.util.ArrayList;
import java.util.List;

public class APCampfirePatch {

    @SpirePatch(clz = CampfireUI.class, method="initializeButtons")
    public static class AddAPLocationsPatch {

        @SpireInsertPatch(rloc=117-92)
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
                    opt.usable = restCount >= Math.min(AbstractDungeon.actNum, 3);
                } else if (opt instanceof SmithOption) {
                    opt.usable = smithCount >= Math.min(AbstractDungeon.actNum, 3);
                }
            }

            LocationTracker locationTracker = ctx.getLocationTracker();
            LocationTracker.CampfireLocations campfireLocs = locationTracker.getCampfireLocations();
            List<Long> remaining = campfireLocs.getLocationsForAct(AbstractDungeon.actNum);
            ctx.getClient().scoutLocations(new ArrayList<>(remaining), CreateAsHint.BROADCAST_NEW);
            for(Long rem : remaining) {
                ___buttons.add(new APCampfireButton(rem, locationTracker.getScoutedItem(rem)));
            }
        }


    }
}
