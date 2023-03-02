package ArchipelagoMW.ui.Components;

import ArchipelagoMW.teams.TeamInfo;
import ArchipelagoMW.ui.connection.APTeamsPanel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;

public class TeamButton extends APButton {

    public TeamInfo team;

    public TeamButton(TeamInfo team) {
        super(team.name, 250f * Settings.scale, ArchipelagoMW.ui.Components.TeamButton::clicked);
        this.team = team;
    }

    public static void clicked(APButton button) {
        ArchipelagoMW.ui.Components.TeamButton me = (ArchipelagoMW.ui.Components.TeamButton) button;
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
