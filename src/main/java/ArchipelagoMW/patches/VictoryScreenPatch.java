package ArchipelagoMW.patches;

import ArchipelagoMW.APClient;
import ArchipelagoMW.LocationTracker;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.rooms.TrueVictoryRoom;
import com.megacrit.cardcrawl.rooms.VictoryRoom;
import gg.archipelago.APClient.ClientStatus;

public class VictoryScreenPatch {

    @SpirePatch(clz = VictoryRoom.class, method = "onPlayerEntry")
    public static class VictoryPatch {

        @SpirePrefixPatch
        public static void Prefix() {
            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
            LocationTracker.forfeit();
        }
    }

    @SpirePatch(clz = TrueVictoryRoom.class, method = "onPlayerEntry")
    public static class TrueVictoryPatch {

        @SpirePrefixPatch
        public static void Prefix() {
            APClient.apClient.setGameState(ClientStatus.CLIENT_GOAL);
            LocationTracker.forfeit();
        }
    }
}
