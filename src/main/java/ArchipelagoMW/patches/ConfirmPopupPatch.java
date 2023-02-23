package ArchipelagoMW.patches;

import ArchipelagoMW.apEvents.ConnectionResult;
import ArchipelagoMW.ui.connection.ArchipelagoPreGameScreen;
import ArchipelagoMW.ui.mainMenu.ArchipelagoMainMenuButton;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;

public class ConfirmPopupPatch {
    @SpireEnum
    public static ConfirmPopup.ConfirmType AP_SAVE_RESUME;

    @SpirePatch(clz = ConfirmPopup.class, method = "yesButtonEffect")
    public static class YesButtonEffect {
        @SpirePostfixPatch
        public static void Postfix(ConfirmPopup __instance, ConfirmPopup.ConfirmType ___type) {
            if (___type == AP_SAVE_RESUME) {
                CardCrawlGame.loadingSave = true;
                CardCrawlGame.chosenCharacter = ConnectionResult.character.chosenClass;
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
                ArchipelagoMainMenuButton.archipelagoPreGameScreen.screen = ArchipelagoPreGameScreen.APScreen.team;
            }
        }
    }
}
