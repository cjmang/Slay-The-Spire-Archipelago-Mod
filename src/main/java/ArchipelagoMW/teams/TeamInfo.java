package ArchipelagoMW.teams;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Arrays;

public class TeamInfo {

    @Expose
    public String name;
    @Expose
    public boolean healthLink = false;
    @Expose
    public boolean potionLink = false;
    @Expose
    public boolean goldLink = false;

    @Expose
    public Color teamColor;

    @Expose(deserialize = false, serialize = false)
    public ArrayList<String> members = new ArrayList<>();

    @Expose
    public String leader;

    @Expose
    public boolean locked;

    public TeamInfo(String name) {
        this.name = name;
    }

    public void update(TeamInfo team) {
        this.goldLink = team.goldLink;
        this.potionLink = team.potionLink;
        this.healthLink = team.healthLink;
        this.teamColor = team.teamColor;
        this.leader = team.leader;
        this.locked = team.locked;
    }
}
