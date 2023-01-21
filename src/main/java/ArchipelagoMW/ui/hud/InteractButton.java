package ArchipelagoMW.ui.hud;

import ArchipelagoMW.Archipelago;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.APButton;

public class InteractButton extends APButton {

    public InteractButton(String text) {
        super(text, InteractButton::clicked);
    }

    public static void clicked(APButton button) {
        if (TeamManager.myTeam == null && Archipelago.sideBar.APTeamsPanel.selectedTeam != null)
            TeamManager.joinTeam(Archipelago.sideBar.APTeamsPanel.selectedTeam);
        else
            TeamManager.leaveTeam();

    }

}
