package ArchipelagoMW.ui.hud;

import ArchipelagoMW.APTextures;
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

public class PlayerPanel implements Comparable<PlayerPanel>, HitboxListener {

    private PlayerInfo player;

    private float currentX = 0f;
    private float currentY = 0f;

    private float startX = 0f;
    private float startY = 0f;

    private float endX = 0f;
    private float endY = 0f;

    private float movementTime = 0f;
    private float travelTime = 1.5f;

    private final Hitbox hb = new Hitbox(APTextures.PLAYER_PANEL.getWidth() * Settings.scale * .85f, APTextures.PLAYER_PANEL.getHeight() * Settings.scale);

    private final GlyphLayout glyphLayout = new GlyphLayout();

    //all top bar icons are 64x64 cut that in half(ish) and apply the ui scale.
    private final float ICON_SIZE = 36f * Settings.scale;

    public PlayerPanel(PlayerInfo player) {
        this.setIndex(SidePanel.playerPanels.size(), true);
        this.player = player;
    }

    public PlayerInfo getPlayer() {
        return player;
    }

    public void setIndex(int index) {
        setIndex(index, false);
    }
    public void setIndex(int index, boolean slideIn) {
        float x = 0;
        float y = Settings.HEIGHT - (320f * Settings.scale) - (85 * Math.min(index,5) * Settings.scale);
        if(slideIn) {
            this.currentX = x - APTextures.PLAYER_PANEL.getWidth() * .85f * Settings.scale;
            this.currentY = y;
            this.setPos(x, y);
            return;
        }
        this.setPos(0, y);
    }

    private void setPos(float x, float y) {
        this.startX = this.currentX;
        this.startY = this.currentY;

        this.endX = x;
        this.endY = y;

        if (this.movementTime <= 0f)
            this.movementTime = travelTime;
    }

    public void render(SpriteBatch sb) {
        sb.setColor(Color.WHITE);

        float barHeightMod = 1.5f;
        float panelWidthMod = 1.85f;

        //draw background
        sb.draw(APTextures.PLAYER_PANEL, currentX, currentY, APTextures.PLAYER_PANEL.getWidth() * panelWidthMod * Settings.scale, APTextures.PLAYER_PANEL.getHeight() * Settings.scale);

        //draw team bar
        if(player.teamColor != null) {
            sb.setColor(player.teamColor);

            sb.draw(APTextures.TEAM_BAR, currentX, currentY + APTextures.PLAYER_PANEL.getHeight() * Settings.scale - APTextures.TEAM_BAR.getHeight() * barHeightMod * Settings.scale, APTextures.TEAM_BAR.getWidth() * panelWidthMod * Settings.scale, APTextures.TEAM_BAR.getHeight() * barHeightMod * Settings.scale);
            sb.setColor(Color.WHITE);
        }

        //draw name
        FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, player.displayName, currentX + 12f, currentY + APTextures.PLAYER_PANEL.getHeight() * Settings.scale - APTextures.TEAM_BAR.getHeight() * barHeightMod - 5f, Settings.CREAM_COLOR);

        //draw health
        sb.draw(ImageMaster.TP_HP, currentX + 5f, currentY, ICON_SIZE, ICON_SIZE);
        FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, Integer.toString(player.health), currentX + 34f, currentY + 23f, Settings.RED_TEXT_COLOR);

        //draw gold
        sb.draw(ImageMaster.TP_GOLD, currentX + 74f, currentY, ICON_SIZE, ICON_SIZE);
        FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, Integer.toString(player.gold), currentX + 104f, currentY + 23f, Settings.GOLD_COLOR);

        //draw floor
        sb.draw(ImageMaster.TP_FLOOR, currentX + 163f, currentY, ICON_SIZE, ICON_SIZE);
        FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, Integer.toString(player.floor), currentX + 196f, currentY + 23f, Settings.CREAM_COLOR);

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
            player.displayName = nameString.substring(0, Math.max(nameString.indexOf(", #"),0));
            player.dirty = false;
        }
    }

    @Override
    public int compareTo(PlayerPanel o) {
        return o.player.floor - this.player.floor;
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
}
