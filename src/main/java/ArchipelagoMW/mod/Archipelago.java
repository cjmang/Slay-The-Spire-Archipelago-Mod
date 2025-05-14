package ArchipelagoMW.mod;

import ArchipelagoMW.game.save.APSaveable;
import ArchipelagoMW.game.teams.TeamManager;
import ArchipelagoMW.game.ui.APTextures;
import ArchipelagoMW.game.items.ui.ArchipelagoRewardScreen;
import ArchipelagoMW.game.locations.ui.RewardMenu.BossRelicRewardScreen;
import ArchipelagoMW.game.teams.ui.hud.SideBar;
import ArchipelagoMW.game.items.ui.ArchipelagoIcon;
import ArchipelagoMW.game.save.APRewardSave;
import basemod.BaseMod;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;


@SpireInitializer
public class Archipelago implements
        EditStringsSubscriber,
        PostInitializeSubscriber {

    public static final Logger logger = LogManager.getLogger(Archipelago.class.getName());
    public static final String modID = "ArchipelagoMW-2.0";
    public static final String MODNAME = "Archipelago Multi-World";
    public static final String AUTHOR = "Kono Tyran & Mavelovent & PlatanoBailando";
    public static final String DESCRIPTION = "An Archipelago multiworld mod.";

    public static BossRelicRewardScreen bossRelicRewardScreen;

    // Archipelago Client Varaiables
    public static String address;
    public static String slotName;
    public static String password;

    public static SideBar sideBar;


    public Archipelago() {
        logger.info("Subscribe to BaseMod hooks");
        BaseMod.subscribe(this);
        logger.info("Done subscribing");

        APSettings.loadSettings();
        Runtime.getRuntime().addShutdownHook(new Thread(TeamManager::leaveTeam));
    }

    public static String getModID() {
        return modID;
    }

    @SuppressWarnings("unused")
    public static void initialize() {
        logger.info("========================= Initializing Archipelago Multi-World Version 1.13 =========================");
        new Archipelago();
        logger.info("=========================  Archipelago Multi-World Initialized. =========================");
    }

    public static void setConnectionInfo(String addressField, String slotNameField, String passwordField) {
        address = addressField;
        slotName = slotNameField;
        password = passwordField;
    }

    @Override
    public void receivePostInitialize() {
        logger.info("Loading badge image and mod options");

        //initalize textures
        APTextures.initialize();

        //initialize Mod Menu and settings.
        APSettings.initialize();

        BaseMod.addTopPanelItem(new ArchipelagoIcon());
        //BaseMod.addTopPanelItem(new TestButton());

        Type rewardSaveList = new TypeToken<ArrayList<APRewardSave>>() {
        }.getType();

        BaseMod.addCustomScreen(new ArchipelagoRewardScreen());
        BaseMod.addSaveField("ap_rewards", new APSaveable());
        sideBar = new SideBar();
        BaseMod.removeRelic(RelicLibrary.getRelic("Calling Bell"));
        logger.info("Done loading badge Image and mod options");
    }

    @Override
    public void receiveEditStrings() {
        logger.info("Beginning to edit strings for mod with ID: " + getModID());

        // UIStrings
        BaseMod.loadCustomStringsFile(UIStrings.class,
                getModID() + "Resources/localization/eng/ArchipelagoMW-UI-Strings.json");

        logger.info("Done editing strings");
    }

    public static String makeID(String idText) {
        return getModID() + ":" + idText;
    }
}
