package ArchipelagoMW.patches;

import ArchipelagoMW.APClient;
import ArchipelagoMW.CharacterConfig;
import ArchipelagoMW.CharacterManager;
import ArchipelagoMW.LocationTracker;
import ArchipelagoMW.apEvents.DataStorageGet;
import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import com.megacrit.cardcrawl.ui.buttons.ReturnToMenuButton;
import dev.koifysh.archipelago.ClientStatus;
import dev.koifysh.archipelago.events.RetrievedEvent;
import dev.koifysh.archipelago.network.client.SetPacket;
import javassist.CtBehavior;

import java.util.*;
import java.util.stream.Collectors;

public class VictoryScreenPatch {

    private static final String VICTORY_KEY = "spire_" + APClient.apClient.getSlot() + "_victory";

    @SpirePatch(clz = DeathScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class deathPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            // check if this is a post-act3 death which the "isVictory" flag tracks
            // if we should be finishing final act, this is not a victory regardless of act3 boss's death
            if (!GameOverScreen.isVictory || APClient.charManager.getCurrentCharacterConfig().finalAct)
                return;

            if(victoryForCurrentCharacter()) {
                LocationTracker.endOfTheRoad();
            }
        }
    }

    @SpirePatch(clz = VictoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class VictoryPatch {

        @SpirePostfixPatch
        public static void Postfix() {
            // victory screen called when this is a final act victory.
            if (!GameOverScreen.isVictory)
                return;

            if(victoryForCurrentCharacter()) {
                LocationTracker.endOfTheRoad();
            }
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "update")
    public static class ReturnClicked {

        @SpireInsertPatch(locator = locator.class)
        public static void Clicked(DeathScreen __instance, ReturnToMenuButton ___returnButton) {
            if (___returnButton.hb.clicked || ___returnButton.show && CInputActionSet.select.isJustPressed()) {
                APClient.apClient.disconnect();
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

    public static boolean victoryForCurrentCharacter() {
        CharacterManager characterManager = APClient.charManager;
        AbstractPlayer character = characterManager.getCurrentCharacter();
        RetrievedEvent event = DataStorageGet.getData(Collections.singleton(VICTORY_KEY));
        if(event == null)
        {
            APClient.logger.info("Couldn't communicate with server for victory; save file will be retained");
            return false;
        }
        Set<String> charsWonWith = new HashSet<>();
        charsWonWith.add(character.chosenClass.name());
        String result = event.getString(VICTORY_KEY);
        if(result != null)
        {
            String[] chars = result.split(":;:");
            charsWonWith.addAll(Arrays.asList(chars));
        }
        else
        {
            for(CharacterConfig config : characterManager.getUnrecognizedCharacters())
            {
                charsWonWith.add(config.officialName);
            }
        }
        APClient.logger.info("Won with the following characters: {}", charsWonWith);
        if(charsWonWith.size() >= characterManager.getCharacters().size())
        {
            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
        }
        SetPacket packet = new SetPacket(VICTORY_KEY, String.join(":;:", charsWonWith));
        // TODO: this API feels awkward, and I dunno the error handling semantics
        APClient.apClient.dataStorageSet(packet);
        return true;
    }
}
