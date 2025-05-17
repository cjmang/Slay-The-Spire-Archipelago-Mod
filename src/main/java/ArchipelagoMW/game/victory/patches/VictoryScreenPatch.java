package ArchipelagoMW.game.victory.patches;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.game.locations.LocationTracker;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import com.megacrit.cardcrawl.ui.buttons.ReturnToMenuButton;
import dev.koifysh.archipelago.ClientStatus;
import dev.koifysh.archipelago.events.SetReplyEvent;
import dev.koifysh.archipelago.network.client.SetPacket;
import javassist.CtBehavior;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class VictoryScreenPatch {


    @SpirePatch(clz = DeathScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class deathPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            // check if this is a post-act3 death which the "isVictory" flag tracks
            // if we should be finishing final act, this is not a victory regardless of act3 boss's death
            if (!GameOverScreen.isVictory || APContext.getContext().getCharacterManager().getCurrentCharacterConfig().finalAct)
                return;

            victoryForCurrentCharacter();
        }
    }

    @SpirePatch(clz = VictoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class VictoryPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            // victory screen called when this is a final act victory.
            if (!GameOverScreen.isVictory)
                return;

            victoryForCurrentCharacter();
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "update")
    public static class ReturnClicked {

        @SpireInsertPatch(locator = locator.class)
        public static void Clicked(DeathScreen __instance, ReturnToMenuButton ___returnButton) {
            if (___returnButton.hb.clicked || ___returnButton.show && CInputActionSet.select.isJustPressed()) {
                APContext.getContext().getClient().disconnect();
            }
        }

        private static class locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.MethodCallMatcher(ReturnToMenuButton.class, "update");
                return new int[] {LineFinder.findInOrder(ctBehavior, match)[0]+1};
            }
        }
    }

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

        private String createVictoryKey(APClient client)
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

                List<String> charsWonWith = new ArrayList<>();
                charsWonWith.add(character.chosenClass.name());
                for (CharacterConfig config : characterManager.getUnrecognizedCharacters()) {
                    charsWonWith.add(config.officialName);
                }

                SetPacket packet = new SetPacket(victoryKey, charsWonWith);
                packet.defaultValue = new ArrayList<>();
                packet.addDataStorageOperation(SetPacket.Operation.DEFAULT, null);
                packet.addDataStorageOperation(SetPacket.Operation.UPDATE, charsWonWith);

                Future<SetReplyEvent> reply = client.dataStorageSetFuture(packet);
                SetReplyEvent event = reply.get();
                if (event == null) {
                    APClient.logger.info("Couldn't communicate with server for victory; save file will be retained");
                    return;
                }

                List<String> eventChars = (List<String>) event.value;

                APClient.logger.info("Won with the following characters: {}", eventChars);
                if (eventChars.size() >= characterManager.getCharacters().size()) {
                    client.setGameState(ClientStatus.CLIENT_GOAL);
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
