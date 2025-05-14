package ArchipelagoMW.client.apEvents;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.client.config.SlotData;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.game.save.SaveManager;
import ArchipelagoMW.game.items.ui.ArchipelagoRewardScreen;
import ArchipelagoMW.game.connect.ui.connection.ArchipelagoPreGameScreen;
import ArchipelagoMW.game.connect.ui.connection.ConnectionPanel;
import ArchipelagoMW.game.connect.ui.mainMenu.ArchipelagoMainMenuButton;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.client.util.DeathLinkHelper;
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
        CharacterManager.getInstance().initialize(slotData.characters);
        Archipelago.logger.info("slot data parsed");
        SaveManager.getInstance().loadSaves();

        ArchipelagoMainMenuButton.archipelagoPreGameScreen.screen = ArchipelagoPreGameScreen.APScreen.charSelect;
    }

    // TODO: Move this, it doesn't make sense here anymore
    public static void start() {

        Archipelago.logger.info("about to parse slot data");
        try {

            CharacterConfig config = CharacterManager.getInstance().getCurrentCharacterConfig();
            CharacterManager.getInstance().getItemTracker().initialize(APClient.apClient.getItemManager().getReceivedItemIDs());
            CardCrawlGame.chosenCharacter = CharacterManager.getInstance().getCurrentCharacter().chosenClass;

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
