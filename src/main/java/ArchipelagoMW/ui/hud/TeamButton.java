package ArchipelagoMW.ui.hud;

import ArchipelagoMW.Archipelago;
import ArchipelagoMW.teams.TeamInfo;
import ArchipelagoMW.ui.APButton;

public class TeamButton extends APButton {

    public TeamInfo team;

    public TeamButton(TeamInfo team) {
        super(team.name, TeamButton::clicked);
        this.team = team;
    }
    public static void clicked(APButton button) {
        TeamButton me = (TeamButton)button;
        Archipelago.sideBar.APTeamsPanel.selectedTeam = me.team;
        Archipelago.sideBar.APTeamsPanel.updateToggles();
    }

    public String getName() {
        return team.name;
    }
}
