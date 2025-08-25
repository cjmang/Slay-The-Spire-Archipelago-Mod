package ArchipelagoMW.game.patches;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;

public class DownfallFinalActPatch {

    @SpirePatch(cls="downfall.events.HeartEvent",
            method=SpirePatch.CONSTRUCTOR,
            paramtypez = {boolean.class},
            optional = true,
            requiredModId = "downfall")
    public static class ForceSetFinalAct {

        @SpirePostfixPatch
        public static void setFinalAct()
        {
            CharacterManager characterManager = APContext.getContext().getCharacterManager();
            CharacterConfig config = characterManager.getCurrentCharacterConfig();
            if(config != null)
            {
                Settings.isFinalActAvailable = config.finalAct;
            }
        }
    }
}
