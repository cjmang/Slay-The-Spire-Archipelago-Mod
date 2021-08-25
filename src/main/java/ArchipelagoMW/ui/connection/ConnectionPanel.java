package ArchipelagoMW.ui.connection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionPanel {

    private static final Logger logger = LogManager.getLogger(ConnectionPanel.class.getName()); // This is our logger! It prints stuff out in the console.

    private static final UIStrings uiStrings;
    public static final String[] TEXT;
    public static String addressField = "";
    public static String slotNameField = "";
    private final Color screenColor;
    private final Color uiColor;


    public static String connectionResultText = "";

    // Positioning Magic Numbers
    public float OPTION_X = -190f * Settings.scale;
    public float OPTION_Y = -443f * Settings.yScale;
    public float TITLE_X = -70f * Settings.scale;
    public float TITLE_Y = 10f * Settings.yScale;
    public float CONNECTION_X = 10f * Settings.scale;
    public float CONNECTION_Y = -225f * Settings.yScale;

    // Position
    public float x;
    public float y;

    public enum field {
        none,address,slotname
    }

    public static field selected;

    public Hitbox addressHB = new Hitbox(600,50);
    public Hitbox slotNameHB = new Hitbox(600,50);

    public ConnectionPanel() {
        this.screenColor = new Color(0.0F, 0.0F, 0.0F, 1.0F);// 36
        this.uiColor = new Color(1.0F, 1.0F, 1.0F, 1.0F);// 37

        this.move(Settings.WIDTH * 0.33333F, Settings.HEIGHT * 0.72f);

        //default to address bar being selected
        Gdx.input.setInputProcessor(new AddressTypeHelper());
        selected = field.address;

        addressHB.move(this.x + TITLE_X + 500, this.y + TITLE_Y - 35.0F);
        slotNameHB.move(this.x + TITLE_X + 500, this.y + TITLE_Y - 123.0F);
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        addressHB.update();
        if(addressHB.hovered && InputHelper.justClickedLeft) {
            addressHB.clickStarted = true;
            CardCrawlGame.sound.play("UI_CLICK_1");
        }

        if(addressHB.clicked) {
            addressHB.clicked = false;
            selected = field.address;
            Gdx.input.setInputProcessor(new AddressTypeHelper());
        }
        if(addressHB.justHovered && selected != field.address) {
            CardCrawlGame.sound.play("UI_HOVER");
        }

        slotNameHB.update();
        if(slotNameHB.hovered && InputHelper.justClickedLeft) {
            slotNameHB.clickStarted = true;
            CardCrawlGame.sound.play("UI_CLICK_1");
        }

        if(slotNameHB.clicked && InputHelper.justReleasedClickLeft) {
            slotNameHB.clicked = false;
            selected = field.slotname;
            Gdx.input.setInputProcessor(new SlotNameTypeHelper());
        }
        if(slotNameHB.justHovered && selected != field.slotname) {
            CardCrawlGame.sound.play("UI_HOVER");
        }

        if((!slotNameHB.hovered || !addressHB.hovered) && InputHelper.justClickedLeft) {
            selected = field.none;
            //select no input box.
            Gdx.input.setInputProcessor(new ScrollInputProcessor());
        }
    }

    public static boolean addressIsFull() {
        return addressField.length() >= 100;
    }
    public static boolean slotNameIsFull() {
        return slotNameField.length() >= 16;
    }

    public void render(SpriteBatch sb) {

        sb.setColor(Color.WHITE.cpy());
        sb.draw(ImageMaster.OPTION_CONFIRM,
                this.x + OPTION_X + 180f * Settings.scale, this.y + OPTION_Y - 100f * Settings.yScale,
                100.0F, 0, 575.0F, 414.0F, Settings.xScale * 1.5F, Settings.yScale * 1.5F, 0.0F, 0, 0, 360, 414, false, false);

        sb.draw(ImageMaster.OPTION_ABANDON,
                this.x + TITLE_X, this.y + TITLE_Y + 4.0F,
                330f / 2.0F, 100f / 2.0F, 1000.0F, 100f, Settings.scale, Settings.scale, 0.0F, 0, 0, 850, 100, false, false);

        FontHelper.renderFontCentered(sb, FontHelper.SCP_cardTitleFont_small, "Connection Info",
                this.x + TITLE_X + 275f, this.y + TITLE_Y + 63F,
                new Color(0.9F, 0.9F, 0.9F, 1.0F), 1.0f);

        FontHelper.renderSmartText(sb, FontHelper.tipBodyFont, connectionResultText,
                this.x + CONNECTION_X , this.y + CONNECTION_Y,
                750, 30, new Color(0.9F, 0.9F, 0.9F, 1.0F), 1f);


        // Address Box Chunk
        if(selected == field.address)
            sb.setColor(Color.LIGHT_GRAY.cpy());
        else if(addressHB.hovered)
            sb.setColor(Color.GRAY.cpy());
        else
            sb.setColor(Color.DARK_GRAY.cpy());

        sb.draw(ImageMaster.INPUT_SETTINGS_ROW,
                this.x + TITLE_X + 183, this.y + TITLE_Y - 98.0F,
                330f / 2.0F, 100f / 2.0F, 600F, 100F, Settings.scale, Settings.scale, 0.0F, 0, 0, 800, 150, false, false);

        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, addressField,
                this.x + TITLE_X + 200.0F, this.y + TITLE_Y - 10,
                100000.0F, 0.0F, this.uiColor, 0.82F);

        String addressText = "Address: ";
        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, addressText,
                this.x + TITLE_X + 200.0F-FontHelper.getSmartWidth(FontHelper.cardTitleFont, addressText, 1000000.0F, 0.0F, 0.82F), this.y + TITLE_Y - 10,
                100000.0F, 0.0F, this.uiColor, 0.82F);

        if (!addressIsFull() && selected == field.address) {
            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "_",
                    this.x + TITLE_X + 200.0F + FontHelper.getSmartWidth(FontHelper.cardTitleFont, addressField, 1000000.0F, 0.0F, 0.82F), this.y + TITLE_Y - 10.0F, 100000.0F, 0.0F, new Color(1.0F, 1.0F, 1.0F, 1.0F), 0.82F);
        }


        // Slot Name Chunk
        if(selected == field.slotname)
            sb.setColor(Color.LIGHT_GRAY.cpy());
        else if(slotNameHB.hovered)
            sb.setColor(Color.GRAY.cpy());
        else
            sb.setColor(Color.DARK_GRAY.cpy());

        sb.draw(ImageMaster.INPUT_SETTINGS_ROW,
                this.x + TITLE_X + 183, this.y + TITLE_Y - 198.0F,
                330f / 2.0F, 100f / 2.0F, 600F, 100F, Settings.scale, Settings.scale, 0.0F, 0, 0, 800, 150, false, false);

        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, slotNameField,
                this.x + TITLE_X + 200.0F, this.y + TITLE_Y - 110,
                100000.0F, 0.0F, this.uiColor, 0.82F);

        String slotText = "Slot Name: ";
        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, slotText,
                this.x + TITLE_X + 200.0F-FontHelper.getSmartWidth(FontHelper.cardTitleFont, slotText, 1000000.0F, 0.0F, 0.82F), this.y + TITLE_Y - 110,
                100000.0F, 0.0F, this.uiColor, 0.82F);

        if (!slotNameIsFull() && selected == field.slotname) {
            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "_",
                    this.x + TITLE_X + 200.0F + FontHelper.getSmartWidth(FontHelper.cardTitleFont, slotNameField, 1000000.0F, 0.0F, 0.82F), this.y + TITLE_Y - 110.0F, 100000.0F, 0.0F, new Color(1.0F, 1.0F, 1.0F, 1.0F), 0.82F);
        }

    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString("SeedPanel");
        TEXT = uiStrings.TEXT;
        addressField = "";
    }
}
