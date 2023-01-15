package ArchipelagoMW;

import ArchipelagoMW.patches.SavePatch;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.rewards.RewardItem;
import gg.archipelago.client.network.client.SetPacket;
import gg.archipelago.client.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationTracker {
    public static final Logger logger = LogManager.getLogger(LocationTracker.class.getName());

    public static int cardDrawIndex = 0;
    public static final ArrayList<Long> cardDrawLocations = new ArrayList<Long>() {{
        add(19001L);
        add(19002L);
        add(19003L);
        add(19004L);
        add(19005L);
        add(19006L);
        add(19007L);
        add(19008L);
        add(19009L);
        add(19010L);
        add(19011L);
        add(19012L);
        add(19013L);
        add(19014L);
        add(19015L);
    }};

    public static int relicIndex = 0;
    public static final ArrayList<Long> relicLocations = new ArrayList<Long>() {{
        add(20001L);
        add(20002L);
        add(20003L);
        add(20004L);
        add(20005L);
        add(20006L);
        add(20007L);
        add(20008L);
        add(20009L);
        add(20010L);
    }};

    public static int rareCardIndex = 0;
    public static final ArrayList<Long> rareCardLocations = new ArrayList<Long>() {{
        add(21001L);
        add(21002L);
        add(21003L);
    }};

    public static int bossRelicIndex = 0;
    public static final ArrayList<Long> bossRelicLocations = new ArrayList<Long>() {{
        add(22001L);
        add(22002L);
        add(22003L);
    }};

    public static boolean cardDraw;

    public static HashMap<Long, NetworkItem> scoutedLocations = new HashMap<>();

    public static void reset() {
        bossRelicIndex = 0;
        cardDrawIndex = 0;
        rareCardIndex = 0;
        relicIndex = 0;
    }

    /**
     * @return a {@link NetworkItem} if this card draw was sent to AP,
     * null if you should keep this card draw locally.
     */
    static public NetworkItem sendCardDraw(RewardItem reward) {
        boolean isBoss = ReflectionHacks.getPrivate(reward, RewardItem.class, "isBoss");
        if (isBoss) {
            if (rareCardIndex >= rareCardLocations.size())
                return null;
            long locationID = rareCardLocations.get(rareCardIndex);
            rareCardIndex++;
            APClient.apClient.checkLocation(locationID);
            NetworkItem item = scoutedLocations.get(locationID);
            if (item == null) {
                NetworkItem networkItem = new NetworkItem();
                networkItem.itemName = "Rare Card Draw " + (3 - rareCardLocations.size());
                networkItem.playerName = "";
                return networkItem;
            }
            return item;
        }

        cardDraw = !cardDraw;
        if (cardDraw) {
            if (cardDrawIndex >= cardDrawLocations.size())
                return null;
            long locationID = cardDrawLocations.get(cardDrawIndex);
            cardDrawIndex++;
            APClient.apClient.checkLocation(locationID);
            NetworkItem item = scoutedLocations.get(locationID);
            if (item == null) {
                NetworkItem networkItem = new NetworkItem();
                networkItem.itemName = "Card Draw " + (15 - cardDrawLocations.size());
                networkItem.playerName = "";
                return networkItem;
            }
            return item;
        }
        return null;
    }

    /**
     * sends the next relic location to AP
     */
    static public NetworkItem sendRelic() {
        if (relicIndex >= relicLocations.size())
            return null;

        long locationID = relicLocations.get(relicIndex);
        relicIndex++;
        APClient.apClient.checkLocation(locationID);
        NetworkItem item = scoutedLocations.get(locationID);
        if (item == null) {
            NetworkItem networkItem = new NetworkItem();
            networkItem.itemName = "Relic " + (10 - relicLocations.size());
            networkItem.playerName = "";
            return networkItem;
        }
        return item;
    }

    /**
     * sends the next boss relic location to AP
     */
    static public NetworkItem sendBossRelic(int act) {
        logger.info("Going to send relic from act " + act);
        long locationID;
        try {
            locationID = bossRelicLocations.get(act - 1);
        } catch (IndexOutOfBoundsException e) {
            logger.info("Index out of bounds! Tried to access bossRelicLocation position " + (act - 1));
            logger.info("while the length is " + bossRelicLocations.size());
            return null;
        }
        APClient.apClient.checkLocation(locationID);
        NetworkItem item = scoutedLocations.get(locationID);
        if (item == null) {
            NetworkItem networkItem = new NetworkItem();
            networkItem.itemName = "Boss Relic " + act;
            networkItem.playerName = "";
            return networkItem;
        }
        return item;
    }

    public static void forfeit() {
        ArrayList<Long> allLocations = new ArrayList<Long>() {{
            addAll(cardDrawLocations);
            addAll(rareCardLocations);
            addAll(relicLocations);
            addAll(bossRelicLocations);
        }};

        APClient.apClient.getLocationManager().checkLocations(allLocations);
        SetPacket set = new SetPacket(SavePatch.AP_SAVE_STRING,"");
        set.addDataStorageOperation(SetPacket.Operation.REPLACE, "");
        APClient.apClient.dataStorageSet(set);
    }

    public static void scoutAllLocations() {
        ArrayList<Long> locations = new ArrayList<Long>() {{
            addAll(cardDrawLocations);
            addAll(relicLocations);
            addAll(rareCardLocations);
            addAll(bossRelicLocations);
        }};
        APClient.apClient.scoutLocations(locations);
    }

    public static void addToScoutedLocations(ArrayList<NetworkItem> networkItems) {
        for (NetworkItem item : networkItems) {
            scoutedLocations.put(item.locationID, item);
        }

    }
}
