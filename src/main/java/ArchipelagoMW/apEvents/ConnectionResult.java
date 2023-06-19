package ArchipelagoMW.apEvents;

import ArchipelagoMW.APClient;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.LocationTracker;
import ArchipelagoMW.SlotData;
import ArchipelagoMW.patches.SavePatch;
import ArchipelagoMW.teams.PlayerManager;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import ArchipelagoMW.ui.hud.SideBar;
import ArchipelagoMW.util.DeathLinkHelper;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import downfall.patches.EvilModeCharacterSelect;
import gg.archipelago.client.events.ArchipelagoEventListener;
import gg.archipelago.client.events.ConnectionResultEvent;
import gg.archipelago.client.helper.DeathLink;
import gg.archipelago.client.parts.Version;

import java.util.Collections;

public class ConnectionResult {

    public static AbstractPlayer character = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.IRONCLAD);

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

        if (event.getResult() != gg.archipelago.client.network.ConnectionResult.Success)
            return;

        if (CardCrawlGame.mode != CardCrawlGame.GameMode.CHAR_SELECT)
            return;

        APClient.slotData = event.getSlotData(SlotData.class);


        SlotData slotData = APClient.slotData;
        Archipelago.logger.info("slot data parsed");


        character = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.IRONCLAD);
        switch (slotData.character) {
            case "0":
                slotData.character = AbstractPlayer.PlayerClass.IRONCLAD.name();
                break;
            case "1":
                slotData.character = AbstractPlayer.PlayerClass.THE_SILENT.name();
                break;
            case "2":
                slotData.character = AbstractPlayer.PlayerClass.DEFECT.name();
                break;
            case "3":
                slotData.character = AbstractPlayer.PlayerClass.WATCHER.name();
                break;
            case "4":
                slotData.character = "HERMIT";
                break;
            case "5":
                slotData.character = "SLIMEBOUND";
                break;
            case "6":
                slotData.character = "GUARDIAN";
                break;
            case "7":
                slotData.character = "THE_SPIRIT";
                break;
            case "8":
                slotData.character = "THE_CHAMP";
                break;
            case "9":
                slotData.character = "GREMLIN";
                break;
            case "10":
                slotData.character = "THE_AUTOMATON";
                break;
            case "11":
                slotData.character = "THE_SNECKO";
                break;
            case "12":
                character = CardCrawlGame.characterManager.getRandomCharacter(new Random());
        }

        for (AbstractPlayer ch : CardCrawlGame.characterManager.getAllCharacters()) {
            if (ch.chosenClass.name().equalsIgnoreCase(slotData.character)) {
                character = ch;
                break;
            }
        }
        LocationTracker.reset();
        ArchipelagoRewardScreen.rewards.clear();
        ArchipelagoRewardScreen.receivedItemsIndex = 0;
        ArchipelagoRewardScreen.apRareReward = false;
        ArchipelagoRewardScreen.apReward= false;
        ArchipelagoRewardScreen.APScreen= false;
        LocationTracker.scoutAllLocations();
        TeamManager.initialLoad();
        PlayerManager.initialLoad();
        DataStorageGet.loadRequestId = APClient.apClient.dataStorageGet(Collections.singleton(SavePatch.AP_SAVE_STRING));
    }

    public static void start() {

        Archipelago.logger.info("about to parse slot data");
        try {

            Archipelago.logger.info("character: " + character.name);
            Archipelago.logger.info("heart: " + APClient.slotData.finalAct);
            Archipelago.logger.info("seed: " + APClient.slotData.seed);
            Archipelago.logger.info("ascension: " + APClient.slotData.ascension);

            CardCrawlGame.chosenCharacter = character.chosenClass;
            if (Loader.isModLoaded("downfall"))
                EvilModeCharacterSelect.evilMode = APClient.slotData.downfall == 1;

            if (APClient.slotData.deathLink > 0) {
                DeathLink.setDeathLinkEnabled(true);
            }

            DeathLinkHelper.update.sendDeath = false;

            Settings.isFinalActAvailable = (APClient.slotData.finalAct == 1);
            SeedHelper.setSeed(APClient.slotData.seed);

            AbstractDungeon.isAscensionMode = (APClient.slotData.ascension > 0);
            AbstractDungeon.ascensionLevel = APClient.slotData.ascension;

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
