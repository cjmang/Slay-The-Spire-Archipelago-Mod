package ArchipelagoMW.save;

import ArchipelagoMW.APClient;
import ArchipelagoMW.CharacterConfig;
import ArchipelagoMW.ui.connection.ArchipelagoPreGameScreen;
import ArchipelagoMW.ui.mainMenu.ArchipelagoMainMenuButton;
import ArchipelagoMW.util.DeathLinkHelper;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.SeedHelper;
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

                APClient.charManager.markUnrecognziedCharacters();

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

                CharacterConfig config = APClient.charManager.getCurrentCharacterConfig();

                if (Loader.isModLoaded("downfall"))
                    EvilModeCharacterSelect.evilMode = config.downfall;

                if (APClient.slotData.deathLink > 0) {
                    DeathLink.setDeathLinkEnabled(true);
                }

                DeathLinkHelper.update.sendDeath = false;

                Settings.isFinalActAvailable = config.finalAct;
                SeedHelper.setSeed(config.seed);

                AbstractDungeon.isAscensionMode = config.ascension > 0;
                AbstractDungeon.ascensionLevel = config.ascension;

                AbstractDungeon.generateSeeds();
                Settings.seedSet = true;

                CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
                CardCrawlGame.mainMenuScreen.isFadingOut = true;
                CardCrawlGame.mainMenuScreen.fadeOutMusic();
            }
        }
    }
}
