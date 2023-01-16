package ArchipelagoMW.ui.hud;

import com.badlogic.gdx.graphics.Color;

public class PlayerInfo {

        private String name;
        public String displayName;
        public int health;
        public int floor;
        public int gold;

        public Color teamColor;
        public String team;

        public boolean dirty;

        public PlayerInfo(String name, int health, int floor, int gold) {
            this.name = name;
            this.displayName = name;
            this.health = health;
            this.floor = floor;
            this.gold = gold;
            this.dirty = true;
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
            this.team = player.team;
            this.teamColor = player.teamColor;
    }
}
