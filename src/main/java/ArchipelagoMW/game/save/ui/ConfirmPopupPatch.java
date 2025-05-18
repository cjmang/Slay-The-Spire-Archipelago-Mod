package ArchipelagoMW.game.save.ui;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
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
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import dev.koifysh.archipelago.helper.DeathLink;
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

                APContext.getContext().getCharacterManager().markUnrecognziedCharacters();

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

                if (Loader.isModLoaded("downfall"))
                    EvilModeCharacterSelect.evilMode = config.downfall;

                if (APContext.getContext().getSlotData().deathLink > 0) {
                    DeathLink.setDeathLinkEnabled(true);
                }

                DeathLinkHelper.update.sendDeath = false;

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

                AbstractDungeon.isAscensionMode = config.ascension > 0;
                AbstractDungeon.ascensionLevel = config.ascension;

                AbstractDungeon.generateSeeds();

                CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
                CardCrawlGame.mainMenuScreen.isFadingOut = true;
                CardCrawlGame.mainMenuScreen.fadeOutMusic();
            }
        }
    }
}
