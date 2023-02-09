package ArchipelagoMW.ui.connection;

import ArchipelagoMW.APClient;
import ArchipelagoMW.APSettings;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.ui.mainMenu.NewMenuButtons;
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

public class ConnectionInfoScreen {

    private static final Logger logger = LogManager.getLogger(ConnectionInfoScreen.class.getName()); // This is our logger! It prints stuff out in the console.

    private static final UIStrings uiStrings;
    public static final String[] TEXT;

    public static class Enum {
        @SpireEnum
        public static MainMenuScreen.CurScreen CONNECTION_INFO;
    }

    public GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(TEXT[4]);
    public MenuCancelButton backButton = new MenuCancelButton();

    public static String address;
    private float waitTimer;


    public final ConnectionPanel addressPanel;

    public ConnectionInfoScreen() {
        addressPanel = new ConnectionPanel();
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

    //update when something happens on our screen.
    public void update() {

        if(!addressPanel.resumeSave.shown) {
            //back button
            backButton.update();
            if (backButton.hb.clicked || InputHelper.pressedEscape) {
                backButton.hb.clicked = false;
                InputHelper.pressedEscape = false;
                backToMenu();
            }


            confirmButton.update();
            if (confirmButton.hb.clicked || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                Gdx.input.setInputProcessor(new ScrollInputProcessor());
                confirmButton.hb.clicked = false;
                ConnectionPanel.connectionResultText = TEXT[5];
                APSettings.address = addressPanel.addressTextBox.getText();
                APSettings.slot = addressPanel.slotNameTextBox.getText();
                APSettings.password = addressPanel.passwordTextBox.getText();
                APSettings.saveSettings();
                Archipelago.setConnectionInfo(NewMenuButtons.connectionInfoScreen.addressPanel.addressTextBox.getText(), NewMenuButtons.connectionInfoScreen.addressPanel.slotNameTextBox.getText(), NewMenuButtons.connectionInfoScreen.addressPanel.passwordTextBox.getText());
                APClient.newConnection(Archipelago.address, Archipelago.slotName, Archipelago.password.isEmpty() ? null : Archipelago.password);
            }
        }

        //pass the update to our address panel.
        addressPanel.update();

    }

    //this will be called to render our screen
    public void render(SpriteBatch sb) {
        FontHelper.renderFontCentered(sb, FontHelper.SCP_cardTitleFont_small, TEXT[0],
                Settings.WIDTH / 2.0f,
                Settings.HEIGHT - 70.0f * Settings.yScale,
                Settings.GOLD_COLOR);

        this.addressPanel.render(sb);
        if(!addressPanel.resumeSave.shown) {
            this.backButton.render(sb);
            this.confirmButton.render(sb);
        }
    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString(Archipelago.getModID() + ":ConnectionMenu");
        TEXT = uiStrings.TEXT;
    }

}
