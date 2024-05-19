package ArchipelagoMW.ui.mainMenu;

import ArchipelagoMW.Archipelago;
import ArchipelagoMW.ui.connection.ArchipelagoPreGameScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class ArchipelagoMainMenuButton {

    private static final Logger logger = LogManager.getLogger(ArchipelagoMainMenuButton.class.getName()); // This is our logger! It prints stuff out in the console.

    @SpireEnum
    static MenuButton.ClickResult ARCHIPELAGO;

    static public ArchipelagoPreGameScreen archipelagoPreGameScreen = null;


    //set the text for our new button
    @SpirePatch(clz = MenuButton.class, method = "setLabel")
    public static class SetLabel {
        public static void Postfix(MenuButton button) {
            try {
                if (button.result == ARCHIPELAGO) {
                    Field f_label = MenuButton.class.getDeclaredField("label");
                    f_label.setAccessible(true);
                    f_label.set(button, CardCrawlGame.languagePack.getUIString(Archipelago.getModID() + ":MainMenu").TEXT[0]);
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
                ArchipelagoMainMenuButton.openConnectionInfo();
            }
        }
    }

    public static void openConnectionInfo() {
        logger.info("Opening Connection Window");
        ConnectionPanel.connectionResultText = "";
        archipelagoPreGameScreen = new ArchipelagoPreGameScreen();
        archipelagoPreGameScreen.open();
    }

}
