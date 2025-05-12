package ArchipelagoMW.apEvents;

import ArchipelagoMW.APClient;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;

import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.ReceiveItemEvent;

public class ReceiveItem {

    @ArchipelagoEventListener
    public void onReceiveItem(ReceiveItemEvent event) {
        // TODO: needs changing once we have multiple characters
        if(event.getIndex() > ArchipelagoRewardScreen.receivedItemsIndex) {
            if(APClient.charManager.isItemIDForCurrentCharacter(event.getItemID()))
            {
                // only increase counter, actual items get fetched when you open the reward screen.
                ArchipelagoRewardScreen.rewardsQueued += 1;
            }
        }
    }
}
