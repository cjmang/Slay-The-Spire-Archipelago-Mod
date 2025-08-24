package ArchipelagoMW.game.items.ui;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.SlotData;
import ArchipelagoMW.game.ShopManager;
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
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import io.github.archipelagomw.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArchipelagoIcon extends TopPanelItem {

    static float rotateTimer;

    public static final Logger logger = LogManager.getLogger(ArchipelagoIcon.class.getName());

    public static final String ID = Archipelago.makeID("ClaimRewards");

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
        if(APInputActionSet.cAPMenu.isJustPressed() && isClickable())
        {
            onClick();
        }

        if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN) {
            rotateTimer += Gdx.graphics.getDeltaTime() * 4.0F;
            angle = MathHelper.angleLerpSnap(angle, MathUtils.sin(rotateTimer) * 15.0F);
        }

        if (this.hitbox.justHovered)
            CardCrawlGame.sound.play("UI_HOVER");

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

            SlotData slotData = ctx.getSlotData();
            StringBuilder body = new StringBuilder("View unclaimed rewards that have been sent by Archipelago. NL NL ")
                    .append("#yChecked #yLocations: NL ")
                    .append("TAB Card Draw: #b").append(locationTracker.getCardDrawLocations().getIndex()).append(" NL ")
                    .append("TAB Rare Card Draw: #b").append(locationTracker.getRareDrawLocations().getIndex()).append(" NL ")
                    .append("TAB Relic: #b").append(locationTracker.getRelicLocations().getIndex()).append(" NL ")
                    .append("TAB Boss Relic: #b").append(locationTracker.getBossRelicLocations().getIndex());

            if(slotData.campfireSanity != 0)
            {
                body.append(" NL ")
                        .append("TAB Campfires: #b").append(locationTracker.getCampfireLocations().getNumberChecked());
            }

            if(slotData.shopSanity != 0)
            {
                ShopManager shop = ctx.getShopManager();
                body.append(" NL ")
                        .append("TAB Shop Slots: #b").append(shop.getFoundChecks());
            }

            if(slotData.includeFloorChecks != 0)
            {
                body.append(" NL ")
                        .append("TAB Floors Reached: #b").append(locationTracker.getFloorIndex());
            }

            TipHelper.renderGenericTip(tipX, tipY,
                    "Archipelago Rewards (" + APInputActionSet.apmenu.getKeyString() + ")",
                        body.toString()
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
            ctx.getClient().reconnect();
        } else if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN) {
            AbstractDungeon.closeCurrentScreen();
        } else if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE) {
            // Don't allow opening the screen except when rooms are over, to avoid dumb bugs
            AbstractPlayer player = AbstractDungeon.player;
            if(player != null)
            {
                AbstractDungeon.effectList.add(new SpeechBubble(player.dialogX, player.dialogY, 5.0f, "I must complete this room first.",true));
            }
            return;
        } else {
            BaseMod.openCustomScreen(ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN);
        }
    }
}
