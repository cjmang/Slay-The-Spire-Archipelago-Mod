package ArchipelagoMW.game.victory.patches;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.saythespire.SayTheSpire;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import com.megacrit.cardcrawl.ui.buttons.ReturnToMenuButton;
import io.github.archipelagomw.ClientStatus;
import io.github.archipelagomw.events.SetReplyEvent;
import io.github.archipelagomw.network.client.SetPacket;
import javassist.CtBehavior;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class VictoryScreenPatch {


    @SpirePatch(clz = DeathScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class deathPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            APClient.logger.info("In death screen; checking if dead or...");
            // check if this is a post-act3 death which the "isVictory" flag tracks
            // if we should be finishing final act, this is not a victory regardless of act3 boss's death
            if (!GameOverScreen.isVictory || APContext.getContext().getCharacterManager().getCurrentCharacterConfig().finalAct) {
                APClient.logger.info("Died");
                return;
            }
            APClient.logger.info("Victory in death screen");
            victoryForCurrentCharacter();
        }
    }

    @SpirePatch(clz = VictoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class VictoryPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            APClient.logger.info("In victory screen; checking if dead or...");
            // victory screen called when this is a final act victory.
            if (!GameOverScreen.isVictory) {
                APClient.logger.info("Died");
                return;
            }
            APClient.logger.info("Victory in victory screen");
            victoryForCurrentCharacter();
        }
    }

    @SpirePatch(cls="duelistmod.ui.gameOver.DuelistVictoryScreen", method=SpirePatch.CONSTRUCTOR, requiredModId="duelistmod" )
    public static class ICantBelieveImDoingThis {
        public static void Postfix() {
            APClient.logger.info("In a stupid victory screen; checking if dead or...");
            if (!GameOverScreen.isVictory) {
                APClient.logger.info("Died");
                return;
            }
            APClient.logger.info("Victory in a stupid victory screen");
            victoryForCurrentCharacter();
        }
    }

    @SpirePatch(cls="duelistmod.ui.gameOver.DuelistDeathScreen", method=SpirePatch.CONSTRUCTOR, requiredModId="duelistmod" )
    public static class ICantBelieveImDoingThisAgain {
        public static void Postfix() {
            APClient.logger.info("In a stupid death screen; checking if dead or...");
            if (!GameOverScreen.isVictory || APContext.getContext().getCharacterManager().getCurrentCharacterConfig().finalAct) {
                APClient.logger.info("Died");
                return;
            }
            APClient.logger.info("Victory in a stupid death screen");
            victoryForCurrentCharacter();
        }
    }

//    @SpirePatch(clz = DeathScreen.class, method = "update")
//    public static class ReturnClicked {
//
//        @SpireInsertPatch(locator = locator.class)
//        public static void Clicked(DeathScreen __instance, ReturnToMenuButton ___returnButton) {
//            if (___returnButton.hb.clicked || ___returnButton.show && CInputActionSet.select.isJustPressed()) {
//                APContext.getContext().getClient().disconnect();
//            }
//        }
//
//        private static class locator extends SpireInsertLocator {
//            @Override
//            public int[] Locate(CtBehavior ctBehavior) throws Exception {
//                Matcher match = new Matcher.MethodCallMatcher(ReturnToMenuButton.class, "update");
//                return new int[] {LineFinder.findInOrder(ctBehavior, match)[0]+1};
//            }
//        }
//    }

    public static void victoryForCurrentCharacter() {
        CharacterManager characterManager = APContext.getContext().getCharacterManager();
        AbstractPlayer character = characterManager.getCurrentCharacter();

        Thread t = new Thread(new VictoryCheck(character));
        t.setDaemon(true);
        t.start();
    }

    public static class VictoryCheck implements Runnable
    {
        private final AbstractPlayer character;

        public VictoryCheck(AbstractPlayer character) {
            this.character = character;
        }

        public static String createVictoryKey(APClient client)
        {
            return "spire_" + "_" + client.getTeam() + "_" + client.getSlot() + "_victory";
        }

        @Override
        public void run() {
            try {
                APContext apContext = APContext.getContext();
                CharacterManager characterManager = apContext.getCharacterManager();
                APClient client = apContext.getClient();
                String victoryKey = createVictoryKey(client);

                Map<String, Boolean> charsWonWith = new HashMap<>();
                charsWonWith.put(character.chosenClass.name(), true);
                for (CharacterConfig config : characterManager.getUnrecognizedCharacters()) {
                    charsWonWith.put(config.officialName, true);
                }

                SetPacket packet = new SetPacket(victoryKey, charsWonWith);
                packet.defaultValue = new HashMap<>();
                packet.addDataStorageOperation(SetPacket.Operation.DEFAULT, packet.defaultValue);
                packet.addDataStorageOperation(SetPacket.Operation.UPDATE, charsWonWith);

                Future<SetReplyEvent> reply = client.dataStorageSetFuture(packet);
                SetReplyEvent event = reply.get();
                if (event == null) {
                    APClient.logger.info("Couldn't communicate with server for victory; save file will be retained");
                    return;
                }

                Map<String, Boolean> eventChars = (Map<String, Boolean>) event.value;

                APClient.logger.info("Won with the following characters: {}", eventChars.keySet());
                int numCharsGoal = apContext.getSlotData().numCharsGoal;
                if ((numCharsGoal == 0 && eventChars.size() >= characterManager.getCharacters().size()) ||
                        (numCharsGoal != 0 && eventChars.size() >= Math.min(numCharsGoal, characterManager.getCharacters().size()))) {
                    client.setGameState(ClientStatus.CLIENT_GOAL);
                    SayTheSpire.sts.output("Archipelago goal completed", true);
                }
                apContext.getLocationTracker().endOfTheRoad();
            }
            catch (ExecutionException | InterruptedException ex)
            {
                APClient.logger.info("Error while doing victory check", ex);
            }
        }
    }
}
