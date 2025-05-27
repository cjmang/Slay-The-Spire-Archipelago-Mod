package ArchipelagoMW.client;

import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.client.config.SlotData;
import ArchipelagoMW.client.apEvents.ConnectionResult;
import ArchipelagoMW.game.TalkQueue;
import ArchipelagoMW.game.teams.PlayerManager;
import ArchipelagoMW.game.teams.TeamManager;
import ArchipelagoMW.game.items.ui.ArchipelagoRewardScreen;
import ArchipelagoMW.game.connect.ui.connection.ConnectionPanel;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import dev.koifysh.archipelago.Client;
import dev.koifysh.archipelago.events.*;
import dev.koifysh.archipelago.flags.ItemsHandling;
import dev.koifysh.archipelago.network.client.SetPacket;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class APClient extends Client {

    public static final Logger logger = LogManager.getLogger(APClient.class.getName());

    private final APContext ctx;
    private SlotData slotData;

    private DataStorageWrapper dataStorageWrapper;
    private final RecieveItemHandler recieveItemHandler = new RecieveItemHandler();

    public static void newConnection(APContext context, String address, String slotName, String password) {
        APClient apClient = context.getClient();
        if (apClient != null) {
            TalkQueue.AbstractDungeonPatch.talkQueue.clear();
            apClient.close();
        }
        apClient = new APClient(context);
        context.setClient(apClient);
        apClient.dataStorageWrapper = new DataStorageWrapper(apClient);
        apClient.setPassword(password);
        apClient.setName(slotName);
        apClient.setItemsHandlingFlags(ItemsHandling.SEND_ITEMS + ItemsHandling.SEND_OWN_ITEMS + ItemsHandling.SEND_STARTING_INVENTORY);

        apClient.getEventManager().registerListener(new ConnectionResult());
        apClient.getEventManager().registerListener(EventHandlers.class);
        apClient.getEventManager().registerListener(apClient.dataStorageWrapper);
        apClient.getEventManager().registerListener(new PlayerManager());
        apClient.getEventManager().registerListener(new TeamManager());
        apClient.getEventManager().registerListener(apClient.recieveItemHandler);
//        apClient.getEventManager().registerListener(new OnJSONMessage());
        //apClient.getEventManager().registerListener(new TestButton());

        try {
            URIBuilder builder = new URIBuilder(!address.contains("//") ? "//" + address : address);
            if (builder.getPort() == -1) {
                builder.setPort(38281);
            }
            SSLSocketFactory socketFactory = apClient.getSocketFactory();
            if (builder.getScheme() == null) {
                builder.setScheme("wss");
                apClient.connect(builder.build(), true, socketFactory);
            } else if ("wss".equals(builder.getScheme()) ){
                apClient.connect(builder.build(), false, socketFactory);
            } else {

                apClient.connect(builder.build() );
            }
        } catch (URISyntaxException | IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private SSLSocketFactory getSocketFactory() throws IOException, GeneralSecurityException
    {
        // On linux, the cacerts is missing the Let's Encrypt root CA.  So we're loading the cacerts
        // from the Windows StS release always.
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        try(InputStream is = ArchipelagoMW.APClient.class.getResourceAsStream("/cacerts"))
        {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(is, "changeit".toCharArray());
            factory.init(keystore);
        }
        TrustManager[] trust = factory.getTrustManagers();

        context.init(null, trust, null);
        return context.getSocketFactory();
    }

    private APClient(APContext ctx) {
        super();
        this.setGame("Slay the Spire");
        this.ctx = ctx;
    }


    public SlotData getSlotData() {
        return slotData;
    }

    public void setSlotData(SlotData slotData)
    {
        this.slotData = slotData;
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

    public static class RecieveItemHandler
    {

        @ArchipelagoEventListener
        public void onReceiveItem(ReceiveItemEvent event)
        {
            CharacterManager charManager = APContext.getContext().getCharacterManager();
            CharacterConfig character = charManager.getCurrentCharacterConfig();
            if(character == null || CardCrawlGame.GameMode.GAMEPLAY != CardCrawlGame.mode)
            {
                return;
            }
            if(event.getIndex() > ArchipelagoRewardScreen.getReceivedItemsIndex()) {

                if(charManager.isItemIDForCurrentCharacter(event.getItemID()))
                {
                    APContext.getContext().getItemTracker().addItem(event.getItemID());
                    // only increase counter, actual items get fetched when you open the reward screen.
                    ArchipelagoRewardScreen.rewardsQueued += 1;
                }
            }
        }
    }

    public static class OnJSONMessage
    {

        @ArchipelagoEventListener
        public void onJSONMessage(PrintJSONEvent event)
        {
            if(CardCrawlGame.GameMode.GAMEPLAY == CardCrawlGame.mode)
            {
                switch(event.type)
                {
                    case Chat:
                    case ItemSend:
                    case ItemCheat:
                    case Goal:
                    case Hint:
                        TalkQueue.AbstractDungeonPatch.talkQueue.add(event);
                }
            }
        }
    }

    public static class EventHandlers
    {
        @ArchipelagoEventListener
        public static void onLocationInfo(LocationInfoEvent event)
        {
            APClient.logger.info("Got Location Scouts");
            APContext.getContext().getLocationTracker().addToScoutedLocations(event.locations);
        }
    }

}
