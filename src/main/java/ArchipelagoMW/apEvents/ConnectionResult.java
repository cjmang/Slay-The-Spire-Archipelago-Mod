package ArchipelagoMW.apEvents;

import ArchipelagoMW.APClient;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.LocationTracker;
import ArchipelagoMW.SlotData;
import ArchipelagoMW.patches.SavePatch;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import ArchipelagoMW.util.DeathLinkHelper;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
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
                ConnectionPanel.showPassword = true;
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
                slotData.character = "The Ironclad";
                break;
            case "1":
                slotData.character = "The Silent";
                break;
            case "2":
                slotData.character = "The Defect";
                break;
            case "3":
                slotData.character = "The Watcher";
                break;
            case "4":
                slotData.character = "The Hermit";
                break;
            case "5":
                slotData.character = "The Slime Boss";
                break;
            case "6":
                slotData.character = "The Guardian";
                break;
            case "7":
                slotData.character = "The Hexaghost";
                break;
            case "8":
                slotData.character = "The Champ";
                break;
            case "9":
                slotData.character = "The Gremlins";
                break;
            case "10":
                slotData.character = "The Automaton";
                break;
            case "11":
                slotData.character = "The Snecko";
                break;
            case "12":
                character = CardCrawlGame.characterManager.getRandomCharacter(new Random());
        }

        for (AbstractPlayer ch : CardCrawlGame.characterManager.getAllCharacters()) {
            if (ch.title.equalsIgnoreCase(slotData.character)) {
                character = ch;
                break;
            }
        }
        LocationTracker.reset();
        ArchipelagoRewardScreen.rewards.clear();
        ArchipelagoRewardScreen.receivedItemsIndex = 0;
        LocationTracker.scoutAllLocations();
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

            DeathLinkHelper.update.death = false;


            CardCrawlGame.mainMenuScreen.isFadingOut = true;
            CardCrawlGame.mainMenuScreen.fadeOutMusic();

            Settings.isFinalActAvailable = (APClient.slotData.finalAct == 1);
            SeedHelper.setSeed(APClient.slotData.seed);

            AbstractDungeon.isAscensionMode = (APClient.slotData.ascension > 0);
            AbstractDungeon.ascensionLevel = APClient.slotData.ascension;

            AbstractDungeon.generateSeeds();
            Settings.seedSet = true;

            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
