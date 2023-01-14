package ArchipelagoMW.ui.topPannel;

import ArchipelagoMW.APClient;
import ArchipelagoMW.APTextures;
import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import ArchipelagoMW.util.APInputActionSet;
import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.TopPanelItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import gg.archipelago.client.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class ArchipelagoIcon extends TopPanelItem {

    static float rotateTimer;

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
        if ((AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD
                || AbstractDungeon.previousScreen == AbstractDungeon.CurrentScreen.COMBAT_REWARD
                || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD
                || AbstractDungeon.previousScreen == AbstractDungeon.CurrentScreen.CARD_REWARD
                || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CHOOSE_ONE
                || AbstractDungeon.previousScreen == AbstractDungeon.CurrentScreen.CHOOSE_ONE
                || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT
                || AbstractDungeon.previousScreen == AbstractDungeon.CurrentScreen.HAND_SELECT)
                && !AbstractDungeon.getCurrRoom().phase.equals(AbstractRoom.RoomPhase.COMPLETE)
        ) {
            setClickable(false);
        } else {
            setClickable(true);
        }

        if (APInputActionSet.apmenu.isJustPressed() && isClickable()) {
            onClick();
        }

        if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN) {
            rotateTimer += Gdx.graphics.getDeltaTime() * 4.0F;
            angle = MathHelper.angleLerpSnap(angle, MathUtils.sin(rotateTimer) * 15.0F);
        }

        if (this.hitbox.justHovered)
            CardCrawlGame.sound.play("UI_HOVER");

        if (pendingRewards.size() > 0)
            for (NetworkItem reward : pendingRewards)
                ((ArchipelagoRewardScreen) BaseMod.getCustomScreen(ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN)).addReward(reward);

        pendingRewards.clear();
    }

    @Override
    public void render(SpriteBatch sb, Color color) {
        if (!APClient.apClient.isConnected()) {
            super.render(sb, Color.RED);
        } else {
            if (!this.isClickable())
                super.render(sb, Color.GRAY);
            else
                super.render(sb, color);
        }


        float tipX = ReflectionHacks.getPrivateStatic(TopPanel.class, "TOP_RIGHT_TIP_X");
        float tipY = ReflectionHacks.getPrivateStatic(TopPanel.class, "TIP_Y");
        if (this.hitbox.hovered) {
            TipHelper.renderGenericTip(tipX, tipY, "Archipelago Rewards (" + APInputActionSet.apmenu.getKeyString() + ")", "View unclaimed rewards that have been sent by Archipelago.");
        }

        FontHelper.renderFontRightTopAligned(sb, FontHelper.topPanelAmountFont, Integer.toString(ArchipelagoRewardScreen.rewardsQueued), this.x + 58.0F * Settings.scale, this.y + 25.0F * Settings.scale, Color.WHITE);
    }

    @Override
    protected void onClick() {
        // if we are disconnected and we click the ap button try new connection.
        if (!APClient.apClient.isConnected()) {
            APClient.newConnection(ArchipelagoMW.address, ArchipelagoMW.slotName, ConnectionPanel.passwordField);
        } else if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN) {
            AbstractDungeon.closeCurrentScreen();
        } else {
            BaseMod.openCustomScreen(ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN);
        }
    }
}
