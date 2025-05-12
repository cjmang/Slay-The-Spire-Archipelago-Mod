package ArchipelagoMW.ui.connection;

import ArchipelagoMW.APClient;
import ArchipelagoMW.APSettings;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.apEvents.ConnectionResult;
import ArchipelagoMW.patches.CharacterSelectScreenPatch;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.mainMenu.ArchipelagoMainMenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArchipelagoPreGameScreen {

    private static final Logger logger = LogManager.getLogger(ArchipelagoPreGameScreen.class.getName()); // This is our logger! It prints stuff out in the console.

    private static final UIStrings uiStrings;
    public static final String[] TEXT;

    public static class Enum {
        @SpireEnum
        public static MainMenuScreen.CurScreen CONNECTION_INFO;
    }

    public GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(TEXT[4]);
    public MenuCancelButton backButton = new MenuCancelButton();

    public final ConnectionPanel connectionPanel;
    public final APTeamsPanel teamPanel;

    public APScreen screen = APScreen.connection;

    public enum APScreen {
        connection, team, charSelect
    }

    public ArchipelagoPreGameScreen() {
        connectionPanel = new ConnectionPanel();
        teamPanel = new APTeamsPanel();
        teamPanel.setPos(450 * Settings.scale,800 * Settings.scale);

    }

    public void open() {

        // Swap to our screen
        CardCrawlGame.mainMenuScreen.darken();
        CardCrawlGame.mainMenuScreen.screen = Enum.CONNECTION_INFO;

        // Set Button text
        backButton.show(CharacterSelectScreen.TEXT[5]);

        //show confirm button
        confirmButton.show();
        confirmButton.isDisabled = false;
    }

    public void backToMenu() {
        CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
        CardCrawlGame.mainMenuScreen.lighten();
        backButton.hide();
        confirmButton.hide();
        Gdx.input.setInputProcessor(new ScrollInputProcessor());
    }

    public void toCharSelect() {
        CharacterSelectScreenPatch.lockNonAPChars();
        CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.CHAR_SELECT;
    }

    //update when something happens on our screen.
    public void update() {

        if (!connectionPanel.resumeSave.shown) {
            //back button
            backButton.update();
            if (backButton.hb.clicked || InputHelper.pressedEscape) {
                backButton.hb.clicked = false;
                InputHelper.pressedEscape = false;
                backToMenu();
                TeamManager.leaveTeam();
            }

            confirmButton.update();
            if (confirmButton.hb.clicked || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                Gdx.input.setInputProcessor(new ScrollInputProcessor());
                confirmButton.hb.clicked = false;
                switch (screen) {
                    case connection:
                        ConnectionPanel.connectionResultText = TEXT[5];
                        APSettings.address = connectionPanel.addressTextBox.getText();
                        APSettings.slot = connectionPanel.slotNameTextBox.getText();
                        APSettings.password = connectionPanel.passwordTextBox.getText();
                        APSettings.saveSettings();
                        Archipelago.setConnectionInfo(ArchipelagoMainMenuButton.archipelagoPreGameScreen.connectionPanel.addressTextBox.getText(), ArchipelagoMainMenuButton.archipelagoPreGameScreen.connectionPanel.slotNameTextBox.getText(), ArchipelagoMainMenuButton.archipelagoPreGameScreen.connectionPanel.passwordTextBox.getText());
                        APClient.newConnection(Archipelago.address, Archipelago.slotName, Archipelago.password.isEmpty() ? null : Archipelago.password);
                        ArchipelagoMainMenuButton.archipelagoPreGameScreen.confirmButton.updateText("Select Character");
                        break;
                    case team:
                        // TODO: needs to start game, need to figure out how for multichar
                        ConnectionResult.start();
                        TeamManager.lockTeam();
                        break;
                }
            }
        }

        //pass the update to our address panel.
        switch (screen) {
            case connection:
                connectionPanel.update();
                break;
//            case team:
//                ConnectionResult.start();
//                teamPanel.update();
//                break;
            case charSelect:
                toCharSelect();
                break;
        }
    }

    //this will be called to render our screen
    public void render(SpriteBatch sb) {
        FontHelper.renderFontCentered(sb, FontHelper.SCP_cardTitleFont_small, TEXT[0],
                Settings.WIDTH / 2.0f,
                Settings.HEIGHT - 70.0f * Settings.yScale,
                Settings.GOLD_COLOR);

        switch (screen) {
            case connection:
                this.connectionPanel.render(sb);
                break;
            case team:
                this.teamPanel.render(sb);
                break;
        }

        if (!connectionPanel.resumeSave.shown) {
            this.backButton.render(sb);
            this.confirmButton.render(sb);
        }
    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString(Archipelago.getModID() + ":ConnectionMenu");
        TEXT = uiStrings.TEXT;
    }

}
