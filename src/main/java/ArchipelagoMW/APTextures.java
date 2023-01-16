package ArchipelagoMW;

import com.badlogic.gdx.graphics.Texture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class APTextures {
    private static final Logger logger = LogManager.getLogger(APTextures.class.getName());

    public static Texture AP_BADGE;
    public static Texture AP_ICON;
    public static Texture AP_CHEST;
    public static Texture PLAYER_PANEL;
    public static Texture TEAM_BAR;

    public static void initialize() {
        AP_BADGE = loadImage("ArchipelagoMWResources/images/Badge.png");
        AP_ICON = loadImage("ArchipelagoMWResources/images/ui/APIcon.png");
        AP_CHEST = loadImage("ArchipelagoMWResources/images/npc/APChest.png");
        PLAYER_PANEL = loadImage("ArchipelagoMWResources/images/ui/hud/PlayerPanel.png");
        TEAM_BAR = loadImage("ArchipelagoMWResources/images/ui/hud/TeamColorBar.png");

    }

    public static Texture loadImage(String imgUrl) {
        return loadImage(imgUrl, true);
    }

    public static Texture loadImage(String imgUrl, boolean linearFiltering) {
        assert imgUrl != null : "DO NOT CALL LOAD IMAGE WITH NULL";

        try {
            Texture texture = new Texture(imgUrl);
            if (linearFiltering) {
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            } else {
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }

            return texture;
        } catch (Exception var3) {
            logger.info("[WARNING] No image at " + imgUrl);
            return null;
        }
    }
}
