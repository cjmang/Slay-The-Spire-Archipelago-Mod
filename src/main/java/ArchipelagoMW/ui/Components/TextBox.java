package ArchipelagoMW.ui.Components;

import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.hud.APTeamsPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Clipboard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor;

public class TextBox implements InputProcessor, HitboxListener {

    private float x;
    private float y;

    public final float width;
    public final float height;
    public Hitbox hb;

    private Color tint;
    private String text;
    private final float flashTime = 1f;
    private float flashCountdown = flashTime;

    private final BitmapFont font = FontHelper.cardTitleFont;
    private float fontHeight;
    public TextBox(float x, float y, float width) {
        tint = Color.GRAY;
        this.width = width;
        this.height = 40 * Settings.scale;
        this.x = x;
        this.y = y;
        hb = new Hitbox(this.width, this.height);
        hb.move(x + hb.width / 2, y + hb.height / 2);
        text = "";
        fontHeight = FontHelper.getHeight(font);
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        hb.encapsulatedUpdate(this);
        hb.update();

        if (Gdx.input.getInputProcessor() != this) {
            tint = hb.hovered ? Color.LIGHT_GRAY: Color.GRAY;
        }
        else if (!hb.hovered && InputHelper.justClickedLeft) {
            Gdx.input.setInputProcessor(new ScrollInputProcessor());
            tint = Color.GRAY;
        } else {
            tint = Color.WHITE;
        }
    }
    public void render(SpriteBatch sb) {
        sb.setColor(tint);
        // render background box
        sb.draw(ImageMaster.INPUT_SETTINGS_ROW,
                x,
                y,
                0f,
                0f,
                width,
                height,
                1f,
                1f,
                0f,
                17,
                6,
                1020,
                52,
                false,
                false
        );


        if(text == null) {
            hb.render(sb);
            return;
        }
        // render current input
        FontHelper.renderFont(sb,
                font,
                text,
                x + 10f * Settings.scale,
                y + fontHeight * 0.5f + height * 0.5f,
                Color.WHITE);


        //render flashing input bar if needed.
        flashCountdown -= Gdx.graphics.getDeltaTime();
        if (flashCountdown <= flashTime / 2 && Gdx.input.getInputProcessor() == this) {
            FontHelper.renderFont(sb, font, "|",
                    x + FontHelper.getWidth(
                            font,
                            text,
                            1.0F) + 8f * Settings.scale,
                    y + fontHeight * 0.5f + height * 0.5f,
                    new Color(1.0F, 1.0F, 1.0F, 1.0F));
        }
        if (flashCountdown <= 0)
            flashCountdown = flashTime;

        this.hb.render(sb);
    }

    @Override
    public void clicked(Hitbox hitbox) {
        CardCrawlGame.sound.play("UI_CLICK_1");
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public boolean keyTyped(char character) {
        String input = String.valueOf(character);
        if (input.length() != 1)
            return false;

        if (InputHelper.isPasteJustPressed()) {
            Clipboard clipBoard = Gdx.app.getClipboard();
            StringBuilder pasteText = new StringBuilder();
            for (char c : clipBoard.getContents().toCharArray()) {
                if (font.getData().hasGlyph(c))
                    pasteText.append(c);
            }
            text += pasteText;
            return true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE) && !text.equals("")) {
            text = text.substring(0, text.length() - 1);
            return true;
        }

        if (Character.isIdentifierIgnorable(character) || Character.isISOControl(character) || !font.getData().hasGlyph(character))
            return false;
        text += input;
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void hoverStarted(Hitbox hitbox) {
        CardCrawlGame.sound.play("UI_HOVER");
    }

    @Override
    public void startClicking(Hitbox hitbox) {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static class APToggleButton {

        public enum CheckBoxType {
            HEALTH_LINK, GOLD_LINK, POTION_LINK
        }

        private static final int W = 32;
        private float x;
        private float y;
        public Hitbox hb;
        public boolean enabled = true;

        public boolean checked = false;

        private final CheckBoxType type;

        private final String label;


        public APToggleButton(String label, CheckBoxType type) {
            this.type = type;
            this.label = label;
            this.hb = new Hitbox(200.0F * Settings.scale, 32.0F * Settings.scale);
            this.hb.move(x + 74.0F * Settings.scale, this.y);
        }

        public void updateToggle() {
            if (APTeamsPanel.selectedTeam == null) {
                checked = false;
                enabled = false;
                return;
            }

            this.enabled = !APTeamsPanel.selectedTeam.locked &&
                    CardCrawlGame.playerName.equals(APTeamsPanel.selectedTeam.leader);
            switch (type) {
                case HEALTH_LINK:
                    checked = APTeamsPanel.selectedTeam.healthLink;
                    break;
                case GOLD_LINK:
                    checked = APTeamsPanel.selectedTeam.goldLink;
                    break;
                case POTION_LINK:
                    checked = APTeamsPanel.selectedTeam.potionLink;
            }
        }

        public void update() {
            if(this.enabled) {
                this.hb.update();
                if (this.hb.hovered && (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed())) {
                    InputHelper.justClickedLeft = false;
                    this.toggle();
                }
            }
        }

        public void render(SpriteBatch sb) {
            if (!this.enabled)
                ShaderHelper.setShader(sb, ShaderHelper.Shader.GRAYSCALE);

            if (this.checked) {
                sb.setColor(Color.LIGHT_GRAY);
            } else {
                sb.setColor(Color.SKY);
            }

            float scale = Settings.scale;
            if (this.hb.hovered) {
                sb.setColor(Color.SKY);
                scale = Settings.scale * 1.25F;
            }

            sb.draw(ImageMaster.OPTION_TOGGLE, this.x - 16.0F, this.y - 16.0F, 16.0F, 16.0F, 32.0F, 32.0F, scale, scale, 0.0F, 0, 0, 32, 32, false, false);
            if (this.checked) {
                sb.setColor(Color.WHITE);
                sb.draw(ImageMaster.OPTION_TOGGLE_ON, this.x - 16.0F, this.y - 16.0F, 16.0F, 16.0F, 32.0F, 32.0F, scale, scale, 0.0F, 0, 0, 32, 32, false, false);
            }

            FontHelper.renderSmartText(sb, FontHelper.tipBodyFont,this.label,this.x + 16f * Settings.scale,this.y+8f*Settings.scale,Color.SKY);

            ShaderHelper.setShader(sb, ShaderHelper.Shader.DEFAULT);

            this.hb.render(sb);
        }

        public void toggle() {
            if(APTeamsPanel.selectedTeam == null)
                return;

            this.checked = !this.checked;
            switch (type) {
                case HEALTH_LINK:
                    APTeamsPanel.selectedTeam.healthLink = this.checked;
                    break;
                case GOLD_LINK:
                    APTeamsPanel.selectedTeam.goldLink = this.checked;
                    break;
                case POTION_LINK:
                    APTeamsPanel.selectedTeam.potionLink = this.checked;
            }

            TeamManager.uploadTeam(APTeamsPanel.selectedTeam);
        }

        public void setPos(float x, float y) {
            this.x = x;
            this.y = y;
            this.hb.move(x + this.hb.width / 2 - 18.0F * Settings.scale, y);
        }
    }
}
