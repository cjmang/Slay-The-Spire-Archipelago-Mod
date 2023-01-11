package ArchipelagoMW.ui.topPannel;

import ArchipelagoMW.APClient;
import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.APTextures;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import basemod.TopPanelItem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import gg.archipelago.APClient.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class ArchipelagoIcon extends TopPanelItem {

    public static final Logger logger = LogManager.getLogger(ArchipelagoIcon.class.getName());

    public static final String ID = ArchipelagoMW.makeID("ClaimRewards");

    public static List<NetworkItem> pendingRewards = new LinkedList<NetworkItem>();

    public ArchipelagoIcon() {
        super(APTextures.AP_ICON, ID);
    }

    public static void addPendingReward(NetworkItem networkItem) {
        pendingRewards.add(networkItem);
    }

    @Override
    public void update() {
        super.update();

        if(pendingRewards.size() > 0)
            for (NetworkItem reward : pendingRewards)
                ArchipelagoRewardScreen.addReward(reward);

        pendingRewards.clear();
    }

    @Override
    public void render(SpriteBatch sb, Color color) {
        if (!APClient.apClient.isConnected()) {
            this.setClickable(true);
            super.render(sb, Color.RED);
        } else if (AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE) {
            this.setClickable(false);
            super.render(sb, Color.GRAY);
        } else {
            this.setClickable(true);
            super.render(sb, color);
        }

        FontHelper.renderFontRightTopAligned(sb, FontHelper.topPanelAmountFont, Integer.toString(ArchipelagoRewardScreen.rewardsQueued), this.x + 58.0F * Settings.scale, this.y + 25.0F * Settings.scale, Color.WHITE);
    }

    @Override
    protected void onClick() {
        // if we are disconnected and we click the ap button try new connection.
        if (!APClient.apClient.isConnected()) {
            APClient.newConnection(ArchipelagoMW.address, ArchipelagoMW.slotName, ConnectionPanel.passwordField);
        } else if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD) {
            ArchipelagoRewardScreen.close();
        } else {
            if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMPLETE) {
                ArchipelagoRewardScreen.open();
            }
        }
    }
}
