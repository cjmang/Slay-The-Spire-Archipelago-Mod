package ArchipelagoMW.saythespire.patches;

import ArchipelagoMW.game.start.patches.CharacterSelectScreenPatch;
import ArchipelagoMW.saythespire.SayTheSpire;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import sayTheSpire.ui.elements.CharacterButtonElement;


public class CharacterOptionPatchPatch {

    @SpirePatch2(cls="CharacterOptionPatch", requiredModId = "Say_the_Spire", method="Prefix", paramtypez = CharacterOption.class)
    public static class APOptionPatch
    {
        public static void Replace(CharacterOption ___instance)
        {
            ___instance.hb.update();
            if(___instance.hb.justHovered)
            {
                CharacterButtonElement button = new CharacterButtonElement(___instance);
                sayTheSpire.Output.setUI(button);
                if(CharacterSelectScreenPatch.CompletedChar.completed.get(___instance)) {
                    SayTheSpire.sts.output("goal completed");
                }
            }
        }
    }

}
