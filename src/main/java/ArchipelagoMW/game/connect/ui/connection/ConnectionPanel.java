package ArchipelagoMW.game.connect.ui.connection;

import ArchipelagoMW.mod.APSettings;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.game.ui.Components.TextBox;
import ArchipelagoMW.saythespire.SayTheSpire;
import ArchipelagoMW.saythespire.UIElement;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionPanel {

    private static final Logger logger = LogManager.getLogger(ConnectionPanel.class.getName()); // This is our logger! It prints stuff out in the console.

    private static final UIStrings uiStrings;
    public static final String[] TEXT;
    private final Color uiColor;


    public static String connectionResultText = "";

    // Position
    public float x = Settings.WIDTH / 6f;
    public float y = Settings.HEIGHT * 0.8f;

    // Positioning Magic Numbers
    public float BACKGROUND_X = this.x + 0f;
    public float BACKGROUND_Y = this.y + 20f * Settings.yScale;

    public float TITLE_X = this.x + 10f * Settings.scale;
    public float TITLE_Y = BACKGROUND_Y - 70f * Settings.scale;

    public float OPTION_X = this.x + 70f * Settings.scale;
    public final float OPTION_Y_START = this.y - 110f * Settings.scale;
    public float currentY = OPTION_Y_START;
    public float stepY = 50f * Settings.scale;

    public float PANEL_WIDTH = Settings.WIDTH * .666f;
    public float PANEL_HEIGHT = 750f * Settings.scale;

    public final BitmapFont textFont = FontHelper.cardTitleFont;
    float addressOffset = FontHelper.getSmartWidth(textFont, TEXT[2], 1000000.0F, 0.0F, 1f);
    float slotNameOffset = FontHelper.getSmartWidth(textFont, TEXT[3], 1000000.0F, 0.0F, 1f);
    float passwordOffset = FontHelper.getSmartWidth(textFont, TEXT[6], 1000000.0F, 0.0F, 1f);

//    public final ConfirmPopup resumeSave;

    public UIElement<TextBox> addressTextBox;
    public UIElement<TextBox> slotNameTextBox;
    public UIElement<TextBox> passwordTextBox;


    public ConnectionPanel() {
        this.uiColor = new Color(1.0F, 1.0F, 1.0F, 1.0F);// 37

        addressTextBox = SayTheSpire.sts.wrapTextBox(new TextBox(OPTION_X + addressOffset, currentY, PANEL_WIDTH * .85f - addressOffset), "address");
        addressTextBox.getObject().setText(APSettings.address);
        currentY -= stepY;
        slotNameTextBox = SayTheSpire.sts.wrapTextBox(new TextBox(OPTION_X + slotNameOffset, currentY, PANEL_WIDTH * .85f - slotNameOffset), "slot name");
        slotNameTextBox.getObject().setText(APSettings.slot);
        currentY -= stepY;
        passwordTextBox = SayTheSpire.sts.wrapTextBox(new TextBox(OPTION_X + passwordOffset, currentY, PANEL_WIDTH * .85f - passwordOffset), "password");
        passwordTextBox.getObject().setText(APSettings.password);
        currentY -= stepY;
//        resumeSave = new ConfirmPopup("Resume?", "Archipelago Save Detected would you like to resume?", ConfirmPopupPatch.AP_SAVE_RESUME);
    }

    public void update() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            if (Gdx.input.getInputProcessor().equals(addressTextBox.getObject())) {
                Gdx.input.setInputProcessor(slotNameTextBox.getObject());
                SayTheSpire.sts.setUI(slotNameTextBox);
            } else if (Gdx.input.getInputProcessor().equals(slotNameTextBox.getObject())) {
                Gdx.input.setInputProcessor(passwordTextBox.getObject());
                SayTheSpire.sts.setUI(passwordTextBox);
            } else if (Gdx.input.getInputProcessor().equals(passwordTextBox.getObject())) {
                Gdx.input.setInputProcessor(addressTextBox.getObject());
                SayTheSpire.sts.setUI(addressTextBox);
            }
            else
            {
                Gdx.input.setInputProcessor(addressTextBox.getObject());
                SayTheSpire.sts.setUI(addressTextBox);
            }
        }
        addressTextBox.getObject().update();
        slotNameTextBox.getObject().update();
        passwordTextBox.getObject().update();
    }

    public void render(SpriteBatch sb) {
        //background panel
        sb.setColor(Color.WHITE.cpy());
        sb.draw(ImageMaster.OPTION_CONFIRM,
                BACKGROUND_X,
                BACKGROUND_Y - PANEL_HEIGHT,
                PANEL_WIDTH,
                PANEL_HEIGHT);

        float titleWidth = 600f * Settings.scale;
        float titleHeight = 100f * Settings.scale;

        // title background
        sb.draw(ImageMaster.OPTION_ABANDON,
                TITLE_X,
                TITLE_Y,
                titleWidth,
                titleHeight);

        FontHelper.renderFontCentered(sb,
                FontHelper.SCP_cardTitleFont_small,
                TEXT[1],
                OPTION_X + titleWidth * 0.5f,
                TITLE_Y + titleHeight * 0.55f,
                new Color(0.9F, 0.9F, 0.9F, 1.0F), 1.0f);

        currentY = OPTION_Y_START;

        // Address Box Chunk
        FontHelper.renderSmartText(sb, textFont, TEXT[2],
                OPTION_X,
                currentY + addressTextBox.getObject().height / 2 + FontHelper.getHeight(textFont) / 2,
                100000.0F,
                0.0F,
                this.uiColor,
                1f);
        addressTextBox.getObject().render(sb);
        currentY -= stepY;

        // Slot Name Chunk
        FontHelper.renderSmartText(sb, textFont, TEXT[3],
                OPTION_X,
                currentY + slotNameTextBox.getObject().height / 2 + FontHelper.getHeight(textFont) / 2,
                100000.0F,
                0.0F,
                this.uiColor,
                1f);
        slotNameTextBox.getObject().render(sb);
        currentY -= stepY;

        FontHelper.renderSmartText(sb, textFont, TEXT[6],
                OPTION_X,
                currentY + passwordTextBox.getObject().height / 2 + FontHelper.getHeight(textFont) / 2,
                100000.0F,
                0.0F,
                this.uiColor,
                1f);
        passwordTextBox.getObject().render(sb);
        currentY -= stepY;

        FontHelper.renderSmartText(sb,
                FontHelper.tipBodyFont,
                connectionResultText,
                OPTION_X,
                currentY,
                PANEL_WIDTH,
                30,
                new Color(0.9F, 0.9F, 0.9F, 1.0F), 1f);

    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString(Archipelago.getModID() + ":ConnectionMenu");
        TEXT = uiStrings.TEXT;
    }
}
