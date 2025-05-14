package ArchipelagoMW.game.locations.patches.campfire;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.game.items.MiscItemTracker;
import ArchipelagoMW.game.locations.LocationTracker;
import ArchipelagoMW.game.locations.ui.campfire.APCampfireButton;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import com.megacrit.cardcrawl.ui.campfire.SmithOption;

import java.util.ArrayList;
import java.util.List;

public class APCampfirePatch {

    @SpirePatch(clz = CampfireUI.class, method="initializeButtons")
    public static class AddAPLocationsPatch {

        @SpireInsertPatch(rloc=117-92)
        public static void changeValidOptions(CampfireUI __instance, ArrayList<AbstractCampfireOption> ___buttons)
        {
            if(APClient.slotData.campfireSanity == 0) {
                return;
            }

            MiscItemTracker itemTracker = CharacterManager.getInstance().getItemTracker();
            int restCount = itemTracker.getRestCount();
            int smithCount = itemTracker.getSmithCount();

            for (AbstractCampfireOption opt : ___buttons) {
                if (opt instanceof RestOption) {
                    opt.usable = restCount >= Math.min(AbstractDungeon.actNum, 3);
                } else if (opt instanceof SmithOption) {
                    opt.usable = smithCount >= Math.min(AbstractDungeon.actNum, 3);
                }
            }

            LocationTracker.CampfireLocations campfireLocs = LocationTracker.campfireLocations;
            List<Long> remaining = campfireLocs.getLocationsForAct(AbstractDungeon.actNum);
            // TODO: Send out hints!
            for(Long rem : remaining) {
                ___buttons.add(new APCampfireButton(rem, LocationTracker.getScoutedItem(rem)));
            }
        }


    }
}
