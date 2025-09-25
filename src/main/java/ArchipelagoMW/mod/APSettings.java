package ArchipelagoMW.mod;

import ArchipelagoMW.game.ui.APTextures;
import basemod.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class APSettings {

    public static FilterType playerFilter;
    public static String address;
    public static String slot;
    public static String password;
    private static final String PLAYER_FILTER_KEY = "PlayerFilter";
    private static final String CONNECT_SCREEN_ADDRESS_KEY = "ConnectAddress";
    private static final String CONNECT_SCREEN_SLOT_KEY = "ConnectSlot";
    private static final String RECEIVE_ITEM_SOUND = "makeReceiveNoise";
    private static final String NO_COLOR_WORDS = "noColorWords";


    public enum FilterType {
        TEAM, RECENT, ALL;
    }

    private static final Properties defaultSettings = new Properties();
    public static SpireConfig config;
    private static ModToggleButton soundToggle;
    private static ModToggleButton teamToggle;
    private static ModToggleButton recentToggle;
    private static ModToggleButton allToggle;
    private static ModToggleButton colorToggle;

    public static void loadSettings() {
        defaultSettings.setProperty(PLAYER_FILTER_KEY, "RECENT");
        defaultSettings.setProperty(CONNECT_SCREEN_ADDRESS_KEY, "Archipelago.gg");
        defaultSettings.setProperty(CONNECT_SCREEN_SLOT_KEY, "");
        defaultSettings.setProperty(RECEIVE_ITEM_SOUND, "true");
        defaultSettings.setProperty(NO_COLOR_WORDS, "false");

        try {
            config = new SpireConfig(Archipelago.getModID(), "archipelagoConfig", defaultSettings);
            playerFilter = FilterType.valueOf(config.getString(PLAYER_FILTER_KEY));
            address = config.getString(CONNECT_SCREEN_ADDRESS_KEY);
            slot = config.getString(CONNECT_SCREEN_SLOT_KEY);
            password = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSoundEnabled()
    {
        return config.getBool(RECEIVE_ITEM_SOUND);
    }

    public static boolean hideColorWords()
    {
        return config.getBool(NO_COLOR_WORDS);
    }

    public static void initialize() {
        // Create the Mod Menu
        ModPanel settingsPanel = new ModPanel();

        float configYpos = 700f;
        float configXPos = 450f;
        float configStep = 40f;

        ModLabel sideBarFilterLabel = new ModLabel("Side Bar Filter:", configXPos, configYpos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(sideBarFilterLabel);

        teamToggle = new ModToggleButton(configXPos + 225f, configYpos - 5f, APSettings.playerFilter == FilterType.TEAM, true, settingsPanel, APSettings::toggleTeam);
        ModLabel teamLabel = new ModLabel("Team", configXPos + 275f, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(teamToggle);
        settingsPanel.addUIElement(teamLabel);
        configYpos -= configStep;

        recentToggle = new ModToggleButton(configXPos + 225f, configYpos - 5f, APSettings.playerFilter == FilterType.RECENT, true, settingsPanel, APSettings::toggleRecent);
        ModLabel recentLabel = new ModLabel("Recent", configXPos + 275f, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(recentToggle);
        settingsPanel.addUIElement(recentLabel);
        configYpos -= configStep;


        allToggle = new ModToggleButton(configXPos + 225f, configYpos - 5f, APSettings.playerFilter == FilterType.ALL, true, settingsPanel, APSettings::toggleAll);
        ModLabel allLabel = new ModLabel("All", configXPos + 275f, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(allToggle);
        settingsPanel.addUIElement(allLabel);
        configYpos -= configStep;

        soundToggle = new ModToggleButton(configXPos, configYpos - 5f, APSettings.config.getBool(RECEIVE_ITEM_SOUND), true, settingsPanel, APSettings::toggleSound);
        ModLabel soundLabel = new ModLabel("Enable Receive Sound", configXPos + 50f, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(soundToggle);
        settingsPanel.addUIElement(soundLabel);
        configYpos -= configStep;

        colorToggle = new ModToggleButton(configXPos, configYpos - 5f, APSettings.config.getBool(NO_COLOR_WORDS), true, settingsPanel, APSettings::toggleColor);
        ModLabel colorLabel = new ModLabel("Disable AP Color Words", configXPos + 50f, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(colorToggle);
        settingsPanel.addUIElement(colorLabel);
        configYpos -= configStep*2;


        ModLabel validLabel = new ModLabel("Valid Characters:", configXPos, configYpos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(validLabel);


        String[] ids = BaseMod.getModdedCharacters().stream().map(p -> p.chosenClass.name()).toArray(String[]::new);

        int lineLength = 4;
        int remainder = ids.length % lineLength; // 1
        int lines = (int) Math.floor((double) ids.length / lineLength) + (remainder > 0 ? 1 : 0); // 9 / 4 = 2
        for (int i = 0; i < lines; i++) {
            configYpos -= configStep;
            String[] line;
            if (i == lines - 1 && remainder > 0) {
                line = Arrays.copyOfRange(ids, (lines - 1) * lineLength, ids.length);
            } else {
                line = Arrays.copyOfRange(ids, i * lineLength, (i + 1) * lineLength);
            }
            ModLabel lineLabel = new ModLabel("\"" + String.join("\", \"", line) + "\"", configXPos, (float) configYpos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (label) -> {
            });
            settingsPanel.addUIElement(lineLabel);
        }


        BaseMod.registerModBadge(APTextures.AP_BADGE, Archipelago.MODNAME, Archipelago.AUTHOR, Archipelago.DESCRIPTION, settingsPanel);
    }

    private static void toggleTeam(ModToggleButton toggle) {
        APSettings.playerFilter = APSettings.FilterType.TEAM;
        toggle.enabled = true;
        recentToggle.enabled = false;
        allToggle.enabled = false;
        APSettings.saveSettings();
    }

    private static void toggleRecent(ModToggleButton toggle) {
        APSettings.playerFilter = APSettings.FilterType.RECENT;
        toggle.enabled = true;
        teamToggle.enabled = false;
        allToggle.enabled = false;
        APSettings.saveSettings();
    }

    private static void toggleAll(ModToggleButton toggle) {
        APSettings.playerFilter = APSettings.FilterType.ALL;
        toggle.enabled = true;
        recentToggle.enabled = false;
        teamToggle.enabled = false;
        APSettings.saveSettings();
    }

    private static void toggleSound(ModToggleButton toggle) {
        config.setBool(RECEIVE_ITEM_SOUND, !config.getBool(RECEIVE_ITEM_SOUND));
        APSettings.saveSettings();
    }

    private static void toggleColor(ModToggleButton toggle) {
        config.setBool(NO_COLOR_WORDS, !config.getBool(NO_COLOR_WORDS));
        APSettings.saveSettings();
    }

    public static void saveSettings() {
        config.setString(PLAYER_FILTER_KEY, playerFilter.toString());
        config.setString(CONNECT_SCREEN_ADDRESS_KEY, address);
        config.setString(CONNECT_SCREEN_SLOT_KEY, slot);
        try {
            config.save();
        } catch (IOException ignored) {
        }
    }
}
