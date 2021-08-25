package ArchipelagoMW;

import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.relics.deprecated.DerpRock;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import gg.archipelago.APClient.Print.APPrint;
import gg.archipelago.APClient.events.ConnectionAttemptEvent;
import gg.archipelago.APClient.events.ConnectionResultEvent;
import gg.archipelago.APClient.network.BouncedPacket;
import gg.archipelago.APClient.network.ConnectionResult;
import gg.archipelago.APClient.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class APClient extends gg.archipelago.APClient.APClient {

    public static final Logger logger = LogManager.getLogger(APClient.class.getName());

    public static APClient apClient;

    public static void newConnection(String address, String slotName) {
        if(apClient != null) {
            apClient.close();
        }
        apClient = new APClient("", 0);
        apClient.setName(slotName);
        apClient.connect(address);
    }

    private APClient(String saveID, int slotID) {
        super(saveID, slotID);
        this.setGame("Slay the Spire");
    }

    @Override
    public void onConnectResult(ConnectionResultEvent connectionResultEvent) {
        String msg = "Connecting to AP...";
        switch ( connectionResultEvent.getResult()) {
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

        if(connectionResultEvent.getResult() != ConnectionResult.Success)
            return;

        if(CardCrawlGame.mode != CardCrawlGame.GameMode.CHAR_SELECT)
            return;

        logger.info("about to parse slot data");
        try {
            SlotData data = connectionResultEvent.getSlotData(SlotData.class);
            logger.info("slot data parsed");
                AbstractPlayer.PlayerClass character;
            switch(data.character) {
                case 1:
                    character = AbstractPlayer.PlayerClass.THE_SILENT;
                    break;
                case 2:
                    character = AbstractPlayer.PlayerClass.DEFECT;
                    break;
                case 3:
                    character = AbstractPlayer.PlayerClass.WATCHER;
                    break;
                case 0:
                default:
                    character = AbstractPlayer.PlayerClass.IRONCLAD;
            }

            logger.info("character: "+character.name());
            logger.info("heart: "+data.heartRun);
            logger.info("seed: "+data.seed);
            logger.info("ascension: "+data.ascension);
            /*
            AbstractDungeon.player = CardCrawlGame.characterManager.recreateCharacter(character);
            for (AbstractRelic relic : AbstractDungeon.player.relics) {
                relic.updateDescription(AbstractDungeon.player.chosenClass);
                relic.onEquip();
            }

            for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
                if(card.rarity != AbstractCard.CardRarity.BASIC) {
                    CardHelper.obtain(card.cardID, card.rarity, card.color);
                }
            }*/

            CardCrawlGame.chosenCharacter = character;
            CardCrawlGame.mainMenuScreen.isFadingOut = true;
            CardCrawlGame.mainMenuScreen.fadeOutMusic();

            Settings.isFinalActAvailable = (data.heartRun >= 1);
            SeedHelper.setSeed(data.seed);

            AbstractDungeon.isAscensionMode = (data.ascension > 0);
            AbstractDungeon.ascensionLevel = data.ascension;

            AbstractDungeon.generateSeeds();
            Settings.seedSet = true;

            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;

            LocationTracker.reset();
            ArchipelagoRewardScreen.rewards.clear();
            ArchipelagoRewardScreen.index = 0;

        }
        catch (Exception e) {
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
        ConnectionPanel.connectionResultText = "Server Error [] NL " + e.getMessage();
    }

    @Override
    public void onClose(String message, int i) {
        ConnectionPanel.connectionResultText = "Connection Closed [] NL " + message;
    }

    @Override
    public void onReceiveItem(int itemID, String location, String player) {
        //ignore received items that happen while we are not yet loaded
        if (AbstractDungeon.isPlayerInDungeon())
            ArchipelagoRewardScreen.addReward(itemID, location,player);
    }

    @Override
    public void onAttemptConnection(ConnectionAttemptEvent connectionAttemptEvent) {

    }
}
