package ArchipelagoMW.apEvents;

import ArchipelagoMW.*;
import ArchipelagoMW.save.SaveManager;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ArchipelagoPreGameScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import ArchipelagoMW.ui.mainMenu.ArchipelagoMainMenuButton;
import ArchipelagoMW.util.DeathLinkHelper;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import downfall.patches.EvilModeCharacterSelect;
import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.ConnectionResultEvent;
import dev.koifysh.archipelago.helper.DeathLink;

public class ConnectionResult {
    /**
     * character offset for locations
     *
     */
//    public static AbstractPlayer character = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.IRONCLAD);
//    public static Set<String> availableAPChars = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    @ArchipelagoEventListener
    public void onConnectionResult(ConnectionResultEvent event) {
        ArchipelagoRewardScreen.rewardsQueued = 0;
        String msg;
        switch (event.getResult()) {
            case SlotAlreadyTaken:
                msg = "Slot already in use.";
                break;
            case Success:
                msg = "Connected Starting Game.";
                break;
            case InvalidSlot:
                msg = "Invalid Slot Name. Please make sure you typed it correctly.";
                break;
            case InvalidPassword:
                msg = "Invalid Password";
                break;
            case IncompatibleVersion:
                msg = "Server Rejected our connection due to an incompatible communication protocol.";
                break;
            default:
                msg = "Unknown Error";
        }
        ConnectionPanel.connectionResultText = msg;

        if (event.getResult() != dev.koifysh.archipelago.network.ConnectionResult.Success)
            return;

        if (CardCrawlGame.mode != CardCrawlGame.GameMode.CHAR_SELECT)
            return;

        APClient.slotData = event.getSlotData(SlotData.class);
        SlotData slotData = APClient.slotData;
        Archipelago.logger.info(slotData.characters.toString());
        APClient.charManager.initialize(slotData.characters);
        Archipelago.logger.info("slot data parsed");
        SaveManager.getInstance().loadSaves();


//        character = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.IRONCLAD);
//        switch (slotData.character) {
//            case "0":
//                slotData.character = AbstractPlayer.PlayerClass.IRONCLAD.name();
//                break;
//            case "1":
//                slotData.character = AbstractPlayer.PlayerClass.THE_SILENT.name();
//                break;
//            case "2":
//                slotData.character = AbstractPlayer.PlayerClass.DEFECT.name();
//                break;
//            case "3":
//                slotData.character = AbstractPlayer.PlayerClass.WATCHER.name();
//                break;
//            case "4":
//                slotData.character = "HERMIT";
//                break;
//            case "5":
//                slotData.character = "SLIMEBOUND";
//                break;
//            case "6":
//                slotData.character = "GUARDIAN";
//                break;
//            case "7":
//                slotData.character = "THE_SPIRIT";
//                break;
//            case "8":
//                slotData.character = "THE_CHAMP";
//                break;
//            case "9":
//                slotData.character = "GREMLIN";
//                break;
//            case "10":
//                slotData.character = "THE_AUTOMATON";
//                break;
//            case "11":
//                slotData.character = "THE_SNECKO";
//                break;
//            case "12":
//                // TODO: Spire Take the Wheel will not work with this change; will need
//                // some additional help
//                character = CardCrawlGame.characterManager.getRandomCharacter(new Random());
//        }
//
//        availableAPChars.add(slotData.character);
//
//        // TODO: update once we can support more than one toon
//        for (AbstractPlayer ch : CardCrawlGame.characterManager.getAllCharacters()) {
//            if (ch.chosenClass.name().equalsIgnoreCase(slotData.character)) {
//                character = ch;
//                break;
//            }
//        }

//        LocationTracker.initialize(slotData.character_offset);
//        ArchipelagoRewardScreen.rewards.clear();
//        ArchipelagoRewardScreen.receivedItemsIndex = 0;
//        ArchipelagoRewardScreen.apRareReward = false;
//        ArchipelagoRewardScreen.apReward= false;
//        ArchipelagoRewardScreen.APScreen= false;
//        LocationTracker.scoutAllLocations();
//        TeamManager.initialLoad();
//        PlayerManager.initialLoad();
//        DataStorageGet.loadRequestId = APClient.apClient.dataStorageGet(Arrays.asList(SavePatch.AP_SAVE_STRING, SavePatch.AP_SAVE_CHAR));
//        APClient.apClient.asyncDSGet(Arrays.asList(SavePatch.AP_SAVE_STRING, SavePatch.AP_SAVE_CHAR),
//                (e) -> {
//                    if (e.getString(SavePatch.AP_SAVE_STRING) != null && !e.getString(SavePatch.AP_SAVE_STRING).isEmpty()) {
//                        SavePatch.compressedSave = e.getString(SavePatch.AP_SAVE_STRING);
//                        SavePatch.savedChar = e.getString(SavePatch.AP_SAVE_CHAR);
//                        APClient.logger.info("Got saved character {}", SavePatch.savedChar);
//                        ArchipelagoMainMenuButton.archipelagoPreGameScreen.connectionPanel.resumeSave.show();
//                    } else {
//                        ArchipelagoMainMenuButton.archipelagoPreGameScreen.screen = ArchipelagoPreGameScreen.APScreen.charSelect;
//                    }
//                });

        ArchipelagoMainMenuButton.archipelagoPreGameScreen.screen = ArchipelagoPreGameScreen.APScreen.charSelect;
    }

    public static void start() {

        Archipelago.logger.info("about to parse slot data");
        try {

            CharacterConfig config = APClient.charManager.getCurrentCharacterConfig();
            CardCrawlGame.chosenCharacter = APClient.charManager.getCurrentCharacter().chosenClass;

            Archipelago.logger.info("character: {}", config.officialName);
            Archipelago.logger.info("heart: " + config.finalAct);
            Archipelago.logger.info("seed: " + config.seed);
            Archipelago.logger.info("ascension: " + config.ascension);
            Archipelago.logger.info("character offset: {}", config.charOffset);


            if (Loader.isModLoaded("downfall"))
                EvilModeCharacterSelect.evilMode = config.downfall;

            if (APClient.slotData.deathLink > 0) {
                DeathLink.setDeathLinkEnabled(true);
            }

            DeathLinkHelper.update.sendDeath = false;

            Settings.isFinalActAvailable = config.finalAct;
            SeedHelper.setSeed(config.seed);

            AbstractDungeon.isAscensionMode = config.ascension > 0;
            AbstractDungeon.ascensionLevel = config.ascension;

            AbstractDungeon.generateSeeds();
            Settings.seedSet = true;

            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
            CardCrawlGame.mainMenuScreen.isFadingOut = true;
            CardCrawlGame.mainMenuScreen.fadeOutMusic();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
