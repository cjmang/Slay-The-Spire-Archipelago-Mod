package ArchipelagoMW.ui.hud;

import ArchipelagoMW.APTextures;
import ArchipelagoMW.teams.PlayerInfo;
import ArchipelagoMW.teams.TeamManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.HitboxListener;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

import java.util.Set;

public class PlayerPanel implements Comparable<PlayerPanel>, HitboxListener {

    public boolean shouldRender = false;
    private PlayerInfo player;

    private float currentX = 0f;
    private float currentY = 0f;

    private float startX = 0f;
    private float startY = 0f;

    private float endX = 0f;
    private float endY = 0f;

    private float movementTime = 0f;
    private float travelTime = 1.5f;
    private boolean slideIn = true;


    private static final float barHeightMod = 1.5f;
    private static final float panelWidthMod = .85f;

    private final Hitbox hb = new Hitbox(APTextures.PLAYER_PANEL.getWidth() * Settings.scale * .85f, APTextures.PLAYER_PANEL.getHeight() * Settings.scale);

    private final GlyphLayout glyphLayout = new GlyphLayout();

    //all top bar icons are 64x64 cut that in half(ish) and apply the ui scale.
    private final float ICON_SIZE = 36f * Settings.scale;

    public PlayerPanel(PlayerInfo player) {
        this.player = player;
    }

    public static float getWidth() {
        return APTextures.PLAYER_PANEL.getWidth() * panelWidthMod * Settings.scale;
    }

    public static float getHeight() {
        return APTextures.PLAYER_PANEL.getHeight() * Settings.scale;
    }

    public PlayerInfo getPlayer() {
        return player;
    }

    public void setPos(float x, float y) {
        if (slideIn) {
            this.currentX = x - APTextures.PLAYER_PANEL.getWidth() * panelWidthMod * Settings.scale;
            this.currentY = y;
            slideIn = false;
        }

        this.startX = this.currentX;
        this.startY = this.currentY;

        this.endX = x;
        this.endY = y;

        if (this.movementTime <= 0f)
            this.movementTime = travelTime;
    }

    public void render(SpriteBatch sb) {
        if(!shouldRender)
            return;
        sb.setColor(Color.WHITE);

        //draw background
        sb.draw(APTextures.PLAYER_PANEL, currentX, currentY, APTextures.PLAYER_PANEL.getWidth() * panelWidthMod * Settings.scale, APTextures.PLAYER_PANEL.getHeight() * Settings.scale);

        //draw team bar
        if (player.teamColor != null) {
            sb.setColor(player.teamColor);

            sb.draw(APTextures.TEAM_BAR, currentX, currentY + APTextures.PLAYER_PANEL.getHeight() * Settings.scale - APTextures.TEAM_BAR.getHeight() * barHeightMod * Settings.scale, APTextures.TEAM_BAR.getWidth() * panelWidthMod * Settings.scale, APTextures.TEAM_BAR.getHeight() * barHeightMod * Settings.scale);
            sb.setColor(Color.WHITE);
        }

        //draw name
        FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, player.displayName, currentX + 12f * Settings.scale, currentY + APTextures.PLAYER_PANEL.getHeight() * Settings.scale - APTextures.TEAM_BAR.getHeight() * barHeightMod - 3f * Settings.scale, Settings.CREAM_COLOR);

        //draw health
        sb.draw(ImageMaster.TP_HP, currentX + 5f * Settings.scale, currentY + 3f * Settings.scale, ICON_SIZE, ICON_SIZE);
        FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, Integer.toString(player.health), currentX + 40f * Settings.scale, currentY + 30f * Settings.scale, Settings.RED_TEXT_COLOR);

        //draw gold
        sb.draw(ImageMaster.TP_GOLD, currentX + 130f * Settings.scale - ICON_SIZE, currentY + 3f * Settings.scale, ICON_SIZE, ICON_SIZE);
        FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, Integer.toString(player.gold), currentX + 130f * Settings.scale, currentY + 30f * Settings.scale, Settings.GOLD_COLOR);

        //draw floor
        sb.draw(ImageMaster.TP_FLOOR, currentX + 250f * Settings.scale - ICON_SIZE, currentY + 3f * Settings.scale, ICON_SIZE, ICON_SIZE);
        FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, Integer.toString(player.floor), currentX + 250f * Settings.scale, currentY + 30f * Settings.scale, Settings.CREAM_COLOR);

        this.hb.render(sb);
    }

    public void update() {

        this.hb.encapsulatedUpdate(this);
        this.hb.update();

        if (this.hb.hovered && InputHelper.justClickedLeft) {
            hb.clickStarted = true;
        }


        if (this.movementTime > 0.0F) {
            this.currentX = Interpolation.exp10.apply(this.endX, this.startX, this.movementTime);
            this.currentY = Interpolation.exp10.apply(this.endY, this.startY, this.movementTime);
            this.hb.move(this.currentX + this.hb.width / 2, this.currentY + this.hb.height / 2);

            this.movementTime -= Gdx.graphics.getDeltaTime();

        } else {
            this.movementTime = 0.0f;
            this.currentX = this.endX;
            this.currentY = this.endY;
        }


        if (player.dirty) {
            glyphLayout.setText(FontHelper.topPanelAmountFont, player.getName(), 0, player.getName().length(), Settings.CREAM_COLOR, APTextures.PLAYER_PANEL.getWidth() * .78f * Settings.scale, 8, false, "...");
            String nameString = (glyphLayout.runs.size >= 1) ? glyphLayout.runs.get(0).toString() : "";
            glyphLayout.reset();
            player.displayName = nameString.substring(0, Math.max(nameString.indexOf(", #"), 0));
            player.dirty = false;

            player.teamColor = TeamManager.colorMap.get(player.team);
        }
    }

    @Override
    public int compareTo(PlayerPanel o) {
        String oTeam = o.player.team;
        String myTeam = this.player.team;

        if(oTeam != null && oTeam.equals(myTeam)) // if they are on the same team sort by floor
            return o.player.floor - this.player.floor;

        if (TeamManager.myTeam != null) { // not same team if one of them are on the active players team push up
            if (oTeam != null && oTeam.equals(TeamManager.myTeam.name)) {
                return 1;
            }
            if (myTeam != null && myTeam.equals(TeamManager.myTeam.name)) {
                return -1;
            }
        }
        if(oTeam != null && myTeam != null) // both players have a team sort alphabetically
            return oTeam.compareTo(myTeam);

        return o.player.floor - this.player.floor; // sort by floor
    }

    @Override
    public void hoverStarted(Hitbox hitbox) {
    }

    @Override
    public void startClicking(Hitbox hitbox) {

    }

    @Override
    public void clicked(Hitbox hitbox) {
    }

    public String getName() {
        return this.player.getName();
    }

    public void slideIn(boolean slideIn) {
        this.slideIn = true;
    }
}
