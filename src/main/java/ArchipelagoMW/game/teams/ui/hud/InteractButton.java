package ArchipelagoMW.game.teams.ui.hud;

import ArchipelagoMW.game.teams.TeamManager;
import ArchipelagoMW.game.ui.Components.APButton;
import ArchipelagoMW.game.connect.ui.connection.APTeamsPanel;
import com.megacrit.cardcrawl.core.Settings;

public class InteractButton extends APButton {

    public InteractButton(String text) {
        super(text, 230f * Settings.scale, InteractButton::clicked);
    }

    public static void clicked(APButton button) {
        if (TeamManager.myTeam == null && APTeamsPanel.selectedTeam != null)
            TeamManager.joinTeam(APTeamsPanel.selectedTeam);
        else if (TeamManager.myTeam != null && !TeamManager.myTeam.locked)
            TeamManager.leaveTeam();

    }

}
