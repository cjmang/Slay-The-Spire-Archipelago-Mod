package ArchipelagoMW.apEvents;

import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;

import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.ReceiveItemEvent;

public class ReceiveItem {

    @ArchipelagoEventListener
    public void onReceiveItem(ReceiveItemEvent event) {
        if(event.getIndex() > ArchipelagoRewardScreen.receivedItemsIndex) {
            // only increase counter, actual items get fetched when you open the reward screen.
            ArchipelagoRewardScreen.rewardsQueued += 1;
        }
    }
}
