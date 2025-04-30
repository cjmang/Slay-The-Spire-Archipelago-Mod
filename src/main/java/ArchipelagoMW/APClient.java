package ArchipelagoMW;

import ArchipelagoMW.apEvents.ConnectionResult;
import ArchipelagoMW.apEvents.DataStorageGet;
import ArchipelagoMW.apEvents.LocationInfo;
import ArchipelagoMW.apEvents.ReceiveItem;
import ArchipelagoMW.teams.PlayerManager;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import dev.koifysh.archipelago.Client;
import dev.koifysh.archipelago.Print.APPrint;
import dev.koifysh.archipelago.flags.ItemsHandling;
import dev.koifysh.archipelago.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;

public class APClient extends Client {

    public static final Logger logger = LogManager.getLogger(APClient.class.getName());

    public static APClient apClient;

    public static SlotData slotData;

    public static void newConnection(String address, String slotName, String password) {
        if (apClient != null) {
            apClient.close();
        }
        apClient = new APClient();
        apClient.setPassword(password);
        apClient.setName(slotName);
        apClient.setItemsHandlingFlags(ItemsHandling.SEND_ITEMS + ItemsHandling.SEND_OWN_ITEMS + ItemsHandling.SEND_STARTING_INVENTORY);

        apClient.getEventManager().registerListener(new ConnectionResult());
        apClient.getEventManager().registerListener(new LocationInfo());
        apClient.getEventManager().registerListener(new ReceiveItem());
        apClient.getEventManager().registerListener(new DataStorageGet());
        apClient.getEventManager().registerListener(new PlayerManager());
        apClient.getEventManager().registerListener(new TeamManager());
        //apClient.getEventManager().registerListener(new TestButton());
        try {
            apClient.connect(address);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private APClient() {
        super();
        this.setGame("Slay the Spire");
    }

    @Override
    public void onError(Exception e) {
        ConnectionPanel.connectionResultText = "Server Error NL " + e.getMessage();
    }

    @Override
    public void onClose(String message, int i) {
        ConnectionPanel.connectionResultText = "Connection Closed NL " + message;
    }
}
