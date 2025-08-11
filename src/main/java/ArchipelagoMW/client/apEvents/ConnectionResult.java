package ArchipelagoMW.client.apEvents;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.client.config.SlotData;
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
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.ConnectionResultEvent;

public class ConnectionResult {

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
                msg = "Unknown Error: " + event.getResult().name();
        }
        ConnectionPanel.connectionResultText = msg;

        if (event.getResult() != io.github.archipelagomw.network.ConnectionResult.Success)
            return;

        if (CardCrawlGame.mode != CardCrawlGame.GameMode.CHAR_SELECT)
            return;

        SlotData slotData = event.getSlotData(SlotData.class);
        if(slotData.modVersion != SlotData.EXPECTED_MOD_VERSION)
        {
            ConnectionPanel.connectionResultText = "Mod is not compatible with generated world; generated world version: " +
                    slotData.modVersion + " expected version " + SlotData.EXPECTED_MOD_VERSION;
            return;
        }
        APContext ctx = APContext.getContext();
        ctx.getClient().setSlotData(slotData);
        Archipelago.logger.info(slotData.characters.toString());
        ctx.getCharacterManager().initialize(slotData.characters);
        Archipelago.logger.info("slot data parsed");
        ctx.getSaveManager().loadSaves();
        if(slotData.chattyMC != 0)
        {
            ctx.getClient().getEventManager().registerListener(new APClient.OnJSONMessage());
        }

        ArchipelagoMainMenuButton.archipelagoPreGameScreen.screen = ArchipelagoPreGameScreen.APScreen.charSelect;
    }

    // TODO: Move this, it doesn't make sense here anymore
    public static void start() {

        Archipelago.logger.info("about to parse slot data");
        try {
            APContext ctx = APContext.getContext();
            CharacterConfig config = ctx.getCharacterManager().getCurrentCharacterConfig();
            ctx.getItemTracker().initialize(APContext.getContext().getItemManager().getReceivedItemIDs());
            CardCrawlGame.chosenCharacter = ctx.getCharacterManager().getCurrentCharacter().chosenClass;

            Archipelago.logger.info("character: {}", config.officialName);
            Archipelago.logger.info("heart: " + config.finalAct);
            Archipelago.logger.info("seed: " + config.seed);
            Archipelago.logger.info("ascension: " + config.ascension);
            Archipelago.logger.info("character offset: {}", config.charOffset);


            if (Loader.isModLoaded("downfall"))
                EvilModeCharacterSelect.evilMode = config.downfall;
            APClient client = APContext.getContext().getClient();
            if (client.getSlotData().deathLink > 0) {
                client.setDeathLinkEnabled(true);
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
