package ArchipelagoMW.ui.topPannel;

import ArchipelagoMW.APClient;
import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import basemod.TopPanelItem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArchipelagoIcon extends TopPanelItem {

    public static final Logger logger = LogManager.getLogger(ArchipelagoIcon.class.getName());

    private static final Texture IMG = ArchipelagoMW.AP_ICON;
    public static final String ID = "ArchipelagoMW:ClaimRewards";

    public ArchipelagoIcon() {
        super(IMG, ID);
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
        logger.info("click da button!");
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
