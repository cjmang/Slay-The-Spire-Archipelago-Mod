package ArchipelagoMW.game.connect.ui.connection;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.items.ui.ArchipelagoRewardScreen;
import ArchipelagoMW.mod.APSettings;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.game.start.patches.CharacterSelectScreenPatch;
import ArchipelagoMW.game.teams.TeamManager;
import ArchipelagoMW.game.connect.ui.mainMenu.ArchipelagoMainMenuButton;
import ArchipelagoMW.saythespire.SayTheSpire;
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
    private boolean justOpened = false;

    public final ConnectionPanel connectionPanel;
    public final APTeamsPanel teamPanel;

    public APScreen screen = APScreen.connection;

    public enum APScreen {
        connection, team, charSelect
    }

    public ArchipelagoPreGameScreen() {
        connectionPanel = new ConnectionPanel();
        teamPanel = new APTeamsPanel();
        teamPanel.setPos(450 * Settings.scale, 800 * Settings.scale);

    }

    public void open() {
        justOpened = true;
        // Swap to our screen
        CardCrawlGame.mainMenuScreen.darken();
        CardCrawlGame.mainMenuScreen.screen = Enum.CONNECTION_INFO;
        SayTheSpire.sts.output("connection panel");

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
        CharacterSelectScreenPatch.lockChars();
        CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.CHAR_SELECT;
        SayTheSpire.sts.output("character select");
    }

    //update when something happens on our screen.
    public void update() {

        backButton.update();
        if (backButton.hb.clicked || InputHelper.pressedEscape) {
            backButton.hb.clicked = false;
            InputHelper.pressedEscape = false;
            backToMenu();
            TeamManager.leaveTeam();
        }

        confirmButton.update();
        if (confirmButton.hb.clicked || (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && !justOpened)) {
            Gdx.input.setInputProcessor(new ScrollInputProcessor());
            confirmButton.hb.clicked = false;
            switch (screen) {
                case connection:
                    ConnectionPanel.setConnectionResultText(TEXT[5]);
                    String address = APSettings.address = connectionPanel.addressTextBox.getObject().getText();
                    String slot = APSettings.slot = connectionPanel.slotNameTextBox.getObject().getText();
                    String password = APSettings.password = connectionPanel.passwordTextBox.getObject().getText();
                    APSettings.saveSettings();
                    APClient client = APContext.getContext().getClient();
                    if (!Archipelago.setConnectionInfo(address, slot, password) && client != null && client.isConnected() && client.connectionSucceeded()) {
                        ArchipelagoRewardScreen.rewardsQueued = 0;
                        APContext.getContext().getSaveManager().loadSaves();
                        screen = APScreen.charSelect;
                    } else {
                        APClient.newConnection(APContext.getContext(), Archipelago.address, Archipelago.slotName, Archipelago.password.isEmpty() ? null : Archipelago.password);
                    }
                    ArchipelagoMainMenuButton.archipelagoPreGameScreen.confirmButton.updateText("Select Character");
                    break;
//                    case team:
//                        ConnectionResult.start();
//                        TeamManager.lockTeam();
//                        break;
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
        justOpened = false;
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
//            case team:
//                this.teamPanel.render(sb);
//                break;
        }

        this.backButton.render(sb);
        this.confirmButton.render(sb);
    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString(Archipelago.getModID() + ":ConnectionMenu");
        TEXT = uiStrings.TEXT;
    }

}
