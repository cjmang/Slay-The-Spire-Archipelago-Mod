package ArchipelagoMW.game.items.ui;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.ui.APTextures;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.game.locations.LocationTracker;
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
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import dev.koifysh.archipelago.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class ArchipelagoIcon extends TopPanelItem {

    static float rotateTimer;

    public static final Logger logger = LogManager.getLogger(ArchipelagoIcon.class.getName());

    public static final String ID = Archipelago.makeID("ClaimRewards");

    public static List<NetworkItem> pendingRewards = new LinkedList<>();
    private final APContext ctx;

    public ArchipelagoIcon() {
        super(APTextures.AP_ICON, ID);
        ctx = APContext.getContext();
    }

    @Override
    public void update() {
        super.update();
        setClickable(((AbstractDungeon.screen != AbstractDungeon.CurrentScreen.COMBAT_REWARD
                && AbstractDungeon.previousScreen != AbstractDungeon.CurrentScreen.COMBAT_REWARD)
                || AbstractDungeon.getCurrRoom().phase.equals(AbstractRoom.RoomPhase.COMPLETE))
                && AbstractDungeon.screen != AbstractDungeon.CurrentScreen.CARD_REWARD
                && AbstractDungeon.previousScreen != AbstractDungeon.CurrentScreen.CARD_REWARD
                && AbstractDungeon.screen != AbstractDungeon.CurrentScreen.CHOOSE_ONE
                && AbstractDungeon.previousScreen != AbstractDungeon.CurrentScreen.CHOOSE_ONE
                && AbstractDungeon.screen != AbstractDungeon.CurrentScreen.HAND_SELECT
                && AbstractDungeon.previousScreen != AbstractDungeon.CurrentScreen.HAND_SELECT
                && AbstractDungeon.screen != AbstractDungeon.CurrentScreen.GRID
                && AbstractDungeon.previousScreen != AbstractDungeon.CurrentScreen.GRID
                && AbstractDungeon.screen != AbstractDungeon.CurrentScreen.INPUT_SETTINGS
                && AbstractDungeon.previousScreen != AbstractDungeon.CurrentScreen.INPUT_SETTINGS
        );

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
        if (!ctx.getClient().isConnected()) {
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
            LocationTracker locationTracker = APContext.getContext().getLocationTracker();
            TipHelper.renderGenericTip(tipX, tipY,
                    "Archipelago Rewards (" + APInputActionSet.apmenu.getKeyString() + ")",
                    "View unclaimed rewards that have been sent by Archipelago. NL NL " +
                            "#yChecked #yLocations: NL " +
                            "TAB Card Draw: #b" + locationTracker.getCardDrawLocations().getIndex() + " NL " +
                            "TAB Rare Card Draw: #b" + locationTracker.getRareDrawLocations().getIndex() + " NL " +
                            "TAB Relic: #b" + locationTracker.getRelicLocations().getIndex() + " NL " +
                            "TAB Boss Relic: #b" + locationTracker.getBossRelicLocations().getIndex() + " NL " +
                            "TAB Campfires: #b" + locationTracker.getCampfireLocations().getNumberChecked() + " NL " +
                            "TAB Floors Reached: #b" + AbstractDungeon.floorNum
            );
        }

        FontHelper.renderFontRightTopAligned(sb, FontHelper.topPanelAmountFont, Integer.toString(ArchipelagoRewardScreen.rewardsQueued), this.x + 58.0F * Settings.scale, this.y + 25.0F * Settings.scale, Color.WHITE);
    }

    @Override
    protected void onClick() {
        //disable button if we are dead, we should not be able to reconnect.
        if(AbstractDungeon.player.isDead)
            return;

        // if we are disconnected, and we click the ap button try new connection.
        if (!ctx.getClient().isConnected() && !AbstractDungeon.player.isDead) {
            APClient.newConnection(APContext.getContext(), Archipelago.address, Archipelago.slotName, Archipelago.password);
        } else if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN) {
            AbstractDungeon.closeCurrentScreen();
        } else {
            BaseMod.openCustomScreen(ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN);
        }
    }
}
