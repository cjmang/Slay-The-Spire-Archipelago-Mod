package ArchipelagoMW;

import ArchipelagoMW.ui.RewardMenu.BossRelicRewardScreen;
import ArchipelagoMW.ui.topPannel.ArchipelagoIcon;
import basemod.BaseMod;
import basemod.ModLabel;
import basemod.ModPanel;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;


@SpireInitializer
public class ArchipelagoMW implements
        EditStringsSubscriber,
        PostInitializeSubscriber {

    public static final Logger logger = LogManager.getLogger(ArchipelagoMW.class.getName());
    private static final String modID = "ArchipelagoMW";

    private static final String MODNAME = "Archipelago Multi-World";
    private static final String AUTHOR = "Kono Tyran & Mavelovent";
    private static final String DESCRIPTION = "An Archipelago multiworld mod.";

    public static final String BADGE_IMAGE = "ArchipelagoMWResources/images/Badge.png";
    public static BossRelicRewardScreen bossRelicRewardScreen;

    // Archipelago Client Varaiables
    public static String address;
    public static String slotName;
    public static String password;


    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/images/ui/" + resourcePath;
    }

    public ArchipelagoMW() {
        logger.info("Subscribe to BaseMod hooks");

        BaseMod.subscribe(this);

        logger.info("Done subscribing");
    }

    public static String getModID() {
        return modID;
    }

    public static void initialize() {
        logger.info("========================= Initializing Archipelago Multi-World. =========================");
        new ArchipelagoMW();
        logger.info("========================= Archipelago Multi-World Initialized. =========================");
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


        // Create the Mod Menu
        ModPanel settingsPanel = new ModPanel();

        int configPos = 800;
        int configStep = 40;
        configPos -= 90;
        ModLabel validLabel = new ModLabel("Valid Characters:", 350.0F, (float) configPos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(validLabel);

        String[] titles = BaseMod.getModdedCharacters().stream().map(p -> p.title).toArray(String[]::new);

        int chunkSize = 4;
        int remainder = titles.length % chunkSize;
        int chunks = titles.length / chunkSize + (remainder > 1 ? 1 : 0);
        for (int i = 0; i <= chunks; i++) {
            configPos -= configStep;
            String[] line;
            if (i == chunks && remainder > 0) {
                line = Arrays.copyOfRange(titles, chunks * chunkSize, titles.length);
            } else {
                line = Arrays.copyOfRange(titles, i * chunkSize, i * chunkSize + chunkSize);
            }
            ModLabel lineLabel = new ModLabel("\"" + String.join("\", \"", line) + "\"", 350.0F, (float) configPos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (label) -> {
            });
            settingsPanel.addUIElement(lineLabel);
        }


        BaseMod.registerModBadge(APTextures.AP_BADGE, MODNAME, AUTHOR, DESCRIPTION, settingsPanel);
        BaseMod.addTopPanelItem(new ArchipelagoIcon());


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
