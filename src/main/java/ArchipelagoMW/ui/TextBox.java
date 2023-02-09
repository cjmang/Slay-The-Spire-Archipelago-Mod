package ArchipelagoMW.ui;

import ArchipelagoMW.ui.hud.APToggleButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Clipboard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.HitboxListener;
import com.megacrit.cardcrawl.helpers.ImageMaster;
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
}
