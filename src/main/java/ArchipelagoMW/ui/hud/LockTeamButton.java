package ArchipelagoMW.ui.hud;

import ArchipelagoMW.Archipelago;
import ArchipelagoMW.teams.TeamInfo;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.APButton;
import com.megacrit.cardcrawl.core.CardCrawlGame;

public class LockTeamButton extends APButton {
    public LockTeamButton(String text) {
        super(text, LockTeamButton::clicked);
    }

    public static void clicked(APButton button) {
        if(!button.enabled)
            return;
        TeamInfo selected = Archipelago.sideBar.APTeamsPanel.selectedTeam;
        if (TeamManager.myTeam != selected || !TeamManager.myTeam.leader.equals(CardCrawlGame.playerName))
            return;

        TeamManager.lockTeam();

    }
}
