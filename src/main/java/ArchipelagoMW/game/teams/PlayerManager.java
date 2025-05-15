package ArchipelagoMW.game.teams;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.game.teams.ui.hud.PlayerPanel;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.RetrievedEvent;
import dev.koifysh.archipelago.events.SetReplyEvent;
import dev.koifysh.archipelago.network.client.SetPacket;
import javassist.CtBehavior;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    @SpirePatch(clz = AbstractPlayer.class, method = "damage")
    public static class damageDetect {
        @SpireInsertPatch(locator = Locator.class, localvars = "damageAmount")
        public static void insert(AbstractPlayer __instance, int damageAmount) {
            if (TeamManager.sendDamageLink(damageAmount)) {
                __instance.currentHealth += damageAmount;
            }
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

    @SpirePatch(clz = AbstractCreature.class, method = "heal", paramtypez = {int.class, boolean.class})
    public static class healDetect {
        @SpireInsertPatch(locator = Locator.class)
        public static void insert(AbstractCreature __instance, int healAmount) {
            if (!(__instance instanceof AbstractPlayer))
                return;

            if (__instance.currentHealth + healAmount > __instance.maxHealth) {
                healAmount = __instance.maxHealth - __instance.currentHealth;
            }
            if (TeamManager.sendDamageLink(-healAmount)) {
                __instance.currentHealth -= healAmount;
            }
            sendUpdate();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(AbstractCreature.class, "currentHealth");
                return new int[]{LineFinder.findAllInOrder(ctBehavior, match)[0]};
            }
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "gainGold")
    public static class gainGold {
        @SpireInsertPatch(locator = Locator.class)
        public static void insert(AbstractPlayer __instance, int amount) {
            TeamManager.sendGoldLink(amount);
            sendUpdate();

        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "gold");
                return LineFinder.findInOrder(ctBehavior, match);
            }
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "loseGold")
    public static class loseGold {
        @SpireInsertPatch(locator = Locator.class)
        public static void insert(AbstractPlayer __instance, int amount) {
            TeamManager.sendGoldLink(-amount);
            sendUpdate();
        }


        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "gold");
                return LineFinder.findInOrder(ctBehavior, match);
            }
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
        public static void Postfix(AbstractDungeon __instance) {
            if (__instance instanceof Exordium)
                PlayerManager.initialLoad();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {String.class, String.class, AbstractPlayer.class, ArrayList.class})
    public static class GenerateConstructor {
        @SpirePostfixPatch
        public static void Postfix(AbstractDungeon __instance) {
            if (__instance instanceof Exordium)
                PlayerManager.initialLoad();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "update")
    public static class update {
        @SpirePrefixPatch
        public static void Prefix() {
            update();
        }
    }

    @SpirePatch(clz = AbstractCreature.class, method = "increaseMaxHp")
    public static class increaseMaxHP {

        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(AbstractCreature __instance, int amount) {
            if (__instance instanceof AbstractPlayer) {
                sendUpdate();
                if (TeamManager.sendMaxHPLink(amount)) {
                    __instance.maxHealth -= amount;
                }
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(AbstractCreature.class, "maxHealth");
                return LineFinder.findInOrder(ctBehavior, match);
            }
        }
    }

    @SpirePatch(clz = AbstractCreature.class, method = "decreaseMaxHealth")
    public static class decreaseMaxHealth {

        @SpireInsertPatch(locator = Locator.class)
        public static void insert(AbstractCreature __instance, int amount) {
            if (__instance instanceof AbstractPlayer) {
                sendUpdate();
                if (TeamManager.sendMaxHPLink(-amount)) {
                    __instance.maxHealth += amount;
                }
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.FieldAccessMatcher(AbstractCreature.class, "maxHealth");
                return LineFinder.findInOrder(ctBehavior, match);
            }
        }
    }

    public static int playerListRequest = 0;

    public static ConcurrentHashMap<String, PlayerInfo> players = new ConcurrentHashMap<>();

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static void sendUpdate() {
        if (!CardCrawlGame.isInARun())
            return;
        PlayerInfo playerInfo = new PlayerInfo(CardCrawlGame.playerName, AbstractDungeon.player.currentHealth, AbstractDungeon.player.maxHealth, AbstractDungeon.floorNum, AbstractDungeon.player.gold);
        if (TeamManager.myTeam != null) {
            playerInfo.teamColor = TeamManager.myTeam.teamColor;
            playerInfo.team = TeamManager.myTeam.name;
        }
        SetPacket packet = new SetPacket("spire_player_" + playerInfo.getName(), "");
        packet.addDataStorageOperation(SetPacket.Operation.REPLACE, gson.toJson(playerInfo));
        packet.want_reply = true;
        APContext.getContext().getClient().dataStorageSet(packet);
    }

    public static void initialLoad() {
        APClient client = APContext.getContext().getClient();
        Archipelago.sideBar.playerPanels.clear();
        players.clear();
        sendUpdate();
        SetPacket initPlayers = new SetPacket("spire_players", Collections.singleton(CardCrawlGame.playerName));
        initPlayers.addDataStorageOperation(SetPacket.Operation.DEFAULT, "i'm needed!");
        initPlayers.want_reply = true;
        playerListRequest = client.dataStorageSet(initPlayers);

        client.dataStorageSetNotify(Collections.singleton("spire_players"));

    }

    private static final Type arrayListString = new TypeToken<ArrayList<String>>() {
    }.getType();

    @ArchipelagoEventListener
    public void playerInfoReceived(RetrievedEvent event) {
        for (String key : event.data.keySet()) {
            if (key.startsWith("spire_player_")) {
                PlayerInfo player = gson.fromJson(event.getString(key), PlayerInfo.class);
                if (player != null)
                    updatePlayer(player);
            }
        }
        Archipelago.sideBar.sortPlayers();
    }

    @ArchipelagoEventListener
    public void dataReceived(SetReplyEvent event) {
        String[] key = event.key.split("_");
        if (!key[1].startsWith("player"))
            return;
        APClient client = APContext.getContext().getClient();
        if (event.getRequestID() == playerListRequest) {
            ArrayList<String> playerNames = event.getValueAsObject(arrayListString);
            if (!playerNames.contains(CardCrawlGame.playerName)) {
                SetPacket addSelf = new SetPacket("spire_players", new ArrayList<String>());
                addSelf.addDataStorageOperation(SetPacket.Operation.ADD, Collections.singleton(CardCrawlGame.playerName));
                client.dataStorageSet(addSelf);
            }

            ArrayList<String> keys = new ArrayList<>();

            for (String playerName : playerNames) {
                keys.add("spire_player_" + playerName);
            }

            client.dataStorageSetNotify(keys);
            client.dataStorageGet(keys);

        } else if (key.length == 3 && key[1].equals("player")) {
            updatePlayer(gson.fromJson((String) event.value, PlayerInfo.class));
            Archipelago.sideBar.sortPlayers();

        } else if (key.length == 2 && key[1].equals("players")) {
            ArrayList<String> playerNames = event.getValueAsObject(arrayListString);
            ArrayList<String> keys = new ArrayList<>();
            for (String playerName : playerNames) {
                keys.add("spire_player_" + playerName);
            }
            client.dataStorageSetNotify(keys);
            client.dataStorageGet(keys);
        }
    }

    public static void update() {

    }

    public static void updatePlayer(PlayerInfo newPlayer) {
        boolean found = false;
        for (PlayerInfo oldPlayer : players.values()) {
            if (oldPlayer.getName().equals(newPlayer.getName())) {
                found = true;
                oldPlayer.update(newPlayer);
                oldPlayer.dirty = true;
                newPlayer = oldPlayer;
            }
        }
        if (!found) {
            newPlayer.dirty = true;
            players.put(newPlayer.getName(), newPlayer);
            Archipelago.sideBar.playerPanels.add(new PlayerPanel(newPlayer));
        }
        if (
                TeamManager.myTeam != null
                        && !TeamManager.myTeam.affectsApplied
                        && TeamManager.myTeam.name.equals(newPlayer.team)
                        && TeamManager.myTeam.leader.equals(CardCrawlGame.playerName)
        ) {
            TeamManager.applyTeamAffects();
        }
    }
}
