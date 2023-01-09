package ArchipelagoMW.ui.mainMenu;

import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.ui.connection.ConnectionInfoScreen;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class NewMenuButtons {

    private static final Logger logger = LogManager.getLogger(NewMenuButtons.class.getName()); // This is our logger! It prints stuff out in the console.

    @SpireEnum
    static MenuButton.ClickResult ARCHIPELAGO;

    static public ConnectionInfoScreen connectionInfoScreen = null;


    //set the text for our new button
    @SpirePatch(clz = MenuButton.class, method = "setLabel")
    public static class SetLabel {
        public static void Postfix(MenuButton button) {
            try {
                if (button.result == ARCHIPELAGO) {
                    Field f_label = MenuButton.class.getDeclaredField("label");
                    f_label.setAccessible(true);
                    f_label.set(button, CardCrawlGame.languagePack.getUIString(ArchipelagoMW.getModID() + ":MainMenu").TEXT[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //set what to do when the button is clicked;
    @SpirePatch(clz = MenuButton.class, method = "buttonEffect")
    public static class ButtonEffect {
        public static void Postfix(MenuButton button) {
            if (button.result == ARCHIPELAGO) {
                NewMenuButtons.openConnectionInfo();
            }
        }
    }

    public static void openConnectionInfo() {
        logger.info("I should be opening the connection window now...");
        connectionInfoScreen = new ConnectionInfoScreen();
        connectionInfoScreen.open();
    }

}
