package ArchipelagoMW;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.helpers.ImageMaster;

public class APTextures {
    public static Texture AP_BADGE;
    public static Texture AP_ICON;
    public static Texture AP_CHEST;
    public static Texture PLAYER_PANEL;
    public static Texture TEAM_BAR;
    public static Texture TEAM_SETTINGS_BACKGROUND;

    public static void initialize() {
        AP_BADGE = ImageMaster.loadImage("ArchipelagoMWResources/images/Badge.png");
        AP_ICON = ImageMaster.loadImage("ArchipelagoMWResources/images/ui/APIcon.png");
        AP_CHEST = ImageMaster.loadImage("ArchipelagoMWResources/images/npc/APChest.png");
        PLAYER_PANEL = ImageMaster.loadImage("ArchipelagoMWResources/images/ui/hud/PlayerPanel.png");
        TEAM_BAR = ImageMaster.loadImage("ArchipelagoMWResources/images/ui/hud/TeamColorBar.png");
        TEAM_SETTINGS_BACKGROUND = ImageMaster.loadImage("ArchipelagoMWResources/images/ui/hud/TeamSettingsBackground.png");

    }
}
