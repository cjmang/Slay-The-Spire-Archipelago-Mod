package ArchipelagoMW;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Net;
import com.megacrit.cardcrawl.rewards.RewardItem;
import gg.archipelago.APClient.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationTracker {
    public static final Logger logger = LogManager.getLogger(LocationTracker.class.getName());

    private static ArrayList<Long> cardDrawLocations;

    private static ArrayList<Long> relicLocations;

    private static ArrayList<Long> rareCardLocations;

    private static ArrayList<Long> bossRelicLocations;

    public static boolean cardDraw;

    public static HashMap<Long, NetworkItem> scoutedLocations = new HashMap<>();

    public static void reset() {
        cardDrawLocations = new ArrayList<Long>() {{
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

        relicLocations = new ArrayList<Long>() {{
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

        rareCardLocations = new ArrayList<Long>() {{
            add(21001L);
            add(21002L);
            add(21003L);
        }};

        bossRelicLocations = new ArrayList<Long>() {{
            add(22001L);
            add(22002L);
            add(22003L);
        }};
    }

    /**
     * @return true if this card draw was sent to AP,
     * false if you should keep this card draw locally.
     */
    static public NetworkItem sendCardDraw(RewardItem reward) {
        boolean isBoss = ReflectionHacks.getPrivate(reward,RewardItem.class,"isBoss");
        if (isBoss) {
            if (rareCardLocations.isEmpty())
                return null;
            long locationID = rareCardLocations.remove(0);
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
            if (cardDrawLocations.isEmpty())
                return null;
            long locationID = cardDrawLocations.remove(0);
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
        if (relicLocations.isEmpty())
            return null;

        long locationID = relicLocations.remove(0);
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
