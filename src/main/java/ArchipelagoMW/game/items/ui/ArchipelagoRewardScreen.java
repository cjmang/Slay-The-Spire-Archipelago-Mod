package ArchipelagoMW.game.items.ui;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.items.APItemID;
import ArchipelagoMW.game.items.MiscItemTracker;
import ArchipelagoMW.game.locations.LocationTracker;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.game.items.patches.RewardItemPatch;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.BottledFlame;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.screens.stats.StatsScreen;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.RewardGlowEffect;
import io.github.archipelagomw.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class ArchipelagoRewardScreen extends CustomScreen {

    public static class Enum {
        @SpireEnum
        public static AbstractDungeon.CurrentScreen ARCHIPELAGO_REWARD_SCREEN;
    }
    private static final Logger logger = LogManager.getLogger(ArchipelagoRewardScreen.class.getName()); // This is our logger! It prints stuff out in the console.

    private static final UIStrings uiStrings;

    public static final String[] TEXT;

    public static List<RewardItem> rewards = Collections.synchronizedList(new ArrayList<>());
    public static int rewardsQueued = 0;
    public ArrayList<AbstractGameEffect> effects = new ArrayList<>();
    public boolean hasTakenAll = false;
    private float rewardAnimTimer = 0.2F;
    private float tipY;
    private final Color uiColor;
    private String tip;

    private final ScrollBar scrollBar;
    private final float scrollLowerBound;
    private float scrollUpperBound;
    private float scrollPosition;
    private float scrollTarget;
    private boolean grabbedScreen;
    private float grabStartY;

    private static int receivedItemsIndex = 0;

    public static int getReceivedItemsIndex() {
        return receivedItemsIndex;
    }

    public static void setReceivedItemsIndex(int receivedItemsIndex) {
        ArchipelagoRewardScreen.receivedItemsIndex = receivedItemsIndex;
    }

    public static boolean apReward = false;
    public static boolean apRareReward = false;
    public static int apGold = 0;

    public static boolean APScreen = false;

    private OrthographicCamera camera = null;

    private AbstractDungeon.CurrentScreen previousScreen;
    private final APContext ctx;


    @SpirePatch(clz = AbstractDungeon.class, method = "rollRarity", paramtypez = {Random.class})
    public static class RarityRollPatch {

        @SpireInsertPatch(rloc = 2003 - 2001, localvars = {"roll"})
        public static SpireReturn<AbstractCard.CardRarity> Insert(int roll) {
            if (apReward) {
                final int rareRate = 3;
                if (roll < rareRate) {
                    return SpireReturn.Return(AbstractCard.CardRarity.RARE);
                }
                if (roll < 40) {
                    return SpireReturn.Return(AbstractCard.CardRarity.UNCOMMON);
                }
                return SpireReturn.Return(AbstractCard.CardRarity.COMMON);
            }
            if (apRareReward) {
                return SpireReturn.Return(AbstractCard.CardRarity.RARE);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = StatsScreen.class, method="incrementFloorClimbed")
    public static class FloorCheckPatch {
        @SpirePostfixPatch()
        public static void insert() {
            APContext.getContext().getLocationTracker().sendFloorCheck(AbstractDungeon.floorNum);
        }

    }



    public ArchipelagoRewardScreen() {
        uiColor = Color.BLACK.cpy();
        tipY = -100.0F * Settings.scale;

        scrollLowerBound = 0.0F;// 50
        scrollUpperBound = 0.0F;// 51
        scrollPosition = 0.0F;// 52
        scrollTarget = 0.0F;// 53
        grabbedScreen = false;// 55
        grabStartY = 0.0F;// 56
        scrollBar = new ScrollBar(new ScrollListener(), (float) Settings.WIDTH / 2.0F + 270.0F * Settings.scale, (float) Settings.HEIGHT / 2.0F - 86.0F * Settings.scale, 500.0F * Settings.scale);// 46
        ctx = APContext.getContext();
    }


    @Override
    public AbstractDungeon.CurrentScreen curScreen() {
        return Enum.ARCHIPELAGO_REWARD_SCREEN;
    }

    @Override
    public void reopen() {
        APClient.logger.info("Reward Screen reopen called: Previous Screen: {}, Internal Previous: {}, CurrentScreen: {}", AbstractDungeon.previousScreen, previousScreen, AbstractDungeon.screen);
        rewardsQueued = 0;
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.dynamicBanner.appear(TEXT[1]);
        AbstractDungeon.overlayMenu.hideCombatPanels();
        AbstractDungeon.overlayMenu.showBlackScreen();
        APClient.logger.info("Reward Screen reopen ended: Previous Screen: {}, Internal Previous: {}, CurrentScreen: {}", AbstractDungeon.previousScreen, previousScreen, AbstractDungeon.screen);
    }

    @Override
    public void close() {
        APScreen = false;
        APClient.logger.info("Reward Screen close called: Previous Screen: {}, Internal Previous: {}, CurrentScreen: {}", AbstractDungeon.previousScreen, previousScreen, AbstractDungeon.screen);
        AbstractDungeon.dynamicBanner.hide();
        AbstractDungeon.isScreenUp = false;
        AbstractDungeon.overlayMenu.hideBlackScreen();
        if(previousScreen == AbstractDungeon.CurrentScreen.CARD_REWARD) {
            AbstractDungeon.previousScreen = null;
        } else if (MonsterRoom.class.isAssignableFrom(AbstractDungeon.getCurrRoom().getClass())) {
            if (!AbstractDungeon.getCurrRoom().isBattleOver) {
                AbstractDungeon.overlayMenu.proceedButton.hide();
                AbstractDungeon.overlayMenu.showCombatPanels();
                AbstractDungeon.previousScreen = null;
            } else {
                AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.COMBAT_REWARD;
            }
        } else if (EventRoom.class.isAssignableFrom(AbstractDungeon.getCurrRoom().getClass())
                && !AbstractDungeon.getCurrRoom().phase.equals(AbstractRoom.RoomPhase.COMPLETE)) {
                AbstractDungeon.previousScreen = null;
        } else if (AbstractDungeon.getCurrRoom() instanceof TreasureRoom) {
            if(((TreasureRoom) AbstractDungeon.getCurrRoom()).chest.isOpen) {
                AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.COMBAT_REWARD;
            } else {
                AbstractDungeon.previousScreen = null;
            }
        } else if (AbstractDungeon.getCurrRoom() instanceof TreasureRoomBoss) {
            AbstractDungeon.previousScreen = null;
        } else if(previousScreen != null && previousScreen != AbstractDungeon.CurrentScreen.NONE) {
                AbstractDungeon.previousScreen = previousScreen;
                previousScreen = null;
        } else if(ShopRoom.class.isAssignableFrom(AbstractDungeon.getCurrRoom().getClass())) {
            previousScreen = null;
        } else if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMPLETE) {
            AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.MAP;
            AbstractDungeon.dungeonMapScreen.open(false);
        } else {
            AbstractDungeon.previousScreen = null;
        }
        APClient.logger.info("Reward Screen close ended: Previous Screen: {}, Internal Previous: {}, CurrentScreen: {}", AbstractDungeon.previousScreen, previousScreen, AbstractDungeon.screen);
    }


    @SuppressWarnings("unused")
    public void open() {
        APClient.logger.info("Reward Screen open called: Previous Screen: {}, Internal Previous: {}, CurrentScreen: {}", AbstractDungeon.previousScreen, previousScreen, AbstractDungeon.screen);
        APScreen = true;
        rewardsQueued = 0;
        AbstractDungeon.player.releaseCard();

        logger.info("current map location y: " + AbstractDungeon.getCurrMapNode().y);
        if (AbstractDungeon.getCurrMapNode().y == -1) {
            AbstractDungeon.nextRoom = null; // this is necessary to make the first nodes available in new act (dunno how else to force it)
        }

        rewardAnimTimer = 0.5F;
        CardCrawlGame.sound.play("UI_CLICK_1");
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.overlayMenu.showBlackScreen();
        AbstractDungeon.dynamicBanner.appear(TEXT[1]);
        AbstractDungeon.overlayMenu.proceedButton.hide();
        AbstractDungeon.overlayMenu.hideCombatPanels();

        previousScreen = AbstractDungeon.screen;
        if(previousScreen == Enum.ARCHIPELAGO_REWARD_SCREEN)
        {
            previousScreen = null;
        }
        AbstractDungeon.screen = Enum.ARCHIPELAGO_REWARD_SCREEN;
        tip = CardCrawlGame.tips.getTip();

        List<NetworkItem> items = ctx.getItemManager().getReceivedItems();
        MiscItemTracker itemTracker = ctx.getItemTracker();
        for (int i = receivedItemsIndex; i < items.size(); ++i) {
            receivedItemsIndex++;
            NetworkItem item = items.get(i);
            if(!APContext.getContext().getCharacterManager().isItemIDForCurrentCharacter(item.itemID))
            {
                continue;
            }
            itemTracker.maybeAddDraw(item.itemID);
            addReward(item, itemTracker.getCount(item.itemID));
        }
        if(apGold > 0)
        {
            addReward(new RewardItem(apGold));
            apGold = 0;
        }
        rewards.sort(rewardSorter);
        condenseGoldRewards();
        APClient.logger.info("Reward Screen open ended: Previous Screen: {}, InternalPrevious: {}, CurrentScreen: {}", AbstractDungeon.previousScreen, previousScreen, AbstractDungeon.screen);
    }

    public void update() {
        if (InputHelper.justClickedLeft && Settings.isDebug) {
            tip = CardCrawlGame.tips.getTip();
        }

        rewardViewUpdate();
        updateEffects();
    }

    private void updateEffects() {
        Iterator<AbstractGameEffect> effectsIterator = effects.iterator();

        while (effectsIterator.hasNext()) {
            AbstractGameEffect effect = effectsIterator.next();
            effect.update();
            if (effect.isDone) {
                effectsIterator.remove();
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void rewardViewUpdate() {
        if (rewardAnimTimer != 0.0F) {
            rewardAnimTimer -= Gdx.graphics.getDeltaTime();
            if (rewardAnimTimer < 0.0F) {
                rewardAnimTimer = 0.0F;
            }

            uiColor.r = 1.0F - rewardAnimTimer / 0.2F;
            uiColor.g = 1.0F - rewardAnimTimer / 0.2F;
            uiColor.b = 1.0F - rewardAnimTimer / 0.2F;
        }

        tipY = MathHelper.uiLerpSnap(tipY, (float) Settings.HEIGHT / 2.0F - 460.0F * Settings.scale);
        updateControllerInput();
        boolean removedSomething = false;
        Iterator<RewardItem> rewardItemIterator = rewards.iterator();

        while (rewardItemIterator.hasNext()) {
            RewardItem rewardItem = rewardItemIterator.next();
            if (doUpdate(rewardItem)) {
                rewardItem.update();
            }
            if (rewardItem.isDone) {
                if (rewardItem.claimReward()) {
                    rewardItemIterator.remove();
                    removedSomething = true;
                } else {
                    rewardItem.isDone = false;
                }
            }
        }

        if (removedSomething) {
            positionRewards();
        }

        if (rewards.size() < 6) {
            scrollTarget = 0.0F;
            scrollPosition = 0.0F;
            positionRewards();
        } else if (!scrollBar.update()) {
            int y = InputHelper.mY;
            if (!grabbedScreen) {
                if (InputHelper.scrolledDown) {
                    scrollTarget = scrollTarget + Settings.SCROLL_SPEED;
                } else if (InputHelper.scrolledUp) {
                    scrollTarget = scrollTarget - Settings.SCROLL_SPEED;
                }

                if (InputHelper.justClickedLeft) {
                    grabbedScreen = true;
                    grabStartY = (float) y - scrollTarget;
                }
            } else if (InputHelper.isMouseDown) {
                scrollTarget = (float) y - grabStartY;
            } else {
                grabbedScreen = false;
            }

            float prev_scrollPosition = scrollTarget;
            scrollPosition = MathHelper.scrollSnapLerpSpeed(scrollPosition, scrollTarget);
            if (scrollTarget < 0.0F) {
                scrollTarget = 0.0F;
            }

            scrollUpperBound = (float) (rewards.size() - 5) * 100.0F * Settings.scale;
            if (scrollTarget > scrollUpperBound) {
                scrollTarget = scrollUpperBound;
            }

            if (scrollPosition != prev_scrollPosition) {
                positionRewards();
            }

            updateBarPosition();
        }
    }

    private void updateBarPosition() {
        float percent = MathHelper.percentFromValueBetween(scrollLowerBound, scrollUpperBound, scrollPosition);
        scrollBar.parentScrolledToPercent(percent);
    }

    public boolean doUpdate(RewardItem reward) {
        boolean ret = false;
        float upperBounds = (float) Settings.HEIGHT / 2.0F + 204.0F * Settings.scale;
        float lowerBounds = (float) Settings.HEIGHT / 2.0F + -336.0F * Settings.scale;
        if (reward.y < upperBounds && reward.y > lowerBounds) {
            ret = true;
        }

        if (!ret) {
            reward.hb.hovered = false;
            reward.hb.justHovered = false;
            reward.hb.clicked = false;
            reward.hb.clickStarted = false;
            if (reward.flashTimer > 0.0F) {
                reward.flashTimer -= Gdx.graphics.getDeltaTime();
                if (reward.flashTimer < 0.0F) {
                    reward.flashTimer = 0.0F;
                }
            }
        }

        ArrayList<AbstractGameEffect> effects = ReflectionHacks.getPrivate(reward,RewardItem.class, "effects");
        if (!ret) {
            if (effects.isEmpty()) {
                effects.add(new RewardGlowEffect(reward.hb.cX, reward.hb.cY));
            }

            Iterator<AbstractGameEffect> effectIterator = effects.iterator();

            while (effectIterator.hasNext()) {
                AbstractGameEffect effect = effectIterator.next();
                effect.update();
                if (effect.isDone) {// 267
                    effectIterator.remove();// 268
                }
            }
        }

        for (AbstractGameEffect effect : effects) {
            if (effect instanceof RewardGlowEffect) {
                moveRewardGlowEffect((RewardGlowEffect) effect, reward.hb.cX, reward.hb.cY);
            }
        }

        return ret;
    }

    private void moveRewardGlowEffect(RewardGlowEffect effect, float x, float y) {
        ReflectionHacks.setPrivate(effect, effect.getClass(), "x",x);
        ReflectionHacks.setPrivate(effect, effect.getClass(), "y",y);
    }

    public void positionRewards() {
        float baseY = (float) Settings.HEIGHT / 2.0F + 124.0F * Settings.scale;
        float spacingY = 100.0F * Settings.scale;

        for (int i = 0; i < rewards.size(); ++i) {
            rewards.get(i).move(baseY - (float) i * spacingY + scrollPosition);
        }

        if (rewards.isEmpty()) {
            hasTakenAll = true;
        }
    }


    public void addReward(RewardItem item) {
        rewards.add(item);
        positionRewards();
    }

    private void condenseGoldRewards()
    {
        Iterator<RewardItem> itr = rewards.iterator();
        int gold = 0;
        while(itr.hasNext())
        {
            RewardItem item = itr.next();
            if(item.type == RewardItem.RewardType.GOLD)
            {
                itr.remove();
                gold += item.goldAmt;
            }
        }
        if(gold > 0)
        {
            rewards.add(new RewardItem(gold));
        }
        positionRewards();
    }

    private static float getAPUpgradeChance(int drawCount)
    {
        float apUpgradeChance = 0.0f;
        int actNum = ((drawCount - 1) / (int) (LocationTracker.CARD_DRAW_NUM / 3)) + 1;
        logger.info("Calculated act num {}; drawcount {}", actNum, drawCount);
        switch(actNum)
        {
            case 4:
            case 3:
                apUpgradeChance = AbstractDungeon.ascensionLevel >= 12 ? 0.25f : 0.5f;
                break;
            case 2:
                apUpgradeChance = AbstractDungeon.ascensionLevel >= 12 ? 0.125f : 0.25f;
                break;
            case 1:
            default:
                break;
        }
        return apUpgradeChance;
    }

    public void addReward(NetworkItem networkItem, int itemCount) {
        long itemID = networkItem.itemID;
        String location = networkItem.locationName;
        String player = networkItem.playerName;
        RewardItem reward;
        APItemID itemType = APItemID.fromLong(itemID );
        if(itemType == null)
        {
            logger.warn("UNKNOWN ITEM {}", itemID % ctx.getCharacterManager().getItemWindow());
            return;
        }
        switch(itemType)
        {
            case CARD_DRAW:
                apReward = true;
                float apUpgradeChance = getAPUpgradeChance(itemCount);
                logger.info("Ap Upgrade Chance {}", apUpgradeChance);
                float upgradeChance = ReflectionHacks.getPrivateStatic(AbstractDungeon.class, "cardUpgradedChance");
                ReflectionHacks.setPrivateStatic(AbstractDungeon.class, "cardUpgradedChance", apUpgradeChance);

                ArrayList<AbstractCard> cards = AbstractDungeon.getRewardCards();

                ReflectionHacks.setPrivateStatic(AbstractDungeon.class, "cardUpgradedChance", upgradeChance);
                apReward = false;

                reward = new RewardItem(1);
                reward.goldAmt = 0;
                reward.type = RewardItem.RewardType.CARD;
                reward.cards = cards;
                RewardItemPatch.CustomFields.apReward.set(reward, true);
                reward.text = player + " NL " + location;
                addReward(reward);
                break;
            case RARE_CARD_DRAW:
                apRareReward = true;
                ArrayList<AbstractCard> rareCards = AbstractDungeon.getRewardCards();
                apRareReward = false;
                reward = new RewardItem(1);
                reward.goldAmt = 0;
                reward.type = RewardItem.RewardType.CARD;
                reward.cards = rareCards;
                RewardItemPatch.CustomFields.apReward.set(reward, true);
                try {
                    Field f = RewardItem.class.getDeclaredField("isBoss");
                    f.setAccessible(true);
                    f.set(reward, true);
                } catch (Exception ignored) {
                }

                reward.text = player + " NL " + location;
                addReward(reward);
                break;
            case RELIC:
//                AbstractRelic relic = AbstractDungeon.returnRandomRelic(getRandomRelicTier());
                AbstractRelic relic = new BottledFlame();
                reward = new RewardItem(relic);
                reward.text = player + " NL " + location;
                RewardItemPatch.CustomFields.apReward.set(reward, true);
                addReward(reward);
                break;
            case BOSS_RELIC:
                ArrayList<AbstractRelic> bossRelics = new ArrayList<AbstractRelic>() {{
                    add(AbstractDungeon.returnRandomRelic(AbstractRelic.RelicTier.BOSS));
                    add(AbstractDungeon.returnRandomRelic(AbstractRelic.RelicTier.BOSS));
                    add(AbstractDungeon.returnRandomRelic(AbstractRelic.RelicTier.BOSS));
                }};
                reward = new RewardItem(1);
                reward.goldAmt = 0;
                reward.type = RewardItemPatch.RewardType.BOSS_RELIC;
                RewardItemPatch.CustomFields.bossRelics.set(reward, bossRelics);
                RewardItemPatch.CustomFields.apReward.set(reward, true);
                reward.text = player + " NL " + location;
                addReward(reward);
                break;
            case ONE_GOLD:
                apGold += 1;
                break;
            case FIVE_GOLD:
                apGold += 5;
                break;
            case FIFTEEN_GOLD:
                apGold += 15;
                break;
            case THIRTY_GOLD:
                apGold += 30;
                break;
            case BOSS_GOLD:
                apGold += AbstractDungeon.ascensionLevel >= 13 ? 75 : 100;
                break;
            case POTION:
                AbstractPotion potion = AbstractDungeon.returnRandomPotion();
                reward = new RewardItem(potion);
                reward.text = player + " NL " + location;
                addReward(reward);
                break;
            default:
        }

    }

    public AbstractCard.CardRarity rollRarity(Random rng) {
        int roll = AbstractDungeon.cardRng.random(99);
        final int rareRate = 3;
        if (roll < rareRate) {
            return AbstractCard.CardRarity.RARE;
        }
        if (roll < 40) {
            return AbstractCard.CardRarity.UNCOMMON;
        }
        return AbstractCard.CardRarity.COMMON;
    }

    private AbstractRelic.RelicTier getRandomRelicTier() {
        int roll = AbstractDungeon.relicRng.random(0, 99);
        if (ModHelper.isModEnabled("Elite Swarm")) {
            roll += 10;
        }
        if (roll < 50) {
            return AbstractRelic.RelicTier.COMMON;
        }
        if (roll > 82) {
            return AbstractRelic.RelicTier.RARE;
        }
        return AbstractRelic.RelicTier.UNCOMMON;
    }

    private void updateControllerInput() {
        if (Settings.isControllerMode && !rewards.isEmpty() && !AbstractDungeon.topPanel.selectPotionMode && AbstractDungeon.topPanel.potionUi.isHidden && !AbstractDungeon.player.viewingRelics) {// 161
            int index = 0;
            boolean anyHovered = false;

            for (Iterator<RewardItem> rewardItemIterator = rewards.iterator(); rewardItemIterator.hasNext(); ++index) {
                RewardItem rewardItem = rewardItemIterator.next();
                if (rewardItem.hb.hovered) {
                    anyHovered = true;
                    break;
                }
            }

            if (!anyHovered) {
                index = 0;
                Gdx.input.setCursorPosition((int) (rewards.get(index)).hb.cX, Settings.HEIGHT - (int) (rewards.get(index)).hb.cY);
            } else if (!CInputActionSet.up.isJustPressed() && !CInputActionSet.altUp.isJustPressed()) {
                if (CInputActionSet.down.isJustPressed() || CInputActionSet.altDown.isJustPressed()) {
                    ++index;
                    if (index > rewards.size() - 1) {
                        index = 0;
                    }

                    Gdx.input.setCursorPosition((int) (rewards.get(index)).hb.cX, Settings.HEIGHT - (int) (rewards.get(index)).hb.cY);
                }
            } else {
                --index;
                if (index < 0) {
                    index = rewards.size() - 1;
                }

                Gdx.input.setCursorPosition((int) (rewards.get(index)).hb.cX, Settings.HEIGHT - (int) (rewards.get(index)).hb.cY);
            }

        }
    }

    public void render(SpriteBatch sb) {
        renderItemReward(sb);
        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, tip, (float) Settings.WIDTH / 2.0F, tipY, Color.LIGHT_GRAY);

        for (AbstractGameEffect effect : effects) {
            effect.render(sb);
        }
    }

    @Override
    public void openingSettings() {
        // Save old screen as previous screen, and put ours in there.
        AbstractDungeon.dynamicBanner.hide();
        previousScreen = AbstractDungeon.previousScreen;
        AbstractDungeon.previousScreen = curScreen();
    }

    @Override
    public void openingDeck() {
        AbstractDungeon.dynamicBanner.hide();
        // Save old screen as previous screen, and put ours in there.
        previousScreen = AbstractDungeon.previousScreen;
        AbstractDungeon.previousScreen = curScreen();
    }

    @Override
    public void openingMap() {
        AbstractDungeon.dynamicBanner.hide();
        // Save old screen as previous screen, and put ours in there.
        previousScreen = AbstractDungeon.previousScreen;
        AbstractDungeon.previousScreen = curScreen();
    }

    @Override
    public boolean allowOpenDeck() {
        return true;
    }

    @Override
    public boolean allowOpenMap() {
        return true;
    }

    private void renderItemReward(SpriteBatch sb) {
        sb.setColor(uiColor);
        sb.draw(ImageMaster.REWARD_SCREEN_SHEET, (float) Settings.WIDTH / 2.0F - 306.0F, (float) Settings.HEIGHT / 2.0F - 46.0F * Settings.scale - 358.0F, 306.0F, 358.0F, 612.0F, 716.0F, Settings.xScale, Settings.scale, 0.0F, 0, 0, 612, 716, false, false);
        if (camera == null) {// 88
            try {
                Field f = CardCrawlGame.class.getDeclaredField("camera");// 90
                f.setAccessible(true);// 91
                camera = (OrthographicCamera) f.get(Gdx.app.getApplicationListener());// 92
            } catch (IllegalAccessException | NoSuchFieldException var4) {// 93
                var4.printStackTrace();// 94
                return;// 95
            }
        }

        sb.flush();// 99
        Rectangle scissors = new Rectangle();// 100
        Rectangle clipBounds = new Rectangle((float) Settings.WIDTH / 2.0F - 300.0F * Settings.scale, (float) Settings.HEIGHT / 2.0F - 350.0F * Settings.scale, 600.0F * Settings.scale, 600.0F * Settings.scale);// 101
        ScissorStack.calculateScissors(camera, sb.getTransformMatrix(), clipBounds, scissors);// 103
        ScissorStack.pushScissors(scissors);
        for (RewardItem reward : rewards) {
            reward.render(sb);
        }
        if (camera != null) {// 109
            sb.flush();// 110
            ScissorStack.popScissors();// 111
        }

        if (rewards.size() > 5) {// 114
            scrollBar.render(sb);// 115
        }
    }

    private class ScrollListener implements ScrollBarListener {
        private ScrollListener() {
        }// 32

        public void scrolledUsingBar(float v) {
            scrollPosition = MathHelper.valueFromPercentBetween(scrollLowerBound, scrollUpperBound, v);// 37
            scrollTarget = scrollPosition;// 38
            positionRewards();// 39
            updateBarPosition();// 40
        }// 41
    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString(Archipelago.getModID() + ":RewardMenu");
        TEXT = uiStrings.TEXT;
    }

    private static final Comparator<RewardItem> rewardSorter = (a, b) -> {
        return b.type.ordinal() - a.type.ordinal();
    };
}
