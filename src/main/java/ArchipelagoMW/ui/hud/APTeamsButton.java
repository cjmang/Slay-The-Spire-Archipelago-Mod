package ArchipelagoMW.ui.hud;

import ArchipelagoMW.Archipelago;
import ArchipelagoMW.ui.APButton;
import basemod.ModLabeledButton;
import basemod.ModPanel;
import basemod.ReflectionHacks;
import basemod.helpers.UIElementModificationHelper;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;

public class APTeamsButton extends APButton {
    public APTeamsButton(String text) {
        super(text, APTeamsButton::click);
        //super(text, 100f * Settings.scale,60f * Settings.scale);
    }

    public static void click(APButton button) {
        if(button.enabled)
            Archipelago.sideBar.showJoinGroupPanel = !Archipelago.sideBar.showJoinGroupPanel;
    }

}
