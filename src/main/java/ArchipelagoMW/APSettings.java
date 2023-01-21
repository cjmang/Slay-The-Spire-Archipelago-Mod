package ArchipelagoMW;

import basemod.ModButton;
import basemod.ModLabeledToggleButton;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.util.Properties;

public class APSettings {

    public static FilterType playerFilter;

    public enum FilterType {
        TEAM,RECENT,ALL
    }

    private static final Properties defaultSettings = new Properties();
    public static void loadSettings() {


        defaultSettings.setProperty("playerFilter", "TEAM");
        try {
            SpireConfig config = new SpireConfig(Archipelago.getModID(), "archipelagoConfig", defaultSettings);
            playerFilter = FilterType.valueOf(config.getString("playerFilter"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
