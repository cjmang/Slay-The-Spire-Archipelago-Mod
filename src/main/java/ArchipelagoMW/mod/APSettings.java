package ArchipelagoMW.mod;

import ArchipelagoMW.game.ui.APTextures;
import basemod.BaseMod;
import basemod.ModLabel;
import basemod.ModPanel;
import basemod.ModToggleButton;
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


    public enum FilterType {
        TEAM, RECENT, ALL;
    }

    private static final Properties defaultSettings = new Properties();
    public static SpireConfig config;
    private static ModToggleButton soundToggle;
    private static ModToggleButton teamToggle;
    private static ModToggleButton recentToggle;
    private static ModToggleButton allToggle;

    public static void loadSettings() {
        defaultSettings.setProperty(PLAYER_FILTER_KEY, "RECENT");
        defaultSettings.setProperty(CONNECT_SCREEN_ADDRESS_KEY, "Archipelago.gg");
        defaultSettings.setProperty(CONNECT_SCREEN_SLOT_KEY, "");
        defaultSettings.setProperty(RECEIVE_ITEM_SOUND, "true");

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

    public static void initialize() {
        // Create the Mod Menu
        ModPanel settingsPanel = new ModPanel();

        float configYpos = 900 * Settings.scale;
        float configXPos = 450 * Settings.scale;
        float configStep = 40 * Settings.scale;

        ModLabel sideBarFilterLabel = new ModLabel("Side Bar Filter:", configXPos, configYpos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(sideBarFilterLabel);

        teamToggle = new ModToggleButton(configXPos + 225f * Settings.scale, configYpos - 5f * Settings.scale, APSettings.playerFilter == FilterType.TEAM, true, settingsPanel, APSettings::toggleTeam);
        ModLabel teamLabel = new ModLabel("Team", configXPos + 275f * Settings.scale, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(teamToggle);
        settingsPanel.addUIElement(teamLabel);
        configYpos -= configStep;

        recentToggle = new ModToggleButton(configXPos + 225f * Settings.scale, configYpos - 5f * Settings.scale, APSettings.playerFilter == FilterType.RECENT, true, settingsPanel, APSettings::toggleRecent);
        ModLabel recentLabel = new ModLabel("Recent", configXPos + 275f * Settings.scale, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(recentToggle);
        settingsPanel.addUIElement(recentLabel);
        configYpos -= configStep;


        allToggle = new ModToggleButton(configXPos + 225f * Settings.scale, configYpos - 5f * Settings.scale, APSettings.playerFilter == FilterType.ALL, true, settingsPanel, APSettings::toggleAll);
        ModLabel allLabel = new ModLabel("All", configXPos + 275f * Settings.scale, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(allToggle);
        settingsPanel.addUIElement(allLabel);
        configYpos -= configStep;

        soundToggle = new ModToggleButton(configXPos + 225f * Settings.scale, configYpos - 5f * Settings.scale, APSettings.config.getBool(RECEIVE_ITEM_SOUND), true, settingsPanel, APSettings::toggleSound);
        ModLabel soundLabel = new ModLabel("Enable Receive Sound", configXPos + 275f * Settings.scale, configYpos, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(soundToggle);
        settingsPanel.addUIElement(soundLabel);
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
