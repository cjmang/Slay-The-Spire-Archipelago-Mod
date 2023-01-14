package ArchipelagoMW.ui.connection;

import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.patches.ConfirmPopupPatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionPanel {

    private static final Logger logger = LogManager.getLogger(ConnectionPanel.class.getName()); // This is our logger! It prints stuff out in the console.

    private static final UIStrings uiStrings;
    public static final String[] TEXT;
    public static String addressField = "";
    public static String slotNameField = "";
    public static String passwordField = "";
    private final Color uiColor;


    public static String connectionResultText = "";

    // Positioning Magic Numbers
    public float BACKGROUND_X = 0f;
    public float BACKGROUND_Y = 20f * Settings.yScale;
    public float TITLE_X = 10f * Settings.scale;
    public float TITLE_Y = BACKGROUND_Y - 70f;
    public float ADDRESS_X = 70f * Settings.scale;
    public float ADDRESS_Y = -100f * Settings.yScale;
    public float SLOT_NAME_X = 70f * Settings.scale;
    public float SLOT_NAME_Y = -160f * Settings.yScale;
    public float PASSWORD_X = 70f * Settings.scale;
    public float PASSWORD_Y = -220f * Settings.yScale;
    public float CONNECTION_X = 70f * Settings.scale;
    public float CONNECTION_Y = -380f * Settings.yScale;

    public float PANEL_WIDTH = Settings.WIDTH * .666f;
    public float PANEL_HEIGHT = 750f * Settings.scale;

    float addressOffset = FontHelper.getSmartWidth(FontHelper.cardTitleFont, TEXT[2], 1000000.0F, 0.0F, 1f);
    float slotNameOffset = FontHelper.getSmartWidth(FontHelper.cardTitleFont, TEXT[3], 1000000.0F, 0.0F, 1f);
    float passwordOffset = FontHelper.getSmartWidth(FontHelper.cardTitleFont, TEXT[6], 1000000.0F, 0.0F, 1f);

    public static boolean showPassword = false;

    // Position
    public float x;
    public float y;

    public enum field {
        none, address, slotname, password
    }

    public static field selected;

    public Hitbox addressHB = new Hitbox(PANEL_WIDTH * .9f - addressOffset, 50 * Settings.scale);
    public Hitbox slotNameHB = new Hitbox(PANEL_WIDTH * .9f - slotNameOffset, 50 * Settings.scale);
    public Hitbox passwordHB = new Hitbox(PANEL_WIDTH * .9f - passwordOffset, 50 * Settings.scale);

    public final ConfirmPopup resumeSave;

    public ConnectionPanel() {

        this.uiColor = new Color(1.0F, 1.0F, 1.0F, 1.0F);// 37

        this.move(Settings.WIDTH / 6F, Settings.HEIGHT * 0.8f);

        //default to address bar being selected
        Gdx.input.setInputProcessor(new AddressTypeHelper());
        selected = field.address;

        addressHB.move(this.x + ADDRESS_X + addressHB.width / 2 + addressOffset, this.y + ADDRESS_Y);
        slotNameHB.move(this.x + SLOT_NAME_X + slotNameHB.width / 2 + slotNameOffset, this.y + SLOT_NAME_Y);
        passwordHB.move(this.x + PASSWORD_X + passwordHB.width / 2 + passwordOffset, this.y + PASSWORD_Y);

        resumeSave = new ConfirmPopup("Resume?", "Archipelago Save Detected would you like to resume?", ConfirmPopupPatch.AP_SAVE_RESUME);
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        if(!resumeSave.shown) {
            addressHB.update();
            if (addressHB.hovered && InputHelper.justClickedLeft) {
                addressHB.clickStarted = true;
                CardCrawlGame.sound.play("UI_CLICK_1");
            }

            if (addressHB.clicked) {
                addressHB.clicked = false;
                selected = field.address;
                Gdx.input.setInputProcessor(new AddressTypeHelper());
            }
            if (addressHB.justHovered && selected != field.address) {
                CardCrawlGame.sound.play("UI_HOVER");
            }

            slotNameHB.update();
            if (slotNameHB.hovered && InputHelper.justClickedLeft) {
                slotNameHB.clickStarted = true;
                CardCrawlGame.sound.play("UI_CLICK_1");
            }

            if (slotNameHB.clicked && InputHelper.justReleasedClickLeft) {
                slotNameHB.clicked = false;
                selected = field.slotname;
                Gdx.input.setInputProcessor(new SlotNameTypeHelper());
            }
            if (slotNameHB.justHovered && selected != field.slotname) {
                CardCrawlGame.sound.play("UI_HOVER");
            }

            if (showPassword) {
                passwordHB.update();
                if (passwordHB.hovered && InputHelper.justClickedLeft) {
                    passwordHB.clickStarted = true;
                    CardCrawlGame.sound.play("UI_CLICK_1");
                }

                if (passwordHB.clicked && InputHelper.justReleasedClickLeft) {
                    passwordHB.clicked = false;
                    selected = field.password;
                    Gdx.input.setInputProcessor(new PasswordTypeHelper());
                }
                if (passwordHB.justHovered && selected != field.password) {
                    CardCrawlGame.sound.play("UI_HOVER");
                }
            }

            if ((!slotNameHB.hovered || !addressHB.hovered || !passwordHB.hovered) && InputHelper.justClickedLeft) {
                selected = field.none;
                //select no input box.
                Gdx.input.setInputProcessor(new ScrollInputProcessor());
            }
        }
        resumeSave.update();
    }

    public static boolean addressIsFull() {
        return addressField.length() >= 100;
    }

    public static boolean slotNameIsFull() {
        return slotNameField.length() >= 100;
    }

    public static boolean passwordIsFull() {
        return slotNameField.length() >= 100;
    }

    public void render(SpriteBatch sb) {

        //background panel
        sb.setColor(Color.WHITE.cpy());
        sb.draw(ImageMaster.OPTION_CONFIRM,
                this.x + BACKGROUND_X, this.y + BACKGROUND_Y - 750f * Settings.scale,
                PANEL_WIDTH, PANEL_HEIGHT);

        // title background
        sb.draw(ImageMaster.OPTION_ABANDON,
                this.x + TITLE_X, this.y + TITLE_Y,
                600.0F, 100f);

        FontHelper.renderFontCentered(sb, FontHelper.SCP_cardTitleFont_small, TEXT[1],
                this.x + TITLE_X + 600.0F / 2f * Settings.scale, this.y + TITLE_Y + 100f * .5f,
                new Color(0.9F, 0.9F, 0.9F, 1.0F), 1.0f);

        FontHelper.renderSmartText(sb, FontHelper.tipBodyFont, connectionResultText,
                this.x + CONNECTION_X, this.y + CONNECTION_Y,
                PANEL_WIDTH, 30, new Color(0.9F, 0.9F, 0.9F, 1.0F), 1f);


        // Address Box Chunk
        if (selected == field.address)
            sb.setColor(Color.LIGHT_GRAY.cpy());
        else if (addressHB.hovered)
            sb.setColor(Color.GRAY.cpy());
        else
            sb.setColor(Color.DARK_GRAY.cpy());

        renderTextBox(sb, addressOffset, ADDRESS_X, ADDRESS_Y, addressHB, addressField);

        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, TEXT[2],
                this.x + ADDRESS_X, this.y + ADDRESS_Y,
                100000.0F, 0.0F, this.uiColor, 1f);

        if (!addressIsFull() && selected == field.address) {
            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "_",
                    this.x + ADDRESS_X + addressOffset + FontHelper.getSmartWidth(FontHelper.cardTitleFont, addressField, 1000000.0F, 0.0F, 1f), this.y + ADDRESS_Y, 100000.0F, 0.0F, new Color(1.0F, 1.0F, 1.0F, 1.0F), 1f);
        }


        // Slot Name Chunk
        if (selected == field.slotname)
            sb.setColor(Color.LIGHT_GRAY.cpy());
        else if (slotNameHB.hovered)
            sb.setColor(Color.GRAY.cpy());
        else
            sb.setColor(Color.DARK_GRAY.cpy());

        renderTextBox(sb, slotNameOffset, SLOT_NAME_X, SLOT_NAME_Y, slotNameHB, slotNameField);

        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, TEXT[3],
                this.x + SLOT_NAME_X, this.y + SLOT_NAME_Y,
                100000.0F, 0.0F, this.uiColor, 1f);

        if (!slotNameIsFull() && selected == field.slotname) {
            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "_",
                    this.x + SLOT_NAME_X + slotNameOffset + FontHelper.getSmartWidth(FontHelper.cardTitleFont, slotNameField, 1000000.0F, 0.0F, 1f), this.y + SLOT_NAME_Y, 100000.0F, 0.0F, new Color(1.0F, 1.0F, 1.0F, 1.0F), 1f);
        }

        if (showPassword) {
            // password
            if (selected == field.password)
                sb.setColor(Color.LIGHT_GRAY.cpy());
            else if (passwordHB.hovered)
                sb.setColor(Color.GRAY.cpy());
            else
                sb.setColor(Color.DARK_GRAY.cpy());

            String hiddenPassword = StringUtils.repeat("*", passwordField.length());

            renderTextBox(sb, passwordOffset, PASSWORD_X, PASSWORD_Y, passwordHB, hiddenPassword);

            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, TEXT[6],
                    this.x + PASSWORD_X, this.y + PASSWORD_Y,
                    100000.0F, 0.0F, this.uiColor, 1f);

            if (!passwordIsFull() && selected == field.password) {
                FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "_",
                        this.x + PASSWORD_X + passwordOffset + FontHelper.getSmartWidth(FontHelper.cardTitleFont, hiddenPassword, 1000000.0F, 0.0F, 1f), this.y + PASSWORD_Y, 100000.0F, 0.0F, new Color(1.0F, 1.0F, 1.0F, 1.0F), 1f);
            }
        }

        if (showPassword)
            passwordHB.render(sb);

        slotNameHB.render(sb);
        addressHB.render(sb);

        resumeSave.render(sb);
    }

    private void renderTextBox(SpriteBatch sb, float textOffset, float poxX, float posY, Hitbox textHB, String text) {
        sb.draw(ImageMaster.INPUT_SETTINGS_ROW,
                this.x + poxX + textOffset - 18, this.y + posY - textHB.height / 2 - 10,
                PANEL_WIDTH * .9f - textOffset - 18, 50f * Settings.scale);

        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, text,
                this.x + poxX + textOffset, this.y + posY,
                100000.0F, 0.0F, this.uiColor, 1f);
    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString(ArchipelagoMW.getModID() + ":ConnectionMenu");
        TEXT = uiStrings.TEXT;
        addressField = "";
        slotNameField = "";
    }
}
