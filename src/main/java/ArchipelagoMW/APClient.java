package ArchipelagoMW;

import ArchipelagoMW.apEvents.ConnectionResult;
import ArchipelagoMW.apEvents.LocationInfo;
import ArchipelagoMW.apEvents.ReceiveItem;
import ArchipelagoMW.teams.PlayerManager;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.connection.ConnectionPanel;
import dev.koifysh.archipelago.Client;
import dev.koifysh.archipelago.events.RetrievedEvent;
import dev.koifysh.archipelago.events.SetReplyEvent;
import dev.koifysh.archipelago.flags.ItemsHandling;
import dev.koifysh.archipelago.network.client.SetPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class APClient extends Client {

    public static final Logger logger = LogManager.getLogger(APClient.class.getName());

    public static APClient apClient;

    public static SlotData slotData;

    public static CharacterManager charManager;

    private DataStorageWrapper dataStorageWrapper;

    public static void newConnection(String address, String slotName, String password) {
        if (apClient != null) {
            apClient.close();
        }
        apClient = new APClient();
        apClient.dataStorageWrapper = new DataStorageWrapper(apClient);
        apClient.setPassword(password);
        apClient.setName(slotName);
        apClient.setItemsHandlingFlags(ItemsHandling.SEND_ITEMS + ItemsHandling.SEND_OWN_ITEMS + ItemsHandling.SEND_STARTING_INVENTORY);

        apClient.getEventManager().registerListener(new ConnectionResult());
        apClient.getEventManager().registerListener(new LocationInfo());
        apClient.getEventManager().registerListener(new ReceiveItem());
        apClient.getEventManager().registerListener(apClient.dataStorageWrapper);
        apClient.getEventManager().registerListener(new PlayerManager());
        apClient.getEventManager().registerListener(new TeamManager());
        //apClient.getEventManager().registerListener(new TestButton());
        try {
            apClient.connect(address);
            charManager = new CharacterManager();
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

    public void asyncDSGet(Collection<String> keys, Consumer<RetrievedEvent> lambda)
    {
        dataStorageWrapper.asyncDSGet(keys, lambda);
    }

    public Future<RetrievedEvent> dataStorageGetFuture(Collection<String> keys)
    {
        return dataStorageWrapper.dataStorageGet(keys);
    }

    public Future<SetReplyEvent> dataStorageSetFuture(SetPacket packet)
    {
        return dataStorageWrapper.dataStorageSet(packet);
    }

    public void asyncDSSet(SetPacket packet, Consumer<SetReplyEvent> lambda)
    {
        dataStorageWrapper.asyncDSSet(packet, lambda);
    }

    @Override
    public void close() {
        super.close();
        dataStorageWrapper.close();
    }
}
