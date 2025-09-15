package ArchipelagoMW.game.save.ui;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.client.util.DeathLinkHelper;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import downfall.patches.EvilModeCharacterSelect;


public class ConfirmPopupPatch {
    @SpireEnum
    public static ConfirmPopup.ConfirmType AP_SAVE_RESUME;

    @SpirePatch(clz = ConfirmPopup.class, method = "yesButtonEffect")
    public static class YesButtonEffect {
        @SpirePostfixPatch
        public static void Postfix(ConfirmPopup __instance, ConfirmPopup.ConfirmType ___type) {
            if (___type == AP_SAVE_RESUME) {
                CardCrawlGame.loadingSave = true;
                APContext ctx = APContext.getContext();
                ctx.getCharacterManager().markUnrecognziedCharacters();
                ctx.getItemTracker().initialize(ctx.getItemManager().getReceivedItemIDs());
                ctx.getTrapManager().initialize();
                APContext.getContext().getShopManager().initializeShop();
                ctx.getAscensionManager().reset();
                CardCrawlGame.mainMenuScreen.isFadingOut = true;
                CardCrawlGame.mainMenuScreen.fadeOutMusic();
                Settings.isDailyRun = false;
                Settings.isTrial = false;
                ModHelper.setModsFalse();
                if (CardCrawlGame.steelSeries.isEnabled) {
                    CardCrawlGame.steelSeries.event_character_chosen(CardCrawlGame.chosenCharacter);
                }
            }
        }
    }

    @SpirePatch(clz = ConfirmPopup.class, method = "noButtonEffect")
    public static class NoButtonEffect {
        @SpirePostfixPatch
        public static void Postfix(ConfirmPopup __instance, ConfirmPopup.ConfirmType ___type) {
            if (___type == AP_SAVE_RESUME) {
                APContext ctx = APContext.getContext();
                CharacterConfig config = ctx.getCharacterManager().getCurrentCharacterConfig();
                ctx.getItemTracker().initialize(ctx.getItemManager().getReceivedItemIDs());
                ctx.getTrapManager().initialize();

                if (Loader.isModLoaded("downfall"))
                    EvilModeCharacterSelect.evilMode = config.downfall;

                APClient client = APContext.getContext().getClient();
                if (client.getSlotData().deathLink > 0) {
                    client.setDeathLinkEnabled(true);
                }

                Settings.isFinalActAvailable = config.finalAct;
                SeedHelper.setSeed(config.seed);
                if(Settings.seed == null)
                {
                    long sourceTime = System.nanoTime();
                    Random rng = new Random(sourceTime);
                    Settings.seedSourceTimestamp = sourceTime;
                    Settings.seed = SeedHelper.generateUnoffensiveSeed(rng);
                    Settings.seedSet = false;
                }

                ctx.getAscensionManager().initializeRunStart();
                AbstractDungeon.isAscensionMode = AbstractDungeon.ascensionLevel > 0;

                AbstractDungeon.generateSeeds();

                CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
                CardCrawlGame.mainMenuScreen.isFadingOut = true;
                CardCrawlGame.mainMenuScreen.fadeOutMusic();
            }
        }
    }
}
