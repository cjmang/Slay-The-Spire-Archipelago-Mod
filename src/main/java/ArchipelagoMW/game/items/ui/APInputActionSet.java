package ArchipelagoMW.game.items.ui;

import ArchipelagoMW.client.APClient;
import com.badlogic.gdx.Input;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.helpers.controller.CInputAction;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
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
            ___elements.add(new RemapInputElement(__instance, "AP rewards", APInputActionSet.apmenu, APInputActionSet.cAPMenu));
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(InputSettingsScreen.class, "maxScrollAmount");
                return LineFinder.findInOrder(ctBehavior,match);
            }
        }
    }

    public static CInputAction cAPMenu;
    public static InputAction apmenu;
    private static final String APMENU_KEY = "AP_MENU";
    private static final String C_APMENU_KEY = "C_AP_MENU";

    public static void load() {
        apmenu  = new InputAction(prefs.getInteger(APMENU_KEY, Input.Keys.R));
    }

    public static void save() {
        prefs.putInteger(APMENU_KEY, apmenu.getKey());
    }

    @SpirePatch(clz=CInputActionSet.class, method="load")
    public static class APCLoad
    {
        public static void Postfix() {
            cAPMenu = new CInputAction(prefs.getInteger(C_APMENU_KEY, 9)); // 9 is R3, I think
            CInputHelper.actions.add(cAPMenu);
        }
    }

    @SpirePatch(clz=CInputActionSet.class, method="save")
    public static class APCSave
    {
        public static void Prefix() {
            APClient.logger.info("Button keycode" + cAPMenu.getKey());
            prefs.putInteger(C_APMENU_KEY, cAPMenu.getKey());
        }
    }
}
