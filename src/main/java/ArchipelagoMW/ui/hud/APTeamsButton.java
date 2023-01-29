package ArchipelagoMW.ui.hud;

import ArchipelagoMW.Archipelago;
import ArchipelagoMW.ui.APButton;

public class APTeamsButton extends APButton {
    public APTeamsButton(String text) {
        super(text, APTeamsButton::click);
        //super(text, 100f * Settings.scale,60f * Settings.scale);
    }

    public static void click(APButton button) {
        if(button.enabled)
            Archipelago.sideBar.showTeamPanel = !Archipelago.sideBar.showTeamPanel;
    }

}
