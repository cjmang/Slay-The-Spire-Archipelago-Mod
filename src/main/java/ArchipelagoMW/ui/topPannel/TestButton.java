package ArchipelagoMW.ui.topPannel;

import ArchipelagoMW.APTextures;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.teams.PlayerInfo;
import ArchipelagoMW.teams.PlayerManager;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.hud.PlayerPanel;
import ArchipelagoMW.ui.hud.SideBar;
import basemod.TopPanelItem;
import gg.archipelago.client.events.ArchipelagoEventListener;
import gg.archipelago.client.events.SetReplyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestButton extends TopPanelItem {


    public static final Logger logger = LogManager.getLogger(TestButton.class.getName());

    public static final String ID = Archipelago.makeID("TestButton");

    public TestButton() {
        super(APTextures.AP_ICON, ID);
    }

    @Override
    protected void onClick() {
        Archipelago.sideBar = new SideBar();
        TeamManager.myTeam = null;
        PlayerManager.players.clear();
        PlayerManager.sendUpdate();
        PlayerInfo test1 = new PlayerInfo("test1",50,20,10,43);
        PlayerInfo test2 = new PlayerInfo("test2",50,20,11,43);
        PlayerInfo test3 = new PlayerInfo("test3",50,20,12,43);
        PlayerInfo test4 = new PlayerInfo("test4",50,20,13,43);
        PlayerInfo test5 = new PlayerInfo("test5",50,20,14,43);
        test1.team = "blue";
        test2.team = "red";
        test3.team = "blue";
        test4.team = "red";
        Archipelago.sideBar.playerPanels.add(new PlayerPanel(test1));
        Archipelago.sideBar.playerPanels.add(new PlayerPanel(test2));
        Archipelago.sideBar.playerPanels.add(new PlayerPanel(test3));
        Archipelago.sideBar.playerPanels.add(new PlayerPanel(test4));
        Archipelago.sideBar.playerPanels.add(new PlayerPanel(test5));
        Archipelago.sideBar.sortPlayers();
    }

    @ArchipelagoEventListener
    public void setNotify(SetReplyEvent event) {

    }
}

