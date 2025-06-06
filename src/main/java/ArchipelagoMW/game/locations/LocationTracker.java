package ArchipelagoMW.game.locations;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.client.config.SlotData;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.client.APClient;
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

    public static final long CARD_DRAW_NUM = 13L;

    private final LocationContainer cardDrawLocations = new LocationContainer();
    private final LocationContainer rareDrawLocations = new LocationContainer();
    private final LocationContainer relicLocations = new LocationContainer();
    private final LocationContainer bossRelicLocations = new LocationContainer();
    private final CampfireLocations campfireLocations = new CampfireLocations();
    private final LocationContainerMap shopLocations = new LocationContainerMap();
    private int floorIndex = 0;

    private final Map<Long, NetworkItem> scoutedLocations = new HashMap<>();

    private boolean cardDraw;
    private long currentOffset = -1;
    private final List<Long> extraOffsets = new ArrayList<>();

    public void reset() {
        bossRelicLocations.index = 0;
        cardDrawLocations.index = 0;
        rareDrawLocations.index = 0;
        relicLocations.index = 0;
        floorIndex = 0;
        cardDraw = false;
    }

    public void loadFromSave(int cdIndex, int rdIndex, int relicIndex, int bossRelicLocation, int floorIndex, boolean cardDraw)
    {
        cardDrawLocations.index = cdIndex;
        rareDrawLocations.index = rdIndex;
        relicLocations.index = relicIndex;
        bossRelicLocations.index = bossRelicLocation;
        this.floorIndex = floorIndex;
        this.cardDraw = cardDraw;
    }

    public void initialize(long charOffset, List<Long> extras)
    {
        currentOffset = charOffset;
        extraOffsets.clear();
        extraOffsets.addAll(extras);
        reset();
        logger.info("Intializing LocationTracker with {} and extras {}", charOffset, extraOffsets);

        getCardDrawLocations().initialize(101L, CARD_DRAW_NUM, charOffset);
        getCampfireLocations().initialize(121L, 6L, charOffset);
        getCampfireLocations().loadFromNetwork();
        getRareDrawLocations().initialize(131L, 2L, charOffset);
        getRelicLocations().initialize(141L, 10L, charOffset);
        getBossRelicLocations().initialize(161L, 2L, charOffset);
    }

    public int getFloorIndex()
    {
        return floorIndex;
    }

    public void scoutShop(long totalSlots)
    {
        shopLocations.initialize(164L, totalSlots, currentOffset);
        shopLocations.loadFromNetwork();
        APContext.getContext().getClient().scoutLocations(new ArrayList<>(shopLocations.locations.keySet()));
    }

    public void sendPressStart(CharacterConfig config)
    {
        if(config.locked) {
            APContext.getContext().getClient().checkLocation(163L + (200L * currentOffset));
        }
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
                networkItem.itemName = "Card Draw " + (CARD_DRAW_NUM - getCardDrawLocations().locations.size());
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
            locationID = bossRelicLocations.locations.get(act - 1);
        } catch (IndexOutOfBoundsException e) {
            logger.info("Index out of bounds! Tried to access bossRelicLocation position {}", act - 1);
            logger.info("while the length is {}", getBossRelicLocations().locations.size());
            return null;
        }
        if(act > bossRelicLocations.index)
        {
            bossRelicLocations.index = act;
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
        getCampfireLocations().sendCheck(locationId, extraOffsets);
    }

    public void sendFloorCheck(int floor)
    {
        if(APContext.getContext().getSlotData().includeFloorChecks != 0) {
            logger.info("Sending floor check for floor {} and id {}", floor, floor + (200L * currentOffset));
            APClient.client.checkLocations(getLocationIDs(floor + (200L * currentOffset)));
            if(floor > floorIndex)
            {
                floorIndex = floor;
            }
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
        APContext apContext = APContext.getContext();
        SlotData slotData = apContext.getSlotData();
        List<Long> allLocations = new ArrayList<>() ;
        allLocations.addAll(getCardDrawLocations().locations);
        allLocations.addAll(getRareDrawLocations().locations);
        allLocations.addAll(getRelicLocations().locations);
        allLocations.addAll(getBossRelicLocations().locations);
        if(slotData.campfireSanity != 0) {
            allLocations.addAll(getCampfireLocations().locations.keySet());
        }
        if(slotData.shopSanity != 0) {
            allLocations.addAll(shopLocations.locations.keySet());
        }
        apContext.getLocationManager().checkLocations(allLocations);
        apContext.getSaveManager().saveString(apContext.getCharacterManager().getCurrentCharacter().chosenClass.name(), "");
    }

    public void scoutAllLocations(SlotData data) {
        ArrayList<Long> locations = new ArrayList<Long>();
        locations.addAll(getCardDrawLocations().locations);
        locations.addAll(getRelicLocations().locations);
        locations.addAll(getRareDrawLocations().locations);
        locations.addAll(getBossRelicLocations().locations);
        if(data.campfireSanity != 0) {
            locations.addAll(getCampfireLocations().locations.keySet());
        }
        if(data.shopSanity != 0) {
            locations.addAll(shopLocations.locations.keySet());
        }
        logger.info("Scouting shop locations: {}", shopLocations.locations.keySet());
        APContext.getContext().getClient().scoutLocations(locations);
    }

    public void addToScoutedLocations(ArrayList<NetworkItem> networkItems) {
        for (NetworkItem item : networkItems) {
            long base_id = item.locationID % 200L;
            if(base_id >= 164 && base_id <= 180)
            {
                logger.info("Got scount for location {} has item {}", item.locationName, item.itemName);
            }
            scoutedLocations.put(item.locationID, item);
        }
    }

    public NetworkItem getScoutedItem(long location)
    {
        return scoutedLocations.get(location);
    }

    public NetworkItem getScoutedItemOrDefault(long location)
    {
        NetworkItem item = scoutedLocations.get(location);
        if(item == null)
        {
            item = new NetworkItem();
            item.locationID = location;
            item.itemName = "An AP Item";
            item.playerName = "Some Player";
        }
        return item;
    }


    public LocationContainer getCardDrawLocations() {
        return cardDrawLocations;
    }

    public boolean getCardDrawToggle()
    {
        return cardDraw;
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

        void sendCheck(long locationId, List<Long> extras)
        {
            logger.info("Sending campfire check: {}", locationId);
            List<Long> locationIds = new ArrayList<>();
            locationIds.add(locationId);
            for(long extra : extras)
            {
                locationIds.add(locationId - (200L * charOffset) + (200L * extra));
            }
            APContext.getContext().getClient().checkLocations(locationIds);
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
            int max = Math.min(act,3);
            for(int i = max - 1; i >= 0; i--) {
                long rest = 121L + (charOffset * 200L) + (i * 2L);
                long smith = 122L + (charOffset * 200L) + (i * 2L);
                if (!locations.getOrDefault(rest, true)) {
                    ret.add(rest);
                }
                if (!locations.getOrDefault(smith, true)) {
                    ret.add(smith);
                }
            }
            return ret;
        }
    }

}
