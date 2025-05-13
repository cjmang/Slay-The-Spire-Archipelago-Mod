package ArchipelagoMW.game.locations;

import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.client.APClient;
import ArchipelagoMW.game.save.SaveManager;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.rewards.RewardItem;
import dev.koifysh.archipelago.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationTracker {
    public static final Logger logger = LogManager.getLogger(LocationTracker.class.getName());

    public static int cardDrawIndex = 0;
    public static final List<Long> cardDrawLocations = new ArrayList<Long>();

    public static int relicIndex = 0;
    public static final List<Long> relicLocations = new ArrayList<Long>();

    public static int rareCardIndex = 0;
    public static final List<Long> rareCardLocations = new ArrayList<Long>();

    public static int bossRelicIndex = 0;
    public static final List<Long> bossRelicLocations = new ArrayList<Long>();

    public static boolean cardDraw;

    public static final Map<Long, NetworkItem> scoutedLocations = new HashMap<>();

    private static long currentOffset = -1;
    private static final List<Long> extraOffsets = new ArrayList<>();

    public static void reset() {
        bossRelicIndex = 0;
        cardDrawIndex = 0;
        rareCardIndex = 0;
        relicIndex = 0;
        cardDraw = false;
    }


    public static void initialize(long charOffset, List<Long> extras)
    {
        currentOffset = charOffset;
        extraOffsets.clear();
        extraOffsets.addAll(extras);
        reset();
        APClient.logger.info("Intializing LocationTracker with {} and extras {}", charOffset, extraOffsets);

        cardDrawLocations.clear();
        cardDrawLocations.add(101L + charOffset * 200L);
        cardDrawLocations.add(102L + charOffset * 200L);
        cardDrawLocations.add(103L + charOffset * 200L);
        cardDrawLocations.add(104L + charOffset * 200L);
        cardDrawLocations.add(105L + charOffset * 200L);
        cardDrawLocations.add(106L + charOffset * 200L);
        cardDrawLocations.add(107L + charOffset * 200L);
        cardDrawLocations.add(108L + charOffset * 200L);
        cardDrawLocations.add(109L + charOffset * 200L);
        cardDrawLocations.add(110L + charOffset * 200L);
        cardDrawLocations.add(111L + charOffset * 200L);
        cardDrawLocations.add(112L + charOffset * 200L);
        cardDrawLocations.add(113L + charOffset * 200L);
        cardDrawLocations.add(114L + charOffset * 200L);
        cardDrawLocations.add(115L + charOffset * 200L);

        relicLocations.clear();
        relicLocations.add(141L + charOffset * 200L);
        relicLocations.add(142L + charOffset * 200L);
        relicLocations.add(143L + charOffset * 200L);
        relicLocations.add(144L + charOffset * 200L);
        relicLocations.add(145L + charOffset * 200L);
        relicLocations.add(146L + charOffset * 200L);
        relicLocations.add(147L + charOffset * 200L);
        relicLocations.add(148L + charOffset * 200L);
        relicLocations.add(149L + charOffset * 200L);
        relicLocations.add(140L + charOffset * 200L);

        rareCardLocations.clear();
        rareCardLocations.add(131L + charOffset * 200L);
        rareCardLocations.add(132L + charOffset * 200L);

        bossRelicLocations.clear();
        bossRelicLocations.add(161L + charOffset * 200L);
        bossRelicLocations.add(162L + charOffset * 200L);
    }

    /**
     * @return a {@link NetworkItem} if this card draw was sent to AP,
     * null if you should keep this card draw locally.
     */
    public static NetworkItem sendCardDraw(RewardItem reward) {
        boolean isBoss = ReflectionHacks.getPrivate(reward, RewardItem.class, "isBoss");
        if (isBoss) {
            if (rareCardIndex >= rareCardLocations.size())
                return null;
            long locationID = rareCardLocations.get(rareCardIndex);
            rareCardIndex++;
            APClient.apClient.checkLocations(getLocationIDs(locationID));
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
            Archipelago.logger.info("Sending Location Id {}", locationID);
            APClient.apClient.checkLocations(getLocationIDs(locationID));
            NetworkItem item = scoutedLocations.get(locationID);
//            Archipelago.logger.info("Got Network item {}", item.itemName);
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
    public static NetworkItem sendRelic() {
        if (relicIndex >= relicLocations.size())
            return null;

        long locationID = relicLocations.get(relicIndex);
        relicIndex++;
        APClient.apClient.checkLocations(getLocationIDs(locationID));
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
    public static NetworkItem sendBossRelic(int act) {
        logger.info("Going to send relic from act " + act);
        long locationID;
        try {
            locationID = bossRelicLocations.get(act - 1);
        } catch (IndexOutOfBoundsException e) {
            logger.info("Index out of bounds! Tried to access bossRelicLocation position {}", act - 1);
            logger.info("while the length is {}", bossRelicLocations.size());
            return null;
        }
        APClient.apClient.checkLocations(getLocationIDs(locationID));
        NetworkItem item = scoutedLocations.get(locationID);
        if (item == null) {
            NetworkItem networkItem = new NetworkItem();
            networkItem.itemName = "Boss Relic " + act;
            networkItem.playerName = "";
            return networkItem;
        }
        return item;
    }

    private static List<Long> getLocationIDs(long locationID)
    {
        List<Long> result = new ArrayList<>(extraOffsets.size() + 1);
        result.add(locationID);

        for(Long extra : extraOffsets)
        {
            result.add(locationID - (currentOffset * 200L) + (extra * 200L));
        }
        APClient.logger.info("Sending location ids: {}", result);
        return result;
    }

    public static void sendFloorCheck(int floor)
    {
        APClient.logger.info("Sending floor check for floor {} and id {}", floor, floor + (200L * currentOffset));
        APClient.client.checkLocations(getLocationIDs(floor + (200L * currentOffset)));
    }

    public static void endOfTheRoad() {
        List<Long> allLocations = new ArrayList<>() ;
        allLocations.addAll(cardDrawLocations);
        allLocations.addAll(rareCardLocations);
        allLocations.addAll(relicLocations);
        allLocations.addAll(bossRelicLocations);

        APClient.apClient.getLocationManager().checkLocations(allLocations);
        SaveManager.getInstance().saveString(CharacterManager.getInstance().getCurrentCharacter().chosenClass.name(), "");
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
