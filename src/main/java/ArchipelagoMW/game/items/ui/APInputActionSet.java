package ArchipelagoMW.game.items.ui;

import com.badlogic.gdx.Input;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputActionSet;
import com.megacrit.cardcrawl.screens.options.InputSettingsScreen;
import com.megacrit.cardcrawl.screens.options.RemapInputElement;
import javassist.CtBehavior;

import java.util.ArrayList;

import static com.megacrit.cardcrawl.helpers.input.InputActionSet.prefs;

public class APInputActionSet {

    @SpirePatch(clz = InputActionSet.class, method = "load")
    public static class load {
        public static void Prefix() {
            load();
        }
    }

    @SpirePatch(clz = InputActionSet.class, method = "save")
    public static class save {
        public static void Prefix() {
            save();
        }
    }

    @SpirePatch(clz = InputSettingsScreen.class, method = "refreshData")
    public static class refreshData {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(InputSettingsScreen __instance, ArrayList<RemapInputElement> ___elements) {
            ___elements.add(new RemapInputElement(__instance, "AP rewards", APInputActionSet.apmenu));
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(InputSettingsScreen.class, "maxScrollAmount");
                return LineFinder.findInOrder(ctBehavior,match);
            }
        }
    }

    public static InputAction apmenu;
    private static final String APMENU_KEY = "AP_MENU";

    public static void load() {
        apmenu  = new InputAction(prefs.getInteger(APMENU_KEY, Input.Keys.Q));
    }

    public static void save() {
        prefs.putInteger(APMENU_KEY, apmenu.getKey());
    }
}
