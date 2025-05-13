package ArchipelagoMW.game.teams.ui.hud;

import ArchipelagoMW.game.teams.TeamManager;
import ArchipelagoMW.game.ui.Components.APButton;

public class CreateButton extends APButton {
    public CreateButton(String text) {
        super(text, CreateButton::clicked);
    }

    public static void clicked(APButton button) {
        TeamManager.createTeam();
    }

}
