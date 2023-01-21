package ArchipelagoMW.teams;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.annotations.Expose;

public class PlayerInfo {

    @Expose
    private String name;

    @Expose(serialize = false, deserialize = false)
    public String displayName;
    @Expose
    public int health;
    @Expose
    public int maxHealth;
    @Expose
    public int floor;
    @Expose
    public int gold;
    @Expose(deserialize = false, serialize = false)
    public Color teamColor;
    @Expose
    public String team;
    @Expose
    public long timestamp;

    @Expose(deserialize = false, serialize = false)
    public boolean dirty;

    public PlayerInfo(String name, int health, int maxHealth, int floor, int gold) {
        this.name = name;
        this.displayName = name;
        this.health = health;
        this.maxHealth = maxHealth;
        this.floor = floor;
        this.gold = gold;
        this.dirty = true;
        timestamp = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.dirty = true;
    }

    public void update(PlayerInfo player) {
        if (!this.name.equals(player.name)) {
            this.name = player.name;
            this.dirty = true;
        }
        this.gold = player.gold;
        this.floor = player.floor;
        this.health = player.health;
        this.maxHealth = player.maxHealth;
        this.team = player.team;
        this.teamColor = player.teamColor;
    }
}
