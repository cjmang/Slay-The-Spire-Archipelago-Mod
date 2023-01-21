package ArchipelagoMW.ui.hud;

import ArchipelagoMW.APTextures;
import ArchipelagoMW.teams.TeamInfo;
import ArchipelagoMW.teams.TeamManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class APTeamsPanel {
    private static final Texture IMG = ImageMaster.OPTION_CONFIRM;

    private float x = 0;
    private float y = 0;

    private final CreateButton createButton;
    private final InteractButton interactButton;
    private final LockTeamButton lockTeamButton;

    public CopyOnWriteArrayList<TeamButton> teamButtons;

    public TeamInfo selectedTeam;
    
    float TEAM_INFO_X ;
    float TEAM_INFO_Y;


    float TEAM_MEMBERS_X;
    float TEAM_MEMBERS_Y;

    float toggleX;
    
    ArrayList<APToggleButton> settings = new ArrayList<>();


    public APTeamsPanel() {
        createButton = new CreateButton("+");
        interactButton = new InteractButton("Join Team");
        lockTeamButton = new LockTeamButton("Lock Team");
        teamButtons = new CopyOnWriteArrayList<>();

        settings.add(new APToggleButton("Health Link",APToggleButton.CheckBoxType.HEALTH_LINK));
        settings.add(new APToggleButton("Gold Link",APToggleButton.CheckBoxType.GOLD_LINK));
        settings.add(new APToggleButton("Potion Link",APToggleButton.CheckBoxType.POTION_LINK));

    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;

        TEAM_INFO_X = this.x + 350f * Settings.scale;
        TEAM_INFO_Y = this.y + 12f * Settings.scale;
        toggleX = TEAM_INFO_X + 25f * Settings.scale;
        float toggleYStart = this.y - 90f * Settings.scale;

        for (int i = 0; i < settings.size(); i++) {
            APToggleButton toggle = settings.get(i);
            toggle.setPos(toggleX, toggleYStart - (35f * Settings.scale * i));
        }

        TEAM_MEMBERS_Y = this.y - 85f * Settings.scale;
        TEAM_MEMBERS_X = this.x + 780f * Settings.scale;

        interactButton.set(TEAM_INFO_X + 15f * Settings.scale,TEAM_INFO_Y - 520f * Settings.scale);
        lockTeamButton.set(TEAM_INFO_X + 15f * Settings.scale,TEAM_INFO_Y - 465f * Settings.scale);
    }

    public void update() {

     teamButtons.removeIf(teamButton -> !TeamManager.teams.containsKey(teamButton.getName()));
        for (int i = 0; i < teamButtons.size(); i++) {
            TeamButton teamButton = teamButtons.get(i);
            teamButton.set(this.x + 60f * Settings.scale, this.y - 80f * Settings.scale - teamButton.getHeight() - (teamButton.getHeight() + 5f * Settings.scale) * i);
            teamButton.update();
        }

        if(TeamManager.myTeam != null) {
            interactButton.setLabel("Leave Team");
            createButton.enabled = false;
            createButton.hb.hovered = false;

        } else {
            interactButton.setLabel("Join Team");
            createButton.enabled = true;
        }
        if (selectedTeam != null) {
            if (selectedTeam.leader.equals(CardCrawlGame.playerName)) {
                lockTeamButton.update();
            }

            interactButton.enabled = !selectedTeam.locked;
            interactButton.update();
        }


        createButton.set(this.x + 125f * Settings.scale, this.y - 80f * Settings.scale - createButton.getHeight() - (createButton.getHeight() + 5f * Settings.scale) * teamButtons.size() );
        createButton.update();


        for (APToggleButton toggle : settings) {
            toggle.update();
        }
    }

    public void render(SpriteBatch sb) {
        sb.setColor(Color.WHITE);

        float xScale = .95f;
        float yScale = .75f;

        //draw background
        sb.draw(APTextures.TEAM_SETTINGS_BACKGROUND, this.x, this.y + 70f * Settings.scale * yScale - APTextures.TEAM_SETTINGS_BACKGROUND.getHeight() * Settings.scale * yScale, APTextures.TEAM_SETTINGS_BACKGROUND.getWidth() * Settings.scale * xScale,APTextures.TEAM_SETTINGS_BACKGROUND.getHeight() * Settings.scale * yScale);


        FontHelper.renderFontCentered(sb,FontHelper.menuBannerFont,"Archipelago",this.x + 140f * Settings.scale, this.y + 25f * Settings.scale, Settings.CREAM_COLOR);


        float titleY = this.y - 38f * Settings.scale;

        // yay for font helper
        FontHelper.renderFontCentered(sb,FontHelper.charTitleFont,"Teams",this.x + 175f * Settings.scale, titleY, Settings.CREAM_COLOR);
        FontHelper.renderFontCentered(sb, FontHelper.charTitleFont,"Settings", TEAM_INFO_X + 138f * Settings.scale, titleY,Settings.CREAM_COLOR);
        FontHelper.renderFontCentered(sb, FontHelper.charTitleFont,"Members", TEAM_MEMBERS_X + 0f * Settings.scale, titleY,Settings.CREAM_COLOR);

        for (TeamButton teamButton : teamButtons) {
            teamButton.render(sb);
        }

        createButton.render(sb);

        if(selectedTeam != null) {
            sb.setColor(Color.WHITE);

            for (APToggleButton toggle : settings) {
                toggle.render(sb);
            }

            interactButton.render(sb);

            if(selectedTeam.leader.equals(CardCrawlGame.playerName))
                lockTeamButton.render(sb);

            float nameStep = FontHelper.getHeight(FontHelper.leaderboardFont) + 12f * Settings.scale;
            float membersYStart = TEAM_MEMBERS_Y;
            for (int i = 0; selectedTeam.members != null && i < selectedTeam.members.size(); i++) {
                String name = selectedTeam.members.get(i);
                Color nameColor = Settings.CREAM_COLOR;
                if (name.equals(selectedTeam.leader))
                    nameColor = Settings.GOLD_COLOR;

                FontHelper.renderFontCentered(sb,FontHelper.leaderboardFont,selectedTeam.members.get(i),TEAM_MEMBERS_X + 0f * Settings.scale, membersYStart - nameStep * i , nameColor);
            }
        }

    }

    public void updateToggles() {
        for (APToggleButton setting : settings) {
            setting.updateToggle();
        }
    }
}
