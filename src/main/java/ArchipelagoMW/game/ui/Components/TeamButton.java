package ArchipelagoMW.game.ui.Components;

import ArchipelagoMW.game.teams.TeamInfo;
import ArchipelagoMW.game.connect.ui.connection.APTeamsPanel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;

public class TeamButton extends APButton {

    public TeamInfo team;

    public TeamButton(TeamInfo team) {
        super(team.name, 250f * Settings.scale, TeamButton::clicked);
        this.team = team;
    }

    public static void clicked(APButton button) {
        TeamButton me = (TeamButton) button;
        APTeamsPanel.selectedTeam = me.team;
        APTeamsPanel.updateToggles();
    }

    @Override
    public void render(SpriteBatch sb) {
        this.tint = (this.team == APTeamsPanel.selectedTeam) ? Color.SKY : Color.LIGHT_GRAY;
        super.render(sb);
    }

    public String getName() {
        return team.name;
    }
}
