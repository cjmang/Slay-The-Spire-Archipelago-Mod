package ArchipelagoMW.ui.hud;

import ArchipelagoMW.teams.TeamInfo;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.APButton;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.helpers.Hitbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class CreateButton extends APButton {
    public CreateButton(String text) {
        super(text,CreateButton::clicked);
    }

    public static void clicked(APButton button) {
        ArrayList<String> names = new ArrayList<>(Arrays.asList("red","blue","green","violet","gold","purple","pink","gray","black","white"));
        names.removeAll(TeamManager.teams.keySet());
        if(names.isEmpty())
            return;

        Collections.shuffle(names);
        TeamInfo team = new TeamInfo(names.remove(0));
        team.teamColor = TeamManager.colorMap.get(team.name);
        TeamManager.createTeam(team);
    }

}
