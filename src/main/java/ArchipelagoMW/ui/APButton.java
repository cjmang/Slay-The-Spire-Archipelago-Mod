package ArchipelagoMW.ui;

import ArchipelagoMW.ui.hud.APTeamsButton;
import basemod.ModLabeledButton;
import basemod.ModPanel;
import basemod.helpers.UIElementModificationHelper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

import java.util.function.Consumer;

public abstract class APButton {

    private Consumer<APButton> click;
    public Hitbox hb;
    private float x;
    private float y;
    private float w;
    private float middle_width;
    private float h;
    public BitmapFont font;
    public String label;
    public Color color;
    public Color colorHover;
    private static final float TEXT_OFFSET = 9.0F;
    private final Texture textureLeft;
    private final Texture textureRight;
    private final Texture textureMiddle;
    public boolean enabled = true;


    public APButton(String label, Consumer<APButton> c) {
        this(label, -10000, -10000, Color.WHITE, Color.GREEN, c);
    }

    public APButton(String label, float xPos, float yPos, Color textColor, Color textColorHover, Consumer<APButton> c) {
        this(label, xPos, yPos, textColor, textColorHover, FontHelper.buttonLabelFont, c);
    }

    public APButton(String label, float xPos, float yPos, Color textColor, Color textColorHover, BitmapFont font, Consumer<APButton> c) {
        this.label = label;
        this.font = font;
        this.color = textColor;
        this.colorHover = textColorHover;
        this.textureLeft = ImageMaster.loadImage("img/ButtonLeft.png");
        this.textureRight = ImageMaster.loadImage("img/ButtonRight.png");
        this.textureMiddle = ImageMaster.loadImage("img/ButtonMiddle.png");
        this.x = xPos;
        this.y = yPos;
        this.middle_width = Math.max(0.0F, FontHelper.getSmartWidth(font, label, 9999.0F, 0.0F) - 18.0F * Settings.scale);
        this.w = (float)(this.textureLeft.getWidth() + this.textureRight.getWidth()) * Settings.scale + this.middle_width;
        this.h = (float)this.textureLeft.getHeight() * Settings.scale * .6f;
        this.hb = new Hitbox(this.x + Settings.scale, this.y + Settings.scale, this.w - 2.0F * Settings.scale, this.h - 2.0F * Settings.scale);
        this.click = c;
    }

    public void set(float xPos, float yPos) {
        this.x = xPos;
        this.y = yPos;
        UIElementModificationHelper.moveHitboxByOriginalParameters(this.hb, this.x + Settings.scale, this.y + Settings.scale);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void update() {
        if(!this.enabled) {
            this.hb.hovered = false;
            return;
        }
        this.hb.update();
        if (this.hb.justHovered) {
            CardCrawlGame.sound.playV("UI_HOVER", 0.75F);
        }

        if (this.hb.hovered && InputHelper.justClickedLeft) {
            CardCrawlGame.sound.playA("UI_CLICK_1", -0.1F);
            this.hb.clickStarted = true;
        }

        if (this.hb.clicked) {
            this.hb.clicked = false;
            this.onClick();
        }

    }

    private void onClick() {
        this.click.accept(this);
    }
    public void render(SpriteBatch sb) {
        sb.setColor(Color.WHITE);
        if(!this.enabled)
            ShaderHelper.setShader(sb, ShaderHelper.Shader.GRAYSCALE);
        sb.draw(this.textureLeft, this.x, this.y, (float)this.textureLeft.getWidth() * Settings.scale, this.h);
        sb.draw(this.textureMiddle, this.x + (float)this.textureLeft.getWidth() * Settings.scale, this.y, this.middle_width, this.h);
        sb.draw(this.textureRight, this.x + (float)this.textureLeft.getWidth() * Settings.scale + this.middle_width, this.y, (float)this.textureRight.getWidth() * Settings.scale, this.h);
        this.hb.render(sb);
        sb.setColor(Color.WHITE);
        if (this.hb.hovered) {
            FontHelper.renderFontCentered(sb, this.font, this.label, this.hb.cX, this.hb.cY, this.colorHover);
        } else {
            FontHelper.renderFontCentered(sb, this.font, this.label, this.hb.cX, this.hb.cY, this.color);
        }
        ShaderHelper.setShader(sb, ShaderHelper.Shader.DEFAULT);
    }


    public float getWidth() {
        return this.w;
    }

    public float getHeight() {
        return this.h;
    }

    public void setX(float xPos) {
        this.set(xPos, this.y );
    }

    public void setY(float yPos) {
        this.set(this.x , yPos);
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }
}
