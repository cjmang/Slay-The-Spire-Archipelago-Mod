package ArchipelagoMW.ui.connection;

import ArchipelagoMW.ui.mainMenu.NewMenuButtons;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

public class NewScreenUpdateRender {

    //patch in our new screen to the main update method.
    @SpirePatch(clz = MainMenuScreen.class, method = "update")
    public static class Update {
        public static void Postfix(MainMenuScreen menuScreen) {
            if (menuScreen.screen == ConnectionInfoScreen.Enum.CONNECTION_INFO) {
                NewMenuButtons.connectionInfoScreen.update();
            }

        }
    }

    //patch in our new screens to the main renderer.
    @SpirePatch(clz = MainMenuScreen.class, method = "render")
    public static class Render {
        public static void Postfix(MainMenuScreen menuScreen, SpriteBatch sb) {
            if (menuScreen.screen == ConnectionInfoScreen.Enum.CONNECTION_INFO) {
                NewMenuButtons.connectionInfoScreen.render(sb);
            }
        }
    }

}
