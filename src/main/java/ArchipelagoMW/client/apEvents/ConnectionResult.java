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
import ArchipelagoMW.saythespire.SayTheSpire;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import downfall.patches.EvilModeCharacterSelect;
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.ConnectionResultEvent;

import java.util.stream.Collectors;

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
        ConnectionPanel.setConnectionResultText(msg);


        if (event.getResult() != io.github.archipelagomw.network.ConnectionResult.Success) {
            return;
        }

        if (CardCrawlGame.mode != CardCrawlGame.GameMode.CHAR_SELECT) {
            return;
        }

        SlotData slotData = event.getSlotData(SlotData.class);
        APClient.logger.info("deathlink: {}", slotData.deathLink);
        if (SlotData.EXPECTED_MOD_VERSIONS.contains(slotData.modVersion)) {
            ConnectionPanel.setConnectionResultText("Mod is not compatible with generated world. Generated world version: " +
                    slotData.modVersion + ". Expected version one of " + SlotData.EXPECTED_MOD_VERSIONS.stream().map(Object::toString).collect(Collectors.joining(" or ")));
            return;
        }
        APContext ctx = APContext.getContext();
        switch (slotData.modVersion) {
            case 2:
                ctx.getCharacterManager().setItemWindow(20L);
                break;
            case 3:
                // fall through
            default:
                ctx.getCharacterManager().setItemWindow(100L);
                break;
        }
        ctx.getClient().setSlotData(slotData);
        Archipelago.logger.info(slotData.characters.toString());
        ctx.getCharacterManager().initialize(slotData.characters);
        ctx.setDeathLinkHelper(new DeathLinkHelper(slotData.deathLink));
        Archipelago.logger.info("slot data parsed");
        ctx.getSaveManager().loadSaves();
        if (slotData.chattyMC != 0) {
            ctx.getClient().getEventManager().registerListener(new APClient.OnJSONMessage());
        }
        ctx.getTrapManager().initialize();

        ArchipelagoMainMenuButton.archipelagoPreGameScreen.screen = ArchipelagoPreGameScreen.APScreen.charSelect;
    }

    // TODO: Move this, it doesn't make sense here anymore
    public static void start() {

        Archipelago.logger.info("about to parse slot data");
        try {
            APContext ctx = APContext.getContext();
            CharacterConfig config = ctx.getCharacterManager().getCurrentCharacterConfig();
            ctx.getItemTracker().initialize(APContext.getContext().getItemManager().getReceivedItemIDs());
            ctx.getTrapManager().initialize();
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

            Settings.isFinalActAvailable = config.finalAct;
            SeedHelper.setSeed(config.seed);
            ctx.getAscensionManager().initializeRunStart();
            AbstractDungeon.isAscensionMode = AbstractDungeon.ascensionLevel > 0;

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
