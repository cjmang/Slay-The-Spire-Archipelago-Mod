package ArchipelagoMW.ui.RewardMenu;

import ArchipelagoMW.ArchipelagoMW;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.ui.buttons.ConfirmButton;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.BossChestShineEffect;
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoteListener;
import de.robojumper.ststwitch.TwitchVoteOption;
import de.robojumper.ststwitch.TwitchVoter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Stream;

public class BossRelicRewardScreen {

    private static final Logger logger;
    private static UIStrings uiStrings;
    public static String[] TEXT;
    private boolean isDone;
    public ArrayList<AbstractRelic> relics;
    public ArrayList<AbstractBlight> blights;
    private MenuCancelButton cancelButton;
    private static final String SELECT_MSG;
    private Texture smokeImg;
    private float shineTimer;
    private static final float SHINE_INTERAL = 0.1f;
    private static final float BANNER_Y;
    private static final float SLOT_1_X;
    private static final float SLOT_1_Y;
    private static final float SLOT_2_X;
    private static final float SLOT_2_Y;
    private static final float SLOT_3_X;
    private final float B_SLOT_1_X;
    private final float B_SLOT_1_Y;
    private final float B_SLOT_2_X;
    public ConfirmButton confirmButton;
    public AbstractRelic touchRelic;
    public AbstractBlight touchBlight;
    boolean isVoting;
    boolean mayVote;

    public static class Enum {
        @SpireEnum
        public static AbstractDungeon.CurrentScreen ARCHIPELAGO_BOSS_REWARD;
    }
    public BossRelicRewardScreen() {
        this.isDone = false;
        this.relics = new ArrayList<AbstractRelic>();
        this.blights = new ArrayList<AbstractBlight>();
        this.cancelButton = new MenuCancelButton();
        this.shineTimer = 0.0f;
        this.B_SLOT_1_X = 844.0f * Settings.scale;
        this.B_SLOT_1_Y = AbstractDungeon.floorY + 310.0f * Settings.scale;
        this.B_SLOT_2_X = 1084.0f * Settings.scale;
        this.confirmButton = new ConfirmButton();
        this.touchRelic = null;
        this.touchBlight = null;
        this.isVoting = false;
        this.mayVote = false;
        uiStrings = CardCrawlGame.languagePack.getUIString("BossRelicRewardScreen");
        TEXT = BossRelicRewardScreen.uiStrings.TEXT;
    }

    public void update() {
        this.updateConfirmButton();
        this.shineTimer -= Gdx.graphics.getDeltaTime();
        if (this.shineTimer < 0.0f && !Settings.DISABLE_EFFECTS) {
            this.shineTimer = 0.1f;
            AbstractDungeon.topLevelEffects.add(new BossChestShineEffect());
            AbstractDungeon.topLevelEffects.add(new BossChestShineEffect(MathUtils.random(0.0f, (float)Settings.WIDTH), MathUtils.random(0.0f, Settings.HEIGHT - 128.0f * Settings.scale)));
        }
        this.updateControllerInput();
        if (AbstractDungeon.actNum < 4 || !AbstractPlayer.customMods.contains("Blight Chests")) {
            for (final AbstractRelic r : this.relics) {
                r.update();
                if (r.isObtained) {
                    this.relicObtainLogic(r);
                }
            }
        }
        else {
            for (final AbstractBlight b : this.blights) {
                b.update();
                if (b.isObtained) {
                    this.blightObtainLogic(b);
                }
            }
        }
        if (this.isDone) {
            this.isDone = false;
            this.mayVote = false;
            this.updateVote();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            this.relics.clear();
            AbstractDungeon.closeCurrentScreen();
        }
        this.updateCancelButton();
    }

    private void updateControllerInput() {
        if (!Settings.isControllerMode || AbstractDungeon.topPanel.selectPotionMode || !AbstractDungeon.topPanel.potionUi.isHidden || AbstractDungeon.player.viewingRelics) {
            return;
        }
        boolean anyHovered = false;
        int index = 0;
        for (final AbstractRelic r : this.relics) {
            if (r.hb.hovered) {
                anyHovered = true;
                break;
            }
            ++index;
        }
        for (final AbstractBlight b : this.blights) {
            if (b.hb.hovered) {
                anyHovered = true;
                break;
            }
            ++index;
        }
        if (!anyHovered) {
            if (!this.relics.isEmpty()) {
                CInputHelper.setCursor(this.relics.get(0).hb);
            }
            else {
                CInputHelper.setCursor(this.blights.get(0).hb);
            }
        }
        else if (!this.relics.isEmpty()) {
            if (CInputActionSet.left.isJustPressed() || CInputActionSet.altLeft.isJustPressed()) {
                if (index != 1) {
                    CInputHelper.setCursor(this.relics.get(1).hb);
                }
            }
            else if (CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed()) {
                if (index != 2) {
                    CInputHelper.setCursor(this.relics.get(2).hb);
                }
            }
            else if (CInputActionSet.up.isJustPressed() || CInputActionSet.altUp.isJustPressed()) {
                if (index != 0) {
                    CInputHelper.setCursor(this.relics.get(0).hb);
                }
            }
            else if ((CInputActionSet.down.isJustPressed() || CInputActionSet.altDown.isJustPressed()) && index == 0) {
                CInputHelper.setCursor(this.relics.get(1).hb);
            }
        }
        else if (CInputActionSet.left.isJustPressed() || CInputActionSet.altLeft.isJustPressed() || CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed()) {
            if (index == 0) {
                CInputHelper.setCursor(this.blights.get(1).hb);
            }
            else {
                CInputHelper.setCursor(this.blights.get(0).hb);
            }
        }
    }

    private void blightObtainLogic(final AbstractBlight b) {
        final HashMap<String, Object> choice = new HashMap<String, Object>();
        final ArrayList<String> notPicked = new ArrayList<String>();
        choice.put("picked", b.blightID);
        final TreasureRoomBoss curRoom = (TreasureRoomBoss)AbstractDungeon.getCurrRoom();
        curRoom.choseRelic = true;
        for (final AbstractBlight otherBlights : this.blights) {
            if (otherBlights != b) {
                notPicked.add(otherBlights.blightID);
            }
        }
        choice.put("not_picked", notPicked);
        CardCrawlGame.metricData.boss_relics.add(choice);
        this.isDone = true;
        AbstractDungeon.getCurrRoom().rewardPopOutTimer = 99999.0f;
        AbstractDungeon.overlayMenu.proceedButton.hide();
        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
    }

    private void relicObtainLogic(final AbstractRelic r) {
        logger.info("relicObtainLogic");
        final HashMap<String, Object> choice = new HashMap<String, Object>();
        final ArrayList<String> notPicked = new ArrayList<String>();
        choice.put("picked", r.relicId);
        final TreasureRoomBoss curRoom = (TreasureRoomBoss)AbstractDungeon.getCurrRoom();
        curRoom.choseRelic = true;
        for (final AbstractRelic otherRelics : this.relics) {
            if (otherRelics != r) {
                notPicked.add(otherRelics.relicId);
            }
        }
        choice.put("not_picked", notPicked);
        CardCrawlGame.metricData.boss_relics.add(choice);
        this.isDone = true;
        AbstractDungeon.getCurrRoom().rewardPopOutTimer = 99999.0f;
        AbstractDungeon.overlayMenu.proceedButton.hide();
        if (r.relicId.equals("Black Blood") || r.relicId.equals("Ring of the Serpent") || r.relicId.equals("FrozenCore") || r.relicId.equals("HolyWater")) {
            r.instantObtain(AbstractDungeon.player, 0, true);
            AbstractDungeon.getCurrRoom().rewardPopOutTimer = 0.25f;
        }
    }

    private void relicSkipLogic() {
        logger.info("relicSkipLogic");
        if (AbstractDungeon.getCurrRoom() instanceof TreasureRoomBoss && AbstractDungeon.screen == Enum.ARCHIPELAGO_BOSS_REWARD) {
            final TreasureRoomBoss r = (TreasureRoomBoss)AbstractDungeon.getCurrRoom();
            r.chest.close();
        }
        AbstractDungeon.closeCurrentScreen();
        AbstractDungeon.getCurrRoom().rewardPopOutTimer = 0.25f;
        AbstractDungeon.overlayMenu.proceedButton.hide();
        this.mayVote = false;
        this.updateVote();
    }

    private void updateCancelButton() {
        logger.info("updateCancelButton");
        this.cancelButton.update();
        if (this.cancelButton.hb.clicked) {
            this.cancelButton.hb.clicked = false;
            this.relicSkipLogic();
        }
    }

    private void updateConfirmButton() {
        logger.info("updateConfirmButton");
        this.confirmButton.update();
        if (this.confirmButton.hb.clicked) {
            this.confirmButton.hb.clicked = false;
            if (this.touchRelic != null) {
                this.touchRelic.bossObtainLogic();
            }
            if (this.touchBlight != null) {
                this.touchBlight.bossObtainLogic();
            }
        }
        if (InputHelper.justReleasedClickLeft && (this.touchRelic != null || this.touchBlight != null)) {
            this.touchRelic = null;
            this.touchBlight = null;
            this.confirmButton.hide();
        }
    }

    public void noPick() {
        //logger.info("noPick");
        final ArrayList<String> notPicked = new ArrayList<>();
        final HashMap<String, Object> choice = new HashMap<>();
        for (final AbstractRelic otherRelics : this.relics) {
            notPicked.add(otherRelics.relicId);
        }
        choice.put("not_picked", notPicked);
        CardCrawlGame.metricData.boss_relics.add(choice);
    }

    public void render(final SpriteBatch sb) {
        for (final AbstractGameEffect e : AbstractDungeon.effectList) {
            e.render(sb);
        }
        this.cancelButton.render(sb);
        this.confirmButton.render(sb);
        AbstractDungeon.player.render(sb);
        sb.setColor(Color.WHITE);
        sb.draw(this.smokeImg, Settings.WIDTH / 2.0f - 490.0f * Settings.scale, AbstractDungeon.floorY - 58.0f * Settings.scale, this.smokeImg.getWidth() * Settings.scale, this.smokeImg.getHeight() * Settings.scale);
        for (final AbstractRelic r : this.relics) {
            r.render(sb);
        }
        for (final AbstractBlight b : this.blights) {
            b.render(sb);
        }
        if (AbstractDungeon.topPanel.twitch.isPresent()) {
            this.renderTwitchVotes(sb);
        }
    }

    private void renderTwitchVotes(final SpriteBatch sb) {
        if (!this.isVoting) {
            return;
        }
        if (this.getVoter().isPresent()) {
            final TwitchVoter twitchVoter = this.getVoter().get();
            final TwitchVoteOption[] options = twitchVoter.getOptions();
            final int sum = Arrays.stream(options).map(c -> c.voteCount).reduce(0, Integer::sum);
            for (int i = 0; i < this.relics.size(); ++i) {
                String s = "#" + (i + 1) + ": " + options[i + 1].voteCount;
                if (sum > 0) {
                    s = s + " (" + options[i + 1].voteCount * 100 / sum + "%)";
                }
                switch (i) {
                    case 0: {
                        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, BossRelicRewardScreen.SLOT_1_X, BossRelicRewardScreen.SLOT_1_Y - 75.0f * Settings.scale, Color.WHITE);
                        break;
                    }
                    case 1: {
                        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, BossRelicRewardScreen.SLOT_2_X, BossRelicRewardScreen.SLOT_2_Y - 75.0f * Settings.scale, Color.WHITE);
                        break;
                    }
                    case 2: {
                        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, BossRelicRewardScreen.SLOT_3_X, BossRelicRewardScreen.SLOT_2_Y - 75.0f * Settings.scale, Color.WHITE);
                        break;
                    }
                }
            }
            String s2 = "#0: " + options[0].voteCount;
            if (sum > 0) {
                s2 = s2 + " (" + options[0].voteCount * 100 / sum + "%)";
            }
            FontHelper.renderFont(sb, FontHelper.panelNameFont, s2, 20.0f, 256.0f * Settings.scale, Color.WHITE);
            FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, BossRelicRewardScreen.TEXT[4] + twitchVoter.getSecondsRemaining() + BossRelicRewardScreen.TEXT[5], Settings.WIDTH / 2.0f, 192.0f * Settings.scale, Color.WHITE);
        }
    }

    public void reopen() {
        logger.info("reopen");
        this.confirmButton.hideInstantly();
        this.touchRelic = null;
        this.touchBlight = null;
        this.refresh();
        this.cancelButton.show(BossRelicRewardScreen.TEXT[3]);
        AbstractDungeon.dynamicBanner.appearInstantly(BossRelicRewardScreen.BANNER_Y, BossRelicRewardScreen.SELECT_MSG);
        AbstractDungeon.screen = Enum.ARCHIPELAGO_BOSS_REWARD;
        AbstractDungeon.overlayMenu.proceedButton.hide();
        AbstractDungeon.overlayMenu.showBlackScreen();
    }

    public void openBlight(final ArrayList<AbstractBlight> chosenBlights) {
        this.confirmButton.hideInstantly();
        this.touchRelic = null;
        this.touchBlight = null;
        this.refresh();
        this.blights.clear();
        AbstractDungeon.dynamicBanner.appear(BossRelicRewardScreen.BANNER_Y, BossRelicRewardScreen.TEXT[6]);
        this.smokeImg = ImageMaster.BOSS_CHEST_SMOKE;
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.screen = Enum.ARCHIPELAGO_BOSS_REWARD;
        AbstractDungeon.overlayMenu.proceedButton.hide();
        AbstractDungeon.overlayMenu.showBlackScreen();
        final AbstractBlight r2 = chosenBlights.get(0);
        r2.spawn(this.B_SLOT_1_X, this.B_SLOT_1_Y);
        r2.hb.move(r2.currentX, r2.currentY);
        this.blights.add(r2);
        final AbstractBlight r3 = chosenBlights.get(1);
        r3.spawn(this.B_SLOT_2_X, this.B_SLOT_1_Y);
        r3.hb.move(r3.currentX, r3.currentY);
        this.blights.add(r3);
    }

    public void open(final ArrayList<AbstractRelic> chosenRelics) {
        logger.info("open");
        this.confirmButton.hideInstantly();
        this.touchRelic = null;
        this.touchBlight = null;
        this.refresh();
        this.relics.clear();
        this.blights.clear();
        this.cancelButton.show(BossRelicRewardScreen.TEXT[3]);
        AbstractDungeon.dynamicBanner.appear(BossRelicRewardScreen.BANNER_Y, BossRelicRewardScreen.SELECT_MSG);
        this.smokeImg = ImageMaster.BOSS_CHEST_SMOKE;
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.screen = Enum.ARCHIPELAGO_BOSS_REWARD;
        AbstractDungeon.overlayMenu.proceedButton.hide();
        AbstractDungeon.overlayMenu.showBlackScreen();
        final AbstractRelic r = chosenRelics.get(0);
        r.spawn(BossRelicRewardScreen.SLOT_1_X, BossRelicRewardScreen.SLOT_1_Y);
        r.hb.move(r.currentX, r.currentY);
        this.relics.add(r);
        final AbstractRelic r2 = chosenRelics.get(1);
        r2.spawn(BossRelicRewardScreen.SLOT_2_X, BossRelicRewardScreen.SLOT_2_Y);
        r2.hb.move(r2.currentX, r2.currentY);
        this.relics.add(r2);
        final AbstractRelic r3 = chosenRelics.get(2);
        r3.spawn(BossRelicRewardScreen.SLOT_3_X, BossRelicRewardScreen.SLOT_2_Y);
        r3.hb.move(r3.currentX, r3.currentY);
        this.relics.add(r3);
        for (final AbstractRelic r4 : this.relics) {
            UnlockTracker.markRelicAsSeen(r4.relicId);
        }
        this.mayVote = true;
        this.updateVote();
    }

    public void refresh() {
        this.isDone = false;
        this.cancelButton = new MenuCancelButton();
        this.shineTimer = 0.0f;
    }

    public void hide() {
        AbstractDungeon.dynamicBanner.hide();
        AbstractDungeon.overlayMenu.cancelButton.hide();
    }

    private Optional<TwitchVoter> getVoter() {
        return TwitchPanel.getDefaultVoter();
    }

    private void updateVote() {
        if (this.getVoter().isPresent()) {
            final TwitchVoter twitchVoter = this.getVoter().get();
            if (this.mayVote && twitchVoter.isVotingConnected() && !this.isVoting) {
                BossRelicRewardScreen.logger.info("Publishing Boss Relic Vote");
                this.isVoting = twitchVoter.initiateSimpleNumberVote(Stream.concat(Stream.of("skip"), this.relics.stream().map(AbstractRelic::toString)).toArray(String[]::new), this::completeVoting);
            }
            else if (this.isVoting && (!this.mayVote || !twitchVoter.isVotingConnected())) {
                twitchVoter.endVoting(true);
                this.isVoting = false;
            }
        }
    }

    public void completeVoting(final int option) {
        if (!this.isVoting) {
            return;
        }
        this.isVoting = false;
        if (this.getVoter().isPresent()) {
            final TwitchVoter twitchVoter = this.getVoter().get();
            AbstractDungeon.topPanel.twitch.ifPresent(twitchPanel -> twitchPanel.connection.sendMessage("Voting on relic ended... chose " + twitchVoter.getOptions()[option].displayName));
        }
        while (AbstractDungeon.screen != Enum.ARCHIPELAGO_BOSS_REWARD) {
            AbstractDungeon.closeCurrentScreen();
        }
        if (option == 0) {
            this.relicSkipLogic();
        }
        else if (option < this.relics.size() + 1) {
            final AbstractRelic r = this.relics.get(option - 1);
            if (!r.relicId.equals("Black Blood") && !r.relicId.equals("Ring of the Serpent")) {
                r.obtain();
            }
            r.isObtained = true;
        }
    }

    static {
        logger = LogManager.getLogger(BossRelicRewardScreen.class.getName());
        SELECT_MSG = BossRelicRewardScreen.TEXT[2];
        BANNER_Y = AbstractDungeon.floorY + 460.0f * Settings.scale;
        SLOT_1_X = Settings.WIDTH / 2.0f + 4.0f * Settings.scale;
        SLOT_1_Y = AbstractDungeon.floorY + 360.0f * Settings.scale;
        SLOT_2_X = Settings.WIDTH / 2.0f - 116.0f * Settings.scale;
        SLOT_2_Y = AbstractDungeon.floorY + 225.0f * Settings.scale;
        SLOT_3_X = Settings.WIDTH / 2.0f + 124.0f * Settings.scale;
        TwitchVoter.registerListener(new TwitchVoteListener() {
            @Override
            public void onTwitchAvailable() {
                ArchipelagoMW.bossRelicRewardScreen.updateVote();
            }

            @Override
            public void onTwitchUnavailable() {
                ArchipelagoMW.bossRelicRewardScreen.updateVote();
            }
        });
    }
}