package ArchipelagoMW;

import ArchipelagoMW.patches.NeowPatch;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import ArchipelagoMW.ui.topPannel.ArchipelagoIcon;
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
import gg.archipelago.APClient.ItemFlags;
import gg.archipelago.APClient.Print.APPrint;
import gg.archipelago.APClient.events.ConnectionAttemptEvent;
import gg.archipelago.APClient.events.ConnectionResultEvent;
import gg.archipelago.APClient.helper.DeathLink;
import gg.archipelago.APClient.network.BouncedPacket;
import gg.archipelago.APClient.network.ConnectionResult;
import gg.archipelago.APClient.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;

public class APClient extends gg.archipelago.APClient.APClient {

    public static final Logger logger = LogManager.getLogger(APClient.class.getName());

    public static APClient apClient;

    public static SlotData slotData;

    public static void newConnection(String address, String slotName, String password) {
        if (apClient != null) {
            apClient.close();
        }
        apClient = new APClient();
        apClient.setPassword(password);
        apClient.setName(slotName);
        apClient.setItemsHandlingFlags(ItemFlags.SEND_ITEMS + ItemFlags.SEND_OWN_ITEMS + ItemFlags.SEND_STARTING_INVENTORY);
        try {
            apClient.connect(address);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private APClient() {
        super();
        this.setGame("Slay the Spire");
    }

    @Override
    public void onConnectResult(ConnectionResultEvent connectionResultEvent) {
        ArchipelagoRewardScreen.rewardsQueued = 0;
        String msg = "Connecting to AP...";
        switch (connectionResultEvent.getResult()) {
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

        if (connectionResultEvent.getResult() != ConnectionResult.Success)
            return;

        if (CardCrawlGame.mode != CardCrawlGame.GameMode.CHAR_SELECT)
            return;

        logger.info("about to parse slot data");
        try {
            slotData = connectionResultEvent.getSlotData(SlotData.class);
            logger.info("slot data parsed");

            AbstractPlayer character = CardCrawlGame.characterManager.getCharacter(AbstractPlayer.PlayerClass.IRONCLAD);
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
            logger.info("character: " + character.name);
            logger.info("heart: " + slotData.finalAct);
            logger.info("seed: " + slotData.seed);
            logger.info("ascension: " + slotData.ascension);

            CardCrawlGame.chosenCharacter = character.chosenClass;
            if (Loader.isModLoaded("downfall"))
                EvilModeCharacterSelect.evilMode = slotData.downfall == 1;

            if(slotData.deathLink > 0) {
                DeathLink.setDeathLinkEnabled(true);
                getEventManager().registerListener(new DeathLinkHelper(slotData.deathLink));
            }

            DeathLinkHelper.update.death = false;

            CardCrawlGame.mainMenuScreen.isFadingOut = true;
            CardCrawlGame.mainMenuScreen.fadeOutMusic();

            Settings.isFinalActAvailable = (slotData.finalAct == 1);
            SeedHelper.setSeed(slotData.seed);

            AbstractDungeon.isAscensionMode = (slotData.ascension > 0);
            AbstractDungeon.ascensionLevel = slotData.ascension;

            AbstractDungeon.generateSeeds();
            Settings.seedSet = true;

            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;

            LocationTracker.reset();
            ArchipelagoRewardScreen.rewards.clear();
            ArchipelagoRewardScreen.index = 0;

            LocationTracker.scoutAllLocations();

            Set<Long> checkedLocations = getLocationManager().getCheckedLocations();
            NeowPatch.act2portalAvailable = checkedLocations.contains(22001L);
            NeowPatch.act3portalAvailable = checkedLocations.contains(22002L);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onJoinRoom() {

    }

    @Override
    public void onPrint(String s) {

    }

    @Override
    public void onPrintJson(APPrint apPrint, String s, int i, NetworkItem networkItem) {

    }

    @Override
    public void onBounced(BouncedPacket bouncedPacket) {

    }

    @Override
    public void onError(Exception e) {
        ConnectionPanel.connectionResultText = "Server Error NL " + e.getMessage();
    }

    @Override
    public void onClose(String message, int i) {
        ConnectionPanel.connectionResultText = "Connection Closed NL " + message;
    }

    @Override
    public void onReceiveItem(NetworkItem networkItem) {
        //ignore received items that happen while we are not yet loaded
        logger.info("NetworkItem received: " + networkItem.itemName);
        ArchipelagoRewardScreen.rewardsQueued += 1;
        if (CardCrawlGame.isInARun()) {
            try {
                logger.info("Adding item to player in room: " + AbstractDungeon.getCurrRoom());
                ArchipelagoIcon.addPendingReward(networkItem);
            } catch (NullPointerException e) {
                logger.info("Player was unable to receive item for now");
            }
        }
    }

    @Override
    public void onLocationInfo(ArrayList<NetworkItem> networkItems) {
        LocationTracker.addToScoutedLocations(networkItems);
    }

    @Override
    public void onLocationChecked(long l) {

    }

    @Override
    public void onAttemptConnection(ConnectionAttemptEvent connectionAttemptEvent) {

    }
}
