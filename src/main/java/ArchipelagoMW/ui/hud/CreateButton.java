package ArchipelagoMW.ui.hud;

import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.APButton;

public class CreateButton extends APButton {
    public CreateButton(String text) {
        super(text, CreateButton::clicked);
    }

    public static void clicked(APButton button) {
        TeamManager.createTeam();
    }

}
