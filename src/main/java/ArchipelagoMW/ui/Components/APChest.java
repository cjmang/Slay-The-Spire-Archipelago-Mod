package ArchipelagoMW.ui.Components;

import ArchipelagoMW.APTextures;
import ArchipelagoMW.LocationTracker;
import ArchipelagoMW.teams.TeamInfo;
import ArchipelagoMW.ui.hud.APTeamsPanel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import gg.archipelago.client.parts.NetworkItem;

public class APChest extends AbstractChest {

    private final NetworkItem item;

    public APChest() {
        this.img = APTextures.AP_CHEST;
        this.openedImg = APTextures.AP_CHEST;
        this.hb = new Hitbox(256.0F * Settings.scale, 256.0F * Settings.scale);
        this.hb.move(CHEST_LOC_X, CHEST_LOC_Y - 100.0F * Settings.scale);
        this.item = LocationTracker.sendBossRelic(AbstractDungeon.actNum);
        AbstractDungeon.overlayMenu.proceedButton.setLabel(TEXT[0]);
    }

    public void open(boolean bossChest) {
        CardCrawlGame.sound.play("CHEST_OPEN");
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        if (!this.isOpen) {
            TipHelper.renderGenericTip(CHEST_LOC_X - hb.width / 2, CHEST_LOC_Y + hb.height / 2f, item.itemName, item.playerName);
        }
    }

    public static class TeamButton extends APButton {

        public TeamInfo team;

        public TeamButton(TeamInfo team) {
            super(team.name, 250f * Settings.scale, TeamButton::clicked);
            this.team = team;
        }
        public static void clicked(APButton button) {
            TeamButton me = (TeamButton)button;
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
}

