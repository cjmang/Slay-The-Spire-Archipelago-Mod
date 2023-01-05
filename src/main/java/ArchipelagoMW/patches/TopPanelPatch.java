package ArchipelagoMW.patches;


import ArchipelagoMW.APClient;
import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TopPanelPatch {

    public static final Logger logger = LogManager.getLogger(TopPanelPatch.class.getName());

    static float ICON_W = 64.0F * Settings.scale;
    static float ICON_Y = Settings.isMobile ? (float) Settings.HEIGHT - ICON_W - 12.0F * Settings.scale : (float) Settings.HEIGHT - ICON_W;// 62
    static float TOP_RIGHT_PAD_X = 10.0F * Settings.scale;
    static float AP_BUTTON_X = (float) Settings.WIDTH - (ICON_W + TOP_RIGHT_PAD_X) * 4.0F;

    static float apIconAngle = 0.0F;
    static float rotateTimer = 0.0F;

    public static Hitbox apButtonHB;

    public TopPanelPatch() {
    }

    @SpirePatch(clz = TopPanel.class, method = SpirePatch.CONSTRUCTOR)
    public static class createAPHitBox {

        @SpirePrefixPatch
        public static void Prefix(TopPanel __instance) {
            apButtonHB = new Hitbox(ICON_W, ICON_W);
            apButtonHB.move(AP_BUTTON_X + ICON_W / 2.0F, ICON_Y + ICON_W / 2.0F);
        }
    }

    @SpirePatch(clz = TopPanel.class, method = "updateButtons")
    public static class UpdateAPButton {

        @SpirePrefixPatch
        public static void Prefix(TopPanel __instance) {
            updateAPButtonLogic();
        }

        @SpirePostfixPatch
        public static void PostFix(TopPanel __instance) {
            if (apButtonHB.justHovered) {
                CardCrawlGame.sound.play("UI_HOVER");
            }
        }
    }

    @SpirePatch(clz = TopPanel.class, method = "renderTopRightIcons")
    public static class renderAPIcon {

        @SpirePostfixPatch
        public static void PostFix(TopPanel __instance, SpriteBatch sb) {
            TopPanelPatch.renderAPIcon(sb);
        }
    }


    private static void renderAPIcon(SpriteBatch sb) {
        //set the color of the icon based on our current state
        if (APClient.apClient.isConnected()) {
            //check if we are in a room where we want to disable the button
            if (AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE || (AbstractDungeon.getCurrMapNode().y == -1 && AbstractDungeon.actNum != 1))
                sb.setColor(Color.GRAY);
            else
                sb.setColor(Color.WHITE);
        } else {
            sb.setColor(Color.RED);
        }

        // if we are hovering we need to highlight and wiggle the button
        if (apButtonHB.hovered) {// 1820
            sb.setBlendFunction(770, 1);// 1821
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.25F));
            sb.draw(ArchipelagoMW.AP_ICON, AP_BUTTON_X - 32.0F + 32.0F * Settings.scale, ICON_Y - 32.0F + 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, apIconAngle, 0, 0, 64, 64, false, false);
            sb.setBlendFunction(770, 771);
        } else {
            sb.draw(ArchipelagoMW.AP_ICON, AP_BUTTON_X - 32.0F + 32.0F * Settings.scale, ICON_Y - 32.0F + 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, apIconAngle, 0, 0, 64, 64, false, false);// 1803

        }
        FontHelper.renderFontRightTopAligned(sb, FontHelper.topPanelAmountFont, Integer.toString(ArchipelagoRewardScreen.rewardsQueued), AP_BUTTON_X + 58.0F * Settings.scale, ICON_Y + 25.0F * Settings.scale, Color.WHITE);

        apButtonHB.render(sb);
    }

    private static void updateAPButtonLogic() {
        apButtonHB.update();
        if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD) {
            rotateTimer += Gdx.graphics.getDeltaTime() * 4.0F;
            apIconAngle = MathHelper.angleLerpSnap(apIconAngle, MathUtils.sin(rotateTimer) * 15.0F);
        } else if (apButtonHB.hovered) {
            apIconAngle = MathHelper.angleLerpSnap(apIconAngle, 15.0F);
        } else {
            apIconAngle = MathHelper.angleLerpSnap(apIconAngle, 0.0F);
        }

        boolean clickedAPButton = apButtonHB.hovered && InputHelper.justClickedLeft;
        if (clickedAPButton) {
            // if we are disconnected and we click the ap button try new connection.
            if (!APClient.apClient.isConnected()) {
                APClient.newConnection(ArchipelagoMW.address, ArchipelagoMW.slotName, ConnectionPanel.passwordField);
            }
            //ArchipelagoMW.logger.info("room phase: " + AbstractDungeon.getCurrRoom().phase.toString());

            if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD) {
                ArchipelagoRewardScreen.close();
            } else {
                if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMPLETE) {
                    if (AbstractDungeon.getCurrMapNode().y == -1 && AbstractDungeon.actNum != 1) {
                        return;
                    }
                    ArchipelagoRewardScreen.open();
                }
            }
        }
    }
}

