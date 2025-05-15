package ArchipelagoMW.game.locations;

import ArchipelagoMW.client.APContext;
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
    private static final Logger logger = LogManager.getLogger(LocationTracker.class.getName());

    private final LocationContainer cardDrawLocations = new LocationContainer();
    private final LocationContainer rareDrawLocations = new LocationContainer();
    private final LocationContainer relicLocations = new LocationContainer();
    private final LocationContainer bossRelicLocations = new LocationContainer();
    private final CampfireLocations campfireLocations = new CampfireLocations();

    private final Map<Long, NetworkItem> scoutedLocations = new HashMap<>();

    private boolean cardDraw;
    private long currentOffset = -1;
    private final List<Long> extraOffsets = new ArrayList<>();

    public void reset() {
        getBossRelicLocations().index = 0;
        getCardDrawLocations().index = 0;
        getRareDrawLocations().index = 0;
        getRelicLocations().index = 0;
        cardDraw = false;
    }

    public void loadFromSave(int cdIndex, int rdIndex, int relicIndex)
    {
        getCardDrawLocations().index = cdIndex;
        getRareDrawLocations().index = rdIndex;
        getRelicLocations().index = relicIndex;
    }

    public void initialize(long charOffset, List<Long> extras)
    {
        currentOffset = charOffset;
        extraOffsets.clear();
        extraOffsets.addAll(extras);
        reset();
        logger.info("Intializing LocationTracker with {} and extras {}", charOffset, extraOffsets);

        getCardDrawLocations().initialize(101L, 15L, charOffset);
        getCampfireLocations().initialize(121L, 6L, charOffset);
        getCampfireLocations().loadFromNetwork();
        getRareDrawLocations().initialize(131L, 2L, charOffset);
        getRelicLocations().initialize(141L, 10L, charOffset);
        getBossRelicLocations().initialize(161L, 2L, charOffset);
    }

    /**
     * @return a {@link NetworkItem} if this card draw was sent to AP,
     * null if you should keep this card draw locally.
     */
    public NetworkItem sendCardDraw(RewardItem reward) {
        boolean isBoss = ReflectionHacks.getPrivate(reward, RewardItem.class, "isBoss");
        if (isBoss) {
            if(getRareDrawLocations().isExhausted()) {
                return null;
            }
            long locationID = getRareDrawLocations().getNext();
            APContext.getContext().getClient().checkLocations(getLocationIDs(locationID));
            NetworkItem item = scoutedLocations.get(locationID);
            if (item == null) {
                NetworkItem networkItem = new NetworkItem();
                networkItem.itemName = "Rare Card Draw " + (3 - getRareDrawLocations().locations.size());
                networkItem.playerName = "";
                return networkItem;
            }
            return item;
        }

        cardDraw = !cardDraw;
        if (cardDraw) {
            if(getCardDrawLocations().isExhausted())
            {
                return null;
            }
            long locationID = getCardDrawLocations().getNext();
            Archipelago.logger.info("Sending Location Id {}", locationID);
            APContext.getContext().getClient().checkLocations(getLocationIDs(locationID));
            NetworkItem item = scoutedLocations.get(locationID);
//            Archipelago.logger.info("Got Network item {}", item.itemName);
            if (item == null) {
                NetworkItem networkItem = new NetworkItem();
                networkItem.itemName = "Card Draw " + (15 - getCardDrawLocations().locations.size());
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
    public NetworkItem sendRelic() {
        if(getRelicLocations().isExhausted())
        {
            return null;
        }

        long locationID = getRelicLocations().getNext();

        APContext.getContext().getClient().checkLocations(getLocationIDs(locationID));
        NetworkItem item = scoutedLocations.get(locationID);
        if (item == null) {
            NetworkItem networkItem = new NetworkItem();
            networkItem.itemName = "Relic " + (10 - getRelicLocations().locations.size());
            networkItem.playerName = "";
            return networkItem;
        }
        return item;
    }

    /**
     * sends the next boss relic location to AP
     */
    public NetworkItem sendBossRelic(int act) {
        logger.info("Going to send relic from act " + act);
        long locationID;
        try {
            locationID = getBossRelicLocations().locations.get(act - 1);
        } catch (IndexOutOfBoundsException e) {
            logger.info("Index out of bounds! Tried to access bossRelicLocation position {}", act - 1);
            logger.info("while the length is {}", getBossRelicLocations().locations.size());
            return null;
        }
        APContext.getContext().getClient().checkLocations(getLocationIDs(locationID));
        NetworkItem item = scoutedLocations.get(locationID);
        if (item == null) {
            NetworkItem networkItem = new NetworkItem();
            networkItem.itemName = "Boss Relic " + act;
            networkItem.playerName = "";
            return networkItem;
        }
        return item;
    }

    public void sendCampfireCheck(long locationId)
    {
        getCampfireLocations().sendCheck(locationId);
    }

    public void sendFloorCheck(int floor)
    {
        if(APContext.getContext().getSlotData().includeFloorChecks != 0) {
            logger.info("Sending floor check for floor {} and id {}", floor, floor + (200L * currentOffset));
            APClient.client.checkLocations(getLocationIDs(floor + (200L * currentOffset)));
        }
    }

    private List<Long> getLocationIDs(long locationID)
    {
        List<Long> result = new ArrayList<>(extraOffsets.size() + 1);
        result.add(locationID);

        for(Long extra : extraOffsets)
        {
            result.add(locationID - (currentOffset * 200L) + (extra * 200L));
        }
        logger.info("Sending location ids: {}", result);
        return result;
    }

    public void endOfTheRoad() {
        List<Long> allLocations = new ArrayList<>() ;
        allLocations.addAll(getCardDrawLocations().locations);
        allLocations.addAll(getRareDrawLocations().locations);
        allLocations.addAll(getRelicLocations().locations);
        allLocations.addAll(getBossRelicLocations().locations);
        allLocations.addAll(getCampfireLocations().locations.keySet());

        APContext.getContext().getLocationManager().checkLocations(allLocations);
        SaveManager.getInstance().saveString(CharacterManager.getInstance().getCurrentCharacter().chosenClass.name(), "");
    }

    public void scoutAllLocations() {
        ArrayList<Long> locations = new ArrayList<Long>();
        locations.addAll(getCardDrawLocations().locations);
        locations.addAll(getRelicLocations().locations);
        locations.addAll(getRareDrawLocations().locations);
        locations.addAll(getBossRelicLocations().locations);
        locations.addAll(getCampfireLocations().locations.keySet());
        APContext.getContext().getClient().scoutLocations(locations);
    }

    public void addToScoutedLocations(ArrayList<NetworkItem> networkItems) {
        for (NetworkItem item : networkItems) {
            scoutedLocations.put(item.locationID, item);
        }
    }

    public NetworkItem getScoutedItem(long location)
    {
        return scoutedLocations.get(location);
    }

    public LocationContainer getCardDrawLocations() {
        return cardDrawLocations;
    }

    public LocationContainer getRareDrawLocations() {
        return rareDrawLocations;
    }

    public LocationContainer getRelicLocations() {
        return relicLocations;
    }

    public LocationContainer getBossRelicLocations() {
        return bossRelicLocations;
    }

    public CampfireLocations getCampfireLocations() {
        return campfireLocations;
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
            for(Long checked : APContext.getContext().getLocationManager().getCheckedLocations())
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
            logger.info("Sending campfire check: {}", locationId);
            APContext.getContext().getClient().checkLocation(locationId);
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
