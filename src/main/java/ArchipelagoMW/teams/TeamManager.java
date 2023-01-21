package ArchipelagoMW.teams;

import ArchipelagoMW.APClient;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.ui.hud.TeamButton;
import ArchipelagoMW.util.DeathLinkHelper;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.DeathScreen;
import gg.archipelago.client.events.ArchipelagoEventListener;
import gg.archipelago.client.events.BouncedEvent;
import gg.archipelago.client.events.RetrievedEvent;
import gg.archipelago.client.events.SetReplyEvent;
import gg.archipelago.client.helper.DeathLink;
import gg.archipelago.client.network.client.BouncePacket;
import gg.archipelago.client.network.client.SetPacket;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {

    public static final  HashMap<String, Color> colorMap = new HashMap<String, Color>() {{
        put("red",Color.RED);
        put("blue",Color.BLUE);
        put("green",Color.GREEN);
        put("violet",Color.VIOLET);
        put("gold",Color.GOLD);
        put("purple",Color.PURPLE);
        put("pink",Color.PINK);
        put("gray",Color.GRAY);
        put("black",Color.BLACK);
        put("white",Color.WHITE);
    }};

    @SpirePatch(clz = AbstractDungeon.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {String.class, AbstractPlayer.class, SaveFile.class})
    public static class LoadConstructor {
        @SpirePostfixPatch
        public static void Postfix(AbstractDungeon __instance) {
            if(__instance instanceof Exordium)
                initialLoad();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {String.class, String.class, AbstractPlayer.class, ArrayList.class})
    public static class GenerateConstructor {
        @SpirePostfixPatch
        public static void Postfix(AbstractDungeon __instance) {
            if(__instance instanceof Exordium)
                initialLoad();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "update")
    public static class update {
        @SpirePrefixPatch
        public static void Prefix() {
            update();
        }
    }

    public static int teamListRequest = 0;

    public static boolean checkIfDead = false;

    public static ConcurrentHashMap<String, TeamInfo> teams = new ConcurrentHashMap<>();

    public static TeamInfo myTeam;

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static void initialLoad() {
        Archipelago.sideBar.APTeamsPanel.teamButtons.clear();
        Archipelago.sideBar.APTeamsPanel.selectedTeam = null;
        teams.clear();
        myTeam = null;

        SetPacket initTeams = new SetPacket("spire_teams", new ArrayList<>());
        initTeams.addDataStorageOperation(SetPacket.Operation.DEFAULT, "i'm needed!");
        initTeams.want_reply = true;
        teamListRequest = APClient.apClient.dataStorageSet(initTeams);
        APClient.apClient.dataStorageSetNotify(Collections.singleton("spire_teams"));
    }

    private static final Type arrayListString = new TypeToken<ArrayList<String>>() {
    }.getType();

    @ArchipelagoEventListener
    public void teamInfoReceived(RetrievedEvent event) {
        for (String stringKey : event.data.keySet()) {
            String[] key = stringKey.split("_");

            if (key.length == 3 && key[1].equals("team")) { // spire_team_{name}
                if (!updateTeam(gson.fromJson((String) event.data.get(stringKey), TeamInfo.class))) {
                    Archipelago.sideBar.APTeamsPanel.teamButtons.removeIf(teamButton -> teamButton.getName().equals(key[2]));
                    if(Archipelago.sideBar.APTeamsPanel.selectedTeam.name.equals(key[2])) {
                        Archipelago.sideBar.APTeamsPanel.selectedTeam = null;
                    }
                }
            }
            if (key.length == 4 && key[3].equals("players")) { // spire_team_{name}_players
                ArrayList<String> players = event.getValueAsObject(stringKey, arrayListString);
                if (players != null && players.contains(CardCrawlGame.playerName) && (myTeam == null || !key[2].equals(myTeam.name))) {
                    while (players.contains(CardCrawlGame.playerName)) {
                        players.remove(CardCrawlGame.playerName);
                    }
                    SetPacket removeMe = new SetPacket(stringKey, "I matter");
                    removeMe.addDataStorageOperation(SetPacket.Operation.REPLACE, players);
                    APClient.apClient.dataStorageSet(removeMe);
                    if (players.isEmpty()) {
                        teams.remove(key[2]);
                        Archipelago.sideBar.APTeamsPanel.teamButtons.removeIf(teamButton -> teamButton.getName().equals(key[2]));
                        SetPacket deleteTeam = new SetPacket("spire_teams", "I matter");

                        deleteTeam.addDataStorageOperation(SetPacket.Operation.REPLACE, teams.keySet().toArray());
                        APClient.apClient.dataStorageSet(deleteTeam);
                    }
                } else {
                    TeamInfo team = teams.get(key[2]);
                    if (team != null)
                        team.members = event.getValueAsObject(stringKey, arrayListString);
                }
            }

            if (key.length == 4 && key[1].equals("team")){// spire_team_{name}_
                if (key[3].equals("players")) { // spire_team_{name}_players
                    TeamInfo team = teams.get(key[2]);
                    if (team != null)
                        team.members = event.getValueAsObject(stringKey,arrayListString);

                } else if ( myTeam != null && key[2].equals(myTeam.name)  ) { // spire_team_{myTeam}_
                    switch (key[3]) {
                        case "hp":  // spire_team_{name}_hp
                            if (myTeam.locked && myTeam.healthLink) {
                                AbstractDungeon.player.currentHealth = event.getValueAsObject(stringKey, Integer.class);
                                checkIfDead = true;
                                AbstractDungeon.player.healthBarUpdatedEvent();
                                PlayerManager.sendUpdate();
                            }
                            break;
                        case "maxhp":  // spire_team_{name}_max
                            AbstractDungeon.player.maxHealth = event.getValueAsObject(stringKey, Integer.class);
                            if (AbstractDungeon.player.currentHealth > AbstractDungeon.player.maxHealth)
                                AbstractDungeon.player.currentHealth = AbstractDungeon.player.maxHealth;
                            AbstractDungeon.player.healthBarUpdatedEvent();
                            PlayerManager.sendUpdate();
                            break;
                        case "gold":
                            AbstractDungeon.player.gold = event.getValueAsObject(stringKey, Integer.class);
                            PlayerManager.sendUpdate();
                            break;
                    }
                }
            }
        }
    }

    @ArchipelagoEventListener
    public void dataReceived(SetReplyEvent event) {
        if (event.value == null || event.value.equals("{}"))
            return;
        String[] key = event.key.split("_");
        if (key.length < 2 || !key[0].equals("spire") || !key[1].startsWith("team")) // all of our keys contain at LEAST 2 keys, and start with "spire"
            return;

        if (key[1].equals("teams")) { // spire_teams

            ArrayList<String> teamNames = event.getValueAsObject(arrayListString);

            // remove known teams from our list of teamNames while also removing all known teams that are not in the TeamName's list
            for (String team : teams.keySet()) {
                if (!teamNames.remove(team)) {
                    teams.remove(team);
                }
            }
            //teams.removeIf(team -> !teamNames.remove(team.name));


            for (String teamName : teamNames) {

                updateTeam(new TeamInfo(teamName));
            }

        } else if (key.length == 3 && key[1].equals("team")) { // spire_team_{name}
            updateTeam(gson.fromJson((String) event.value, TeamInfo.class));

        } else if (key.length == 4 && key[1].equals("team")){// spire_team_{name}_
            if (key[3].equals("players")) { // spire_team_{name}_players
                ArrayList<String> players = event.getValueAsObject(arrayListString);
                TeamInfo team = teams.get(key[2]);
                if (team != null)
                    team.members = players;


                if (players != null && players.contains(CardCrawlGame.playerName) && (myTeam == null || !key[2].equals(myTeam.name))) {
                    while (players.contains(CardCrawlGame.playerName)) {
                        players.remove(CardCrawlGame.playerName);
                    }
                    SetPacket removeMe = new SetPacket(event.key, "I matter");
                    removeMe.addDataStorageOperation(SetPacket.Operation.REPLACE, players);
                    APClient.apClient.dataStorageSet(removeMe);
                    if (players.isEmpty()) {
                        teams.remove(key[2]);
                        Archipelago.sideBar.APTeamsPanel.teamButtons.removeIf(teamButton -> teamButton.getName().equals(key[2]));
                        SetPacket deleteTeam = new SetPacket("spire_teams", "I matter");

                        deleteTeam.addDataStorageOperation(SetPacket.Operation.REPLACE, teams.keySet().toArray());
                        APClient.apClient.dataStorageSet(deleteTeam);
                    }
                }
            } else if ( myTeam != null && key[2].equals(myTeam.name)  ) { // spire_team_{myTeam}_
                switch (key[3]) {
                    case "hp":  // spire_team_{name}_hp
                        if (myTeam.locked && myTeam.healthLink) {
                            AbstractDungeon.player.currentHealth = event.getValueAsObject(Integer.class);
                            checkIfDead = true;
                            AbstractDungeon.player.healthBarUpdatedEvent();
                            PlayerManager.sendUpdate();
                        }
                        break;
                    case "maxhp":  // spire_team_{name}_max
                        AbstractDungeon.player.maxHealth = event.getValueAsObject(Integer.class);
                        PlayerManager.sendUpdate();
                        AbstractDungeon.player.healthBarUpdatedEvent();
                        break;
                    case "gold":
                        AbstractDungeon.player.gold = event.getValueAsObject(Integer.class);
                        PlayerManager.sendUpdate();
                        break;
                }
            }
        }
    }

    public static void update() {
        if (checkIfDead) {
            checkIfDead();
            checkIfDead = false;
        }
    }

    public static void uploadTeam(TeamInfo team) {
        SetPacket teamUpdatePacket = new SetPacket("spire_team_" + team.name, new ArrayList<>());
        teamUpdatePacket.addDataStorageOperation(SetPacket.Operation.REPLACE, gson.toJson(team));
        teamUpdatePacket.want_reply = true;
        APClient.apClient.dataStorageSet(teamUpdatePacket);
    }

    private static boolean updateTeam(TeamInfo newTeam) {
        if (newTeam == null)
            return false;

        if (!teams.containsKey(newTeam.name)) {
            newTeam.members = new ArrayList<>();
            teams.put(newTeam.name, newTeam);
            ArrayList<String> keys = new ArrayList<>();
            keys.add("spire_team_" + newTeam.name);
            keys.add("spire_team_" + newTeam.name + "_players");
            APClient.apClient.dataStorageSetNotify(keys);
            APClient.apClient.dataStorageGet(keys);
            Archipelago.sideBar.APTeamsPanel.teamButtons.add(new TeamButton(newTeam));
            return true;
        }
        TeamInfo oldTeam = teams.get(newTeam.name);
        oldTeam.update(newTeam);
        if (Archipelago.sideBar.APTeamsPanel.selectedTeam != null && Archipelago.sideBar.APTeamsPanel.selectedTeam.equals(oldTeam))
            Archipelago.sideBar.APTeamsPanel.updateToggles();
        return true;
    }


    //new HashMap<String>(Arrays.asList("red","blue","green","violet","gold","purple","pink","gray","black","white")

    public static boolean createTeam(TeamInfo team) {
        if (teams.containsKey(team.name) || myTeam != null)
            return false;

        team.leader = CardCrawlGame.playerName;


        uploadTeam(team);
        joinTeam(team);

        SetPacket addTeamPacket = new SetPacket("spire_teams", new ArrayList<>());
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, Collections.singleton(team.name));
        addTeamPacket.want_reply = true;
        APClient.apClient.dataStorageSet(addTeamPacket);
        return true;
    }


    public static void joinTeam(TeamInfo team) {
        if (myTeam != null || team.locked)
            return;


        myTeam = team;
        updateTeam(team);
        Archipelago.sideBar.APTeamsPanel.selectedTeam = team;

        SetPacket addTeamPacket = new SetPacket("spire_team_" + team.name + "_players", new ArrayList<>());
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, Collections.singleton(CardCrawlGame.playerName));
        addTeamPacket.want_reply = true;
        APClient.apClient.dataStorageSet(addTeamPacket);
        PlayerManager.sendUpdate();

    }

    public static void leaveTeam() {
        if (myTeam == null || myTeam.locked)
            return;

        while (myTeam.members.contains(CardCrawlGame.playerName))
            myTeam.members.remove(CardCrawlGame.playerName);

        SetPacket removeMe = new SetPacket("spire_team_" +myTeam.name+"_players", "I matter");
        removeMe.addDataStorageOperation(SetPacket.Operation.REPLACE, myTeam.members);
        APClient.apClient.dataStorageSet(removeMe);

        if (myTeam.members.isEmpty()) {
            teams.remove(myTeam.name);
            Archipelago.sideBar.APTeamsPanel.teamButtons.removeIf(teamButton -> teamButton.getName().equals(myTeam.name));
            SetPacket deleteTeam = new SetPacket("spire_teams", "I matter");

            deleteTeam.addDataStorageOperation(SetPacket.Operation.REPLACE, teams.keySet().toArray());
            APClient.apClient.dataStorageSet(deleteTeam);
        }

        if (myTeam.members.size() > 0) {
            myTeam.leader = myTeam.members.get(0);
            uploadTeam(myTeam);
        }
        if (Archipelago.sideBar.APTeamsPanel.selectedTeam == myTeam)
            Archipelago.sideBar.APTeamsPanel.selectedTeam = null;
        myTeam = null;
        PlayerManager.sendUpdate();
    }

    public static void lockTeam() {
        myTeam.locked = true;
        uploadTeam(myTeam);
        if (myTeam.healthLink) {
            //INITALIZE PAIN!

            //no death link.. just.. no
            DeathLink.setDeathLinkEnabled(false);

            int health = 0;
            for (String member : myTeam.members) {
                 health += PlayerManager.players.get(member).health;
            }

            health /= myTeam.members.size();
            health *= 1.5;

            int maxHealth = 0;
            for (String member : myTeam.members) {
                maxHealth += PlayerManager.players.get(member).maxHealth;
            }

            maxHealth /= myTeam.members.size();
            maxHealth *= 1.5;

            SetPacket initHP = new SetPacket("spire_team_" +myTeam.name+"_hp", health);
            initHP.addDataStorageOperation(SetPacket.Operation.REPLACE, health);
            APClient.apClient.dataStorageSet(initHP);

            SetPacket initMaxHp = new SetPacket("spire_team_" +myTeam.name+"_maxhp", maxHealth);
            initMaxHp.addDataStorageOperation(SetPacket.Operation.REPLACE, maxHealth);
            APClient.apClient.dataStorageSet(initMaxHp);

            APClient.apClient.dataStorageGet(Arrays.asList("spire_team_" +myTeam.name+"_maxhp","spire_team_" +myTeam.name+"_hp"));
        }

        if(myTeam.goldLink) {
            int gold = 0;
            for (String member : myTeam.members) {
                gold += PlayerManager.players.get(member).gold;
            }

            SetPacket initHP = new SetPacket("spire_team_" +myTeam.name+"_gold", 0);
            initHP.addDataStorageOperation(SetPacket.Operation.REPLACE, gold);
            APClient.apClient.dataStorageSet(initHP);

            APClient.apClient.dataStorageGet(Collections.singletonList("spire_team_" + myTeam.name + "_gold"));

        }

        BouncePacket lockedBounce = new BouncePacket();
        lockedBounce.games = new String[]{"Slay the Spire"};
        lockedBounce.setData(new HashMap<String,Object>(){{put("spire_team_" + myTeam.name +"_locked",true);}});
        APClient.apClient.sendBounce(lockedBounce);
    }

    @ArchipelagoEventListener
    public void bounced(BouncedEvent event) {
        if(myTeam != null && event.containsKey("spire_team_"+myTeam.name+"_locked")) {
            if(myTeam.healthLink) {
                APClient.apClient.dataStorageSetNotify(Arrays.asList("spire_team_" + myTeam.name + "_hp", "spire_team_" + myTeam.name + "_maxhp"));
                APClient.apClient.dataStorageGet(Arrays.asList("spire_team_" + myTeam.name + "_maxhp", "spire_team_" + myTeam.name + "_hp"));
            }
            if(myTeam.goldLink) {
                APClient.apClient.dataStorageSetNotify(Collections.singletonList("spire_team_" + myTeam.name + "_gold"));
                APClient.apClient.dataStorageGet(Collections.singletonList("spire_team_" + myTeam.name + "_gold"));
            }
        }
    }

    public static boolean sendDamageLink(int damageAmount) {
        if (myTeam == null || !myTeam.healthLink)
            return false;

        SetPacket addTeamPacket = new SetPacket("spire_team_" + myTeam.name + "_hp", 0);
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, -damageAmount);
        addTeamPacket.want_reply = true;
        APClient.apClient.dataStorageSet(addTeamPacket);
        return true;
    }

    public static boolean sendGoldLink(int goldAmount) {
        if (myTeam == null || !myTeam.healthLink)
            return false;

        SetPacket addTeamPacket = new SetPacket("spire_team_" + myTeam.name + "_gold", 0);
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, goldAmount);
        addTeamPacket.want_reply = true;
        APClient.apClient.dataStorageSet(addTeamPacket);
        return true;
    }

    public static boolean sendMaxHPLink(int hpChange) {
        if (myTeam == null || !myTeam.healthLink)
            return false;

        SetPacket addTeamPacket = new SetPacket("spire_team_" + myTeam.name + "_maxhp", 0);
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, hpChange);
        addTeamPacket.want_reply = true;
        APClient.apClient.dataStorageSet(addTeamPacket);
        return true;
    }

    public static void checkIfDead() {
        if (AbstractDungeon.player.currentHealth <= 0 && !AbstractDungeon.player.isDead) {
            AbstractDungeon.player.currentHealth = 0;
            AbstractDungeon.player.isDead = true;
            AbstractDungeon.deathScreen = new DeathScreen(null);
            AbstractDungeon.screen = AbstractDungeon.CurrentScreen.DEATH;
            TeamManager.leaveTeam();
        }

    }
}
