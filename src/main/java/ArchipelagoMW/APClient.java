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
import java.security.*;

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
//            apClient.connect(address);
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
        try(InputStream is = APClient.class.getResourceAsStream("/cacerts"))
        {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(is, "changeit".toCharArray());
            factory.init(keystore);
        }
        TrustManager[] trust = factory.getTrustManagers();

        context.init(null, trust, null);
        return context.getSocketFactory();
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
