package ArchipelagoMW.game.locations;

import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.client.APClient;
import ArchipelagoMW.game.save.SaveManager;
import basemod.ReflectionHacks;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.rewards.RewardItem;
import dev.koifysh.archipelago.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocationTracker {
    public static final Logger logger = LogManager.getLogger(LocationTracker.class.getName());

    public static final LocationContainer cardDrawLocations = new LocationContainer();
    public static final LocationContainer rareDrawLocations = new LocationContainer();
    public static final LocationContainer relicLocations = new LocationContainer();
    public static final LocationContainer bossRelicLocations = new LocationContainer();
    public static final CampfireLocations campfireLocations = new CampfireLocations();

    private static final Map<Long, NetworkItem> scoutedLocations = new HashMap<>();

    private static boolean cardDraw;
    private static long currentOffset = -1;
    private static final List<Long> extraOffsets = new ArrayList<>();

    public static void reset() {
        bossRelicLocations.index = 0;
        cardDrawLocations.index = 0;
        rareDrawLocations.index = 0;
        relicLocations.index = 0;
        cardDraw = false;
    }

    public static void loadFromSave(int cdIndex, int rdIndex, int relicIndex)
    {
        cardDrawLocations.index = cdIndex;
        rareDrawLocations.index = rdIndex;
        relicLocations.index = relicIndex;
    }

    public static void initialize(long charOffset, List<Long> extras)
    {
        currentOffset = charOffset;
        extraOffsets.clear();
        extraOffsets.addAll(extras);
        reset();
        APClient.logger.info("Intializing LocationTracker with {} and extras {}", charOffset, extraOffsets);

        cardDrawLocations.initialize(101L, 15L, charOffset);
        campfireLocations.initialize(121L, 6L, charOffset);
        campfireLocations.loadFromNetwork();
        rareDrawLocations.initialize(131L, 2L, charOffset);
        relicLocations.initialize(141L, 10L, charOffset);
        bossRelicLocations.initialize(161L, 2L, charOffset);
    }

    /**
     * @return a {@link NetworkItem} if this card draw was sent to AP,
     * null if you should keep this card draw locally.
     */
    public static NetworkItem sendCardDraw(RewardItem reward) {
        boolean isBoss = ReflectionHacks.getPrivate(reward, RewardItem.class, "isBoss");
        if (isBoss) {
            if(rareDrawLocations.isExhausted()) {
                return null;
            }
            long locationID = rareDrawLocations.getNext();
            APClient.apClient.checkLocations(getLocationIDs(locationID));
            NetworkItem item = scoutedLocations.get(locationID);
            if (item == null) {
                NetworkItem networkItem = new NetworkItem();
                networkItem.itemName = "Rare Card Draw " + (3 - rareDrawLocations.locations.size());
                networkItem.playerName = "";
                return networkItem;
            }
            return item;
        }

        cardDraw = !cardDraw;
        if (cardDraw) {
            if(cardDrawLocations.isExhausted())
            {
                return null;
            }
            long locationID = cardDrawLocations.getNext();
            Archipelago.logger.info("Sending Location Id {}", locationID);
            APClient.apClient.checkLocations(getLocationIDs(locationID));
            NetworkItem item = scoutedLocations.get(locationID);
//            Archipelago.logger.info("Got Network item {}", item.itemName);
            if (item == null) {
                NetworkItem networkItem = new NetworkItem();
                networkItem.itemName = "Card Draw " + (15 - cardDrawLocations.locations.size());
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
        if(relicLocations.isExhausted())
        {
            return null;
        }

        long locationID = relicLocations.getNext();

        APClient.apClient.checkLocations(getLocationIDs(locationID));
        NetworkItem item = scoutedLocations.get(locationID);
        if (item == null) {
            NetworkItem networkItem = new NetworkItem();
            networkItem.itemName = "Relic " + (10 - relicLocations.locations.size());
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
            locationID = bossRelicLocations.locations.get(act - 1);
        } catch (IndexOutOfBoundsException e) {
            logger.info("Index out of bounds! Tried to access bossRelicLocation position {}", act - 1);
            logger.info("while the length is {}", bossRelicLocations.locations.size());
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

    public static void sendCampfireCheck(long locationId)
    {
        campfireLocations.sendCheck(locationId);
    }

    public static void sendFloorCheck(int floor)
    {
        if(APClient.slotData.includeFloorChecks != 0) {
            APClient.logger.info("Sending floor check for floor {} and id {}", floor, floor + (200L * currentOffset));
            APClient.client.checkLocations(getLocationIDs(floor + (200L * currentOffset)));
        }
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

    public static void endOfTheRoad() {
        List<Long> allLocations = new ArrayList<>() ;
        allLocations.addAll(cardDrawLocations.locations);
        allLocations.addAll(rareDrawLocations.locations);
        allLocations.addAll(relicLocations.locations);
        allLocations.addAll(bossRelicLocations.locations);
        allLocations.addAll(campfireLocations.locations.keySet());

        APClient.apClient.getLocationManager().checkLocations(allLocations);
        SaveManager.getInstance().saveString(CharacterManager.getInstance().getCurrentCharacter().chosenClass.name(), "");
    }

    public static void scoutAllLocations() {
        ArrayList<Long> locations = new ArrayList<Long>();
        locations.addAll(cardDrawLocations.locations);
        locations.addAll(relicLocations.locations);
        locations.addAll(rareDrawLocations.locations);
        locations.addAll(bossRelicLocations.locations);
        locations.addAll(campfireLocations.locations.keySet());
        APClient.apClient.scoutLocations(locations);
    }

    public static void addToScoutedLocations(ArrayList<NetworkItem> networkItems) {
        for (NetworkItem item : networkItems) {
            scoutedLocations.put(item.locationID, item);
        }
    }

    public static NetworkItem getScoutedItem(long location)
    {
        return scoutedLocations.get(location);
    }

    public static class LocationContainer {
        private final List<Long> locations = new ArrayList<>();
        private int index = 0;

        public void initialize(long startNum, long amount, long charOffset)
        {
            locations.clear();
            index = 0;
            for(long i = startNum; i < startNum + amount; i++)
            {
                locations.add(i + (200L * charOffset));
            }
        }

        public int getIndex()
        {
            return index;
        }

        public boolean isExhausted()
        {
            return index >= locations.size();
        }

        private Long getNext()
        {
            return locations.get(index++);
        }
    }

    public static class LocationContainerMap
    {
        protected final Map<Long, Boolean> locations = new ConcurrentHashMap<>();
        protected long charOffset;

        public void initialize(long startNum, long amount, long charOffset)
        {
            locations.clear();
            for(long i = startNum; i < startNum + amount; i++)
            {
                locations.put(i + (200L * charOffset), false);
            }
            this.charOffset = charOffset;
        }

        public void loadFromNetwork()
        {
            for(Long checked : APClient.apClient.getLocationManager().getCheckedLocations())
            {
                locations.computeIfPresent(checked, (__, ___) -> true);
            }
        }

        public int getNumberChecked()
        {
            return locations.values().stream().mapToInt(b -> b ? 1 : 0).sum();
        }

        void sendCheck(long locationId)
        {
            APClient.logger.info("Sending campfire check: {}", locationId);
            APClient.client.checkLocation(locationId);
            locations.put(locationId, true);
        }

        public void initializeFromSave(JsonObject jo)
        {
            locations.clear();
            for(Map.Entry<String, JsonElement> entry : jo.entrySet())
            {
                locations.put(Long.parseLong(entry.getKey()), entry.getValue().getAsBoolean());
            }
        }

        public Map<String, Boolean> getData()
        {
            Map<String, Boolean> ret = new HashMap<>();
            for(Map.Entry<Long, Boolean> loc : locations.entrySet())
            {
                ret.put(loc.getKey().toString(), loc.getValue());
            }
            return ret;
        }
    }

    public static class CampfireLocations extends LocationContainerMap
    {

        public List<Long> getLocationsForAct(int act)
        {
            List<Long> ret = new ArrayList<>();
            long first = 121L + (charOffset * 200L) + Math.min((act-1)*2L, 4L);
            long second = 122L + (charOffset * 200L) + Math.min((act-1)*2L, 4L);
            if(!locations.get(first))
            {
                ret.add(first);
            }
            if(!locations.get(second))
            {
                ret.add(second);
            }
            return ret;
        }
    }

}
