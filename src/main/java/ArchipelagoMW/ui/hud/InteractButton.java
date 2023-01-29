package ArchipelagoMW.ui.hud;

import ArchipelagoMW.Archipelago;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.APButton;
import com.megacrit.cardcrawl.core.Settings;

public class InteractButton extends APButton {

    public InteractButton(String text) {
        super(text, 230f * Settings.scale, InteractButton::clicked);
    }

    public static void clicked(APButton button) {
        if (TeamManager.myTeam == null && Archipelago.sideBar.APTeamsPanel.selectedTeam != null)
            TeamManager.joinTeam(Archipelago.sideBar.APTeamsPanel.selectedTeam);
        else if (TeamManager.myTeam != null && !TeamManager.myTeam.locked)
            TeamManager.leaveTeam();

    }

}
