package ArchipelagoMW.ui.mainMenu;


import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;

@SpirePatch(
        clz = MainMenuScreen.class,
        method = "setMainMenuButtons"
)
public class MainMenuItems {

    @SpireInsertPatch(rloc = 16, localvars = {"index"})
    public static SpireReturn Insert(Object menuObject, @ByRef int[] index) {
        MainMenuScreen menu = (MainMenuScreen) menuObject;
        menu.buttons.add(new MenuButton(NewMenuButtons.ARCHIPELAGO, index[0]++));
        return SpireReturn.Return(null);
    }

}
