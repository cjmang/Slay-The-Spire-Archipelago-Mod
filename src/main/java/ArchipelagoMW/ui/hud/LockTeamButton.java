package ArchipelagoMW.ui.hud;

import ArchipelagoMW.teams.TeamInfo;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.Components.APButton;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;

public class LockTeamButton extends APButton {
    public LockTeamButton(String text) {
        super(text, 230f * Settings.scale, LockTeamButton::clicked);
    }

    public static void clicked(APButton button) {
        if(!button.enabled)
            return;
        TeamInfo selected = APTeamsPanel.selectedTeam;
        if (TeamManager.myTeam != selected || !TeamManager.myTeam.leader.equals(CardCrawlGame.playerName))
            return;

        TeamManager.lockTeam();

    }
}
