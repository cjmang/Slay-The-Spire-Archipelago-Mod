package ArchipelagoMW.apEvents;

import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.topPannel.ArchipelagoIcon;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import gg.archipelago.client.events.ArchipelagoEventListener;
import gg.archipelago.client.events.ReceiveItemEvent;

public class ReceiveItem {
    @ArchipelagoEventListener
    public void onReceiveItem(ReceiveItemEvent event) {
        //ignore received items that happen while we are not yet loaded
        ArchipelagoMW.logger.info("NetworkItem received: " + event.getItemName());
        ArchipelagoRewardScreen.rewardsQueued += 1;
        if (CardCrawlGame.isInARun()) {
            try {
                ArchipelagoMW.logger.info("Adding item to player in room: " + AbstractDungeon.getCurrRoom());
                ArchipelagoIcon.addPendingReward(event.getItem());
            } catch (NullPointerException e) {
                ArchipelagoMW.logger.info("Player was unable to receive item for now");
            }
        }
    }
}
