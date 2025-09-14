package ArchipelagoMW.saythespire.patches;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.game.items.ui.APInputActionSet;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Input;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.helpers.Prefs;
import com.megacrit.cardcrawl.helpers.controller.CInputAction;
import sayTheSpire.ui.input.*;

import java.util.ArrayList;
import java.util.HashMap;

public class InputPatches {

    private static final String AP_ACTION = "archipelago";

    @SpirePatch(cls="sayTheSpire.ui.input.InputActionCollection", method="buildKeyboardDefaults", requiredModId = "Say_the_Spire", paramtypez = MappingBuilder.class)
    public static class AddAPKB
    {
        public static void Postfix(InputActionCollection __instance, MappingBuilder builder)
        {
            builder.action(AP_ACTION).mapping(Input.Keys.R);
        }
    }

    @SpirePatch(cls="sayTheSpire.ui.input.InputActionCollection", method="buildControllerDefaults", requiredModId = "Say_the_Spire", paramtypez = MappingBuilder.class)
    public static class AddAPController
    {
        public static void Postfix(InputActionCollection __instance, MappingBuilder builder, Prefs ___controllerPrefs)
        {
//            throw new RuntimeException();
            builder.action(AP_ACTION).mapping(___controllerPrefs.getInteger("ARCHIPELAGO", 9));
        }
    }

    @SpirePatch(cls="sayTheSpire.ui.input.InputActionCollection", method="setupActions", requiredModId = "Say_the_Spire", paramtypez = JsonObject.class)
    public static class AddAPAction
    {
        public static void Postfix(InputActionCollection __instance,
                                   JsonObject input,
                                   HashMap<String, InputAction> ___actions,
                                   HashMap<String, ArrayList<InputMapping>> ___defaults,
                                   InputManager ___inputManager)
        {
            JsonArray mappingsArray = null;
            if(input != null && input.has(AP_ACTION))
            {
                mappingsArray = input.getAsJsonArray(AP_ACTION);
            }
            InputAction action;
            if(mappingsArray == null)
            {
                action = new InputAction(AP_ACTION, ___inputManager);
                ReflectionHacks.RMethod setMappings = ReflectionHacks.privateMethod(InputAction.class, "setMappings", ArrayList.class);
                setMappings.invoke(action, ___defaults.get(AP_ACTION));
            }
            else
            {
                action = new InputAction(AP_ACTION, ___inputManager, mappingsArray);
            }
            ___actions.put(AP_ACTION, action);
        }
    }


    @SpirePatch(cls="sayTheSpire.ui.input.InputAction", method="getGameControllerAction", requiredModId = "Say_the_Spire")
    public static class FixControllerAction
    {
        public static SpireReturn<CInputAction> Prefix(InputAction __instance) {
            if(AP_ACTION.equals(__instance.getName()))
            {
                return SpireReturn.Return(APInputActionSet.cAPMenu);
            }
            return SpireReturn.Continue();
        }
    }
}
