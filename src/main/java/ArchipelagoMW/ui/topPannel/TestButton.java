package ArchipelagoMW.ui.topPannel;

import ArchipelagoMW.APTextures;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.ui.hud.PlayerInfo;
import ArchipelagoMW.ui.hud.PlayerPanel;
import ArchipelagoMW.ui.hud.SidePanel;
import basemod.TopPanelItem;
import com.badlogic.gdx.graphics.Color;
import gg.archipelago.client.events.ArchipelagoEventListener;
import gg.archipelago.client.events.SetReplyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class TestButton extends TopPanelItem {

    static float rotateTimer;

    public static final Logger logger = LogManager.getLogger(TestButton.class.getName());

    public static final String ID = Archipelago.makeID("TestButton");

    public TestButton() {
        super(APTextures.AP_ICON, ID);
    }

    @Override
    protected void onClick() {
        //InfoUpdater.initialLoad();
        Random rand = new Random();
        byte[] array = new byte[rand.nextInt(40)+5];
        rand.nextBytes(array);
        String name = new String(array, StandardCharsets.UTF_8);
        PlayerInfo player = new PlayerInfo(name, rand.nextInt(200), rand.nextInt(50), rand.nextInt(3000));
        player.teamColor = new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat(),1f);
        SidePanel.playerPanels.add(new PlayerPanel(player));
        SidePanel.sortPlayers();
    }

    @ArchipelagoEventListener
    public void setNotify(SetReplyEvent event) {

    }
}

