package ArchipelagoMW.ui.hud;

import ArchipelagoMW.APClient;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import gg.archipelago.client.events.*;
import gg.archipelago.client.network.client.SetPacket;
import javassist.CtBehavior;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class InfoUpdater {

    private static final ArrayList<BouncedEvent> queuedEvents = new ArrayList<>();

    @SpirePatch(clz = AbstractPlayer.class, method = "damage")
    public static class damageDetect {
        @SpireInsertPatch(locator = Locator.class)
        public static void insert() {
            sendUpdate();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "currentHealth");
                return new int[]{LineFinder.findAllInOrder(ctBehavior, match)[1] + 1};
            }
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "gainGold")
    public static class gainGold {
        @SpirePostfixPatch
        public static void postfix() {
            sendUpdate();
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "loseGold")
    public static class loseGold {
        @SpirePostfixPatch
        public static void postfix() {
            sendUpdate();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = {SaveFile.class})
    public static class roomTransition {
        @SpirePostfixPatch
        public static void postfix() {
            sendUpdate();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {String.class, AbstractPlayer.class, SaveFile.class})
    public static class LoadConstructor {
        @SpirePostfixPatch
        public static void Postfix() {
            InfoUpdater.initialLoad();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {String.class, String.class, AbstractPlayer.class, ArrayList.class})
    public static class GenerateConstructor {
        @SpirePostfixPatch
        public static void Postfix() {
            InfoUpdater.initialLoad();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "update")
    public static class update {
        @SpirePrefixPatch
        public static void Prefix() {
            update();
        }
    }

    public static int playerListRequest = 0;

    public static ArrayList<RetrievedEvent> retrievedEvent = new ArrayList<>();
    public static ArrayList<SetReplyEvent> setReplyEvent = new ArrayList<>();

    public static void sendUpdate() {
        PlayerInfo playerInfo = new PlayerInfo(CardCrawlGame.playerName, AbstractDungeon.player.currentHealth, AbstractDungeon.floorNum, AbstractDungeon.player.gold);
        SetPacket packet = new SetPacket("spire_player_" + playerInfo.getName(), "");
        packet.addDataStorageOperation(SetPacket.Operation.REPLACE, playerInfo);
        packet.want_reply = true;
        APClient.apClient.dataStorageSet(packet);
    }

    public static void initialLoad() {
        SidePanel.playerPanels.clear();
        sendUpdate();
        SetPacket init = new SetPacket("spire_players", Collections.singleton(CardCrawlGame.playerName));
        init.addDataStorageOperation(SetPacket.Operation.DEFAULT, "i'm needed!");
        init.want_reply = true;
        playerListRequest = APClient.apClient.dataStorageSet(init);
        APClient.apClient.dataStorageSetNotify(Collections.singleton("spire_players"));
    }

    private static final Type arrayListString = new TypeToken<ArrayList<String>>() {
    }.getType();

    @ArchipelagoEventListener
    public void playerInfoReceived(RetrievedEvent event) {
        retrievedEvent.add(event);
    }

    @ArchipelagoEventListener
    public void dataReceived(SetReplyEvent event) {
        setReplyEvent.add(event);
    }

    public static void update() {
        for (RetrievedEvent event : retrievedEvent) {
            for (String key : event.data.keySet()) {
                if (key.startsWith("spire_player_")) {
                    PlayerInfo player = event.getValueAsObject(key,PlayerInfo.class);
                    if(player != null)
                        SidePanel.updatePlayer(event.getValueAsObject(key,PlayerInfo.class));
                }
            }
        }
        retrievedEvent.clear();
        for (SetReplyEvent event : setReplyEvent) {
            if (event.getRequestID() == playerListRequest) {

                ArrayList<String> playerNames = event.getValueAsObject(arrayListString);
                if (!playerNames.contains(CardCrawlGame.playerName)) {
                    SetPacket init = new SetPacket("spire_players", new ArrayList<String>());
                    init.addDataStorageOperation(SetPacket.Operation.ADD, Collections.singleton(CardCrawlGame.playerName));
                    APClient.apClient.dataStorageSet(init);
                }

                ArrayList<String> keys = new ArrayList<>();

                for (String playerName : playerNames) {
                    keys.add("spire_player_" + playerName);
                }

                APClient.apClient.dataStorageSetNotify(keys);
                APClient.apClient.dataStorageGet(keys);

            } else if (event.key.startsWith("spire_player_")) {
                SidePanel.updatePlayer(event.getValueAsObject(PlayerInfo.class));

            } else if (event.key.startsWith("spire_players")) {
                ArrayList<String> playerNames = event.getValueAsObject(arrayListString);
                ArrayList<String> keys = new ArrayList<>();
                for (String playerName : playerNames) {
                    keys.add("spire_player_" + playerName);
                }
                APClient.apClient.dataStorageSetNotify(keys);
                APClient.apClient.dataStorageGet(keys);
            }
        }
        setReplyEvent.clear();
    }
}
