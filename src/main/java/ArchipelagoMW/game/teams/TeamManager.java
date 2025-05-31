package ArchipelagoMW.game.teams;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.apEvents.ConnectionResult;
import ArchipelagoMW.game.ui.Components.TeamButton;
import ArchipelagoMW.game.connect.ui.connection.APTeamsPanel;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.DeathScreen;
import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.BouncedEvent;
import dev.koifysh.archipelago.events.RetrievedEvent;
import dev.koifysh.archipelago.events.SetReplyEvent;
import dev.koifysh.archipelago.helper.DeathLink;
import dev.koifysh.archipelago.network.client.BouncePacket;
import dev.koifysh.archipelago.network.client.SetPacket;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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

    @SpirePatch(clz = CardCrawlGame.class, method = "update")
    public static class update {
        @SpirePrefixPatch
        public static void Prefix() {
            update();
        }
    }

    public static int teamListRequest = 0;

    public static boolean checkIfDead = false;

    public static ConcurrentHashMap<String, TeamInfo> teams = new ConcurrentHashMap<>();
    public static CopyOnWriteArrayList<TeamInfo> newTeams = new CopyOnWriteArrayList<>();

    public static TeamInfo myTeam;

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static void initialLoad() {
        APTeamsPanel.teamButtons.clear();
        APTeamsPanel.selectedTeam = null;
        teams.clear();
        myTeam = null;

        SetPacket initTeams = new SetPacket("spire_teams", new ArrayList<>());
        initTeams.addDataStorageOperation(SetPacket.Operation.DEFAULT, "i'm needed!");
        initTeams.want_reply = true;
        teamListRequest = APContext.getContext().getClient().dataStorageSet(initTeams);
        APContext.getContext().getClient().dataStorageSetNotify(Collections.singleton("spire_teams"));
    }

    private static final Type arrayListString = new TypeToken<ArrayList<String>>() {
    }.getType();

    @ArchipelagoEventListener
    public void teamInfoReceived(RetrievedEvent event) {
        APClient client = APContext.getContext().getClient();
        for (String stringKey : event.data.keySet()) {
            String[] key = stringKey.split("_");

            if(key.length < 2 || !key[1].startsWith("team")) // not a team update
                return;

            if (key.length == 3 && key[1].equals("team")) { // spire_team_{name}
                updateTeam(gson.fromJson((String) event.data.get(stringKey), TeamInfo.class));
            }
            if (key.length == 4 && key[3].equals("players")) { // spire_team_{name}_players
                ArrayList<String> players = event.getValueAsObject(stringKey, arrayListString);
                if (players != null && players.contains(CardCrawlGame.playerName) && (myTeam == null || !key[2].equals(myTeam.name))) {
                    SetPacket removeMe = new SetPacket(stringKey, "I matter");
                    removeMe.addDataStorageOperation(SetPacket.Operation.REMOVE,CardCrawlGame.playerName);
                    client.dataStorageSet(removeMe);
                    if (players.isEmpty()) {
                        if(APTeamsPanel.selectedTeam != null && APTeamsPanel.selectedTeam.name.equals(key[2]))
                            APTeamsPanel.selectedTeam = null;
                        teams.remove(key[2]);
                        SetPacket deleteTeam = new SetPacket("spire_teams", "I matter");
                        deleteTeam.want_reply = true;
                        deleteTeam.addDataStorageOperation(SetPacket.Operation.REMOVE, key[2]);
                        client.dataStorageSet(deleteTeam);
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

        APClient client = APContext.getContext().getClient();
        if (key[1].equals("teams")) { // spire_teams

            ArrayList<String> teamNames = event.getValueAsObject(arrayListString);

            // remove known teams from our list of teamNames while also removing all known teams that are not in the TeamName's list
            for (String team : teams.keySet()) {
                if (!teamNames.remove(team)) {
                    if(APTeamsPanel.selectedTeam != null && APTeamsPanel.selectedTeam.name.equals(team))
                        APTeamsPanel.selectedTeam = null;
                    teams.remove(team);
                }
            }

            for (String teamName : teamNames) {
                newTeams.add(new TeamInfo(teamName));
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
                    client.dataStorageSet(removeMe);
                    if (players.isEmpty()) {
                        if(APTeamsPanel.selectedTeam != null && APTeamsPanel.selectedTeam.name.equals(key[2]))
                            APTeamsPanel.selectedTeam = null;
                        teams.remove(key[2]);
                        SetPacket deleteTeam = new SetPacket("spire_teams", "I matter");
                        deleteTeam.want_reply = true;
                        deleteTeam.addDataStorageOperation(SetPacket.Operation.REPLACE, teams.keySet().toArray());
                        client.dataStorageSet(deleteTeam);
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
        for (TeamInfo team : newTeams) {
            addTeam(team);
        }
        newTeams.clear();
        if (checkIfDead) {
            checkIfDead();
            checkIfDead = false;
        }
        APTeamsPanel.teamButtons.removeIf(teamButton -> !teams.containsKey(teamButton.getName()));
    }

    public static void uploadTeam(TeamInfo team) {
        SetPacket teamUpdatePacket = new SetPacket("spire_team_" + team.name, null);
        teamUpdatePacket.addDataStorageOperation(SetPacket.Operation.REPLACE, gson.toJson(team));
        teamUpdatePacket.want_reply = true;
        APContext.getContext().getClient().dataStorageSet(teamUpdatePacket);
    }

    private static boolean updateTeam(TeamInfo team) {
        if (team == null || !teams.containsKey(team.name))
            return false;

        if(team.members == null)
            team.members = new ArrayList<>();

        TeamInfo oldTeam = teams.get(team.name);
        if(myTeam != null && myTeam.name.equals(team.name) && !oldTeam.locked && team.locked) {
            // initiate game start!
            // TODO: what is this team manager stuffs?
            ConnectionResult.start();
        }
        oldTeam.update(team);
        if (APTeamsPanel.selectedTeam != null && APTeamsPanel.selectedTeam.equals(oldTeam))
            APTeamsPanel.updateToggles();
        return true;
    }

    private static boolean addTeam(TeamInfo team) {
        if (!teams.containsKey(team.name)) {
            team.members = new ArrayList<>();
            teams.put(team.name, team);
            ArrayList<String> keys = new ArrayList<>();
            keys.add("spire_team_" + team.name);
            keys.add("spire_team_" + team.name + "_players");
            APContext.getContext().getClient().dataStorageSetNotify(keys);
            APContext.getContext().getClient().dataStorageGet(keys);
            APTeamsPanel.teamButtons.add(new TeamButton(team));
            return true;
        }
        return false;
    }

    public static final ArrayList<String> teamNames = new ArrayList<>(Arrays.asList("red","blue","green","gold","purple","pink","gray","white"));

    public static boolean createTeam() {
        ArrayList<String> names = new ArrayList<>(teamNames);
        names.removeAll(teams.keySet());
        if(names.isEmpty())
            return false;

        Collections.shuffle(names);
        TeamInfo team = new TeamInfo(names.remove(0));
        team.teamColor = TeamManager.colorMap.get(team.name);

        if (teams.containsKey(team.name) || myTeam != null)
            return false;

        team.leader = CardCrawlGame.playerName;

        addTeam(team);
        joinTeam(team);
        uploadTeam(team);

        SetPacket addTeamPacket = new SetPacket("spire_teams", new ArrayList<>());
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, Collections.singleton(team.name));
        addTeamPacket.want_reply = true;
        APContext.getContext().getClient().dataStorageSet(addTeamPacket);


        return true;
    }


    public static void joinTeam(TeamInfo team) {
        if (myTeam != null || team.locked)
            return;

        myTeam = team;
        updateTeam(team);
        APTeamsPanel.selectedTeam = team;

        SetPacket addTeamPacket = new SetPacket("spire_team_" + team.name + "_players", new ArrayList<>());
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, Collections.singleton(CardCrawlGame.playerName));
        addTeamPacket.want_reply = true;
        APContext.getContext().getClient().dataStorageSet(addTeamPacket);
    }

    public static void leaveTeam() {
        if (myTeam == null)
            return;
        APClient client = APContext.getContext().getClient();

        while (myTeam.members.contains(CardCrawlGame.playerName))
            myTeam.members.remove(CardCrawlGame.playerName);

        SetPacket removeMe = new SetPacket("spire_team_" +myTeam.name+"_players", null);
        removeMe.addDataStorageOperation(SetPacket.Operation.REMOVE, CardCrawlGame.playerName);
        client.dataStorageSet(removeMe);

        if (myTeam.members.isEmpty()) {
            if(APTeamsPanel.selectedTeam != null && APTeamsPanel.selectedTeam.name.equals(myTeam.name))
                APTeamsPanel.selectedTeam = null;
            teams.remove(myTeam.name);
            SetPacket removeTeamListing = new SetPacket("spire_teams", null);
            removeTeamListing.want_reply = true;
            removeTeamListing.addDataStorageOperation(SetPacket.Operation.REMOVE,myTeam.name);
            client.dataStorageSet(removeTeamListing);
            SetPacket deleteTeam = new SetPacket("spire_team_"+myTeam.name, null);
            deleteTeam.addDataStorageOperation(SetPacket.Operation.REPLACE,null);
            client.dataStorageSet(deleteTeam);
        }
        else {
            myTeam.leader = myTeam.members.get(0);
            uploadTeam(myTeam);
        }
        if (APTeamsPanel.selectedTeam == myTeam)
            APTeamsPanel.selectedTeam = null;
        myTeam = null;
    }

    public static void lockTeam() {
        if(myTeam == null)
            return;

        myTeam.locked = true;
        uploadTeam(myTeam);
    }

    public static void applyTeamAffects() {
        if(myTeam == null)
            return;

        if(myTeam.members.stream().anyMatch(member -> !PlayerManager.players.containsKey(member))) return;

        APClient client = APContext.getContext().getClient();

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
            client.dataStorageSet(initHP);

            SetPacket initMaxHp = new SetPacket("spire_team_" +myTeam.name+"_maxhp", maxHealth);
            initMaxHp.addDataStorageOperation(SetPacket.Operation.REPLACE, maxHealth);
            client.dataStorageSet(initMaxHp);

            client.dataStorageGet(Arrays.asList("spire_team_" +myTeam.name+"_maxhp","spire_team_" +myTeam.name+"_hp"));

            myTeam.affectsApplied = true;
        }

        if(myTeam.goldLink) {
            int gold = 0;
            for (String member : myTeam.members) {
                gold += PlayerManager.players.get(member).gold;
            }

            SetPacket initHP = new SetPacket("spire_team_" +myTeam.name+"_gold", 0);
            initHP.addDataStorageOperation(SetPacket.Operation.REPLACE, gold);
            client.dataStorageSet(initHP);

            client.dataStorageGet(Collections.singletonList("spire_team_" + myTeam.name + "_gold"));

        }

        BouncePacket lockedBounce = new BouncePacket();
        lockedBounce.games = new String[]{"Slay the Spire"};
        lockedBounce.setData(new HashMap<String,Object>(){{put("spire_team_" + myTeam.name +"_locked",true);}});
        client.sendBounce(lockedBounce);
    }

    @ArchipelagoEventListener
    public void bounced(BouncedEvent event) {
        APClient client = APContext.getContext().getClient();
        if(myTeam != null && event.containsKey("spire_team_"+myTeam.name+"_locked")) {
            if(myTeam.healthLink) {
                //no death link.. just.. no
                DeathLink.setDeathLinkEnabled(false);
                client.dataStorageSetNotify(Arrays.asList("spire_team_" + myTeam.name + "_hp", "spire_team_" + myTeam.name + "_maxhp"));
                client.dataStorageGet(Arrays.asList("spire_team_" + myTeam.name + "_maxhp", "spire_team_" + myTeam.name + "_hp"));
            }
            if(myTeam.goldLink) {
                client.dataStorageSetNotify(Collections.singletonList("spire_team_" + myTeam.name + "_gold"));
                client.dataStorageGet(Collections.singletonList("spire_team_" + myTeam.name + "_gold"));
            }
        }
    }

    public static boolean sendDamageLink(int damageAmount) {
        if (myTeam == null || !myTeam.healthLink)
            return false;

        SetPacket addTeamPacket = new SetPacket("spire_team_" + myTeam.name + "_hp", 0);
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, -damageAmount);
        addTeamPacket.want_reply = true;
        APContext.getContext().getClient().dataStorageSet(addTeamPacket);
        return true;
    }

    public static boolean sendGoldLink(int goldAmount) {
        if (myTeam == null || !myTeam.healthLink)
            return false;

        SetPacket addTeamPacket = new SetPacket("spire_team_" + myTeam.name + "_gold", 0);
        addTeamPacket.addDataStorageOperation(SetPacket.Operation.ADD, goldAmount);
        addTeamPacket.want_reply = true;
        APContext.getContext().getClient().dataStorageSet(addTeamPacket);
        return true;
    }

    public static boolean sendMaxHPLink(int hpChange) {
        if (myTeam == null || !myTeam.healthLink)
            return false;

        if (hpChange > 0){
            sendDamageLink(-hpChange);
        }
        if (hpChange < 0) {
            if (AbstractDungeon.player.maxHealth <= 1) {
                hpChange = -AbstractDungeon.player.maxHealth + 1;
            }

            if (AbstractDungeon.player.currentHealth > AbstractDungeon.player.maxHealth + hpChange) {
                sendDamageLink( AbstractDungeon.player.currentHealth - (AbstractDungeon.player.maxHealth + hpChange));
            }
        }

        SetPacket maxHPpacket = new SetPacket("spire_team_" + myTeam.name + "_maxhp", 0);
        maxHPpacket.addDataStorageOperation(SetPacket.Operation.ADD, hpChange);
        maxHPpacket.want_reply = true;
        APContext.getContext().getClient().dataStorageSet(maxHPpacket);
        return true;
    }

    public static void checkIfDead() {
        if(!CardCrawlGame.isInARun())
            return;
        if (AbstractDungeon.player.currentHealth <= 0 && !AbstractDungeon.player.isDead) {
            AbstractDungeon.player.currentHealth = 0;
            AbstractDungeon.player.isDead = true;
            AbstractDungeon.deathScreen = new DeathScreen(null);
            AbstractDungeon.screen = AbstractDungeon.CurrentScreen.DEATH;
        }

    }
}
