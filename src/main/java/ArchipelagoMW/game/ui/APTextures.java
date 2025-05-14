package ArchipelagoMW.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.helpers.ImageMaster;

public class APTextures {
    public static Texture AP_BADGE;
    public static Texture AP_ICON;
    public static Texture AP_CHEST;
    public static Texture AP_CAMPFIRE;
    public static Texture PLAYER_PANEL;
    public static Texture TEAM_BAR;
    public static Texture TEAM_SETTINGS_BACKGROUND;

    public static void initialize() {
        AP_BADGE = ImageMaster.loadImage("ArchipelagoMW-2.0Resources/images/Badge.png");
        AP_ICON = ImageMaster.loadImage("ArchipelagoMW-2.0Resources/images/ui/APIcon.png");
        AP_CHEST = ImageMaster.loadImage("ArchipelagoMW-2.0Resources/images/npc/APChest.png");
        AP_CAMPFIRE = ImageMaster.loadImage("ArchipelagoMW-2.0Resources/images/ui/APCampfire.png");
        PLAYER_PANEL = ImageMaster.loadImage("ArchipelagoMW-2.0Resources/images/ui/hud/PlayerPanel.png");
        TEAM_BAR = ImageMaster.loadImage("ArchipelagoMW-2.0Resources/images/ui/hud/TeamColorBar.png");
        TEAM_SETTINGS_BACKGROUND = ImageMaster.loadImage("ArchipelagoMW-2.0Resources/images/ui/hud/TeamSettingsBackground.png");
    }
}
