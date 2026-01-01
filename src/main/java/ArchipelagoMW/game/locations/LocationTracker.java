package ArchipelagoMW.game.locations;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.client.config.SlotData;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.client.APClient;
import basemod.ReflectionHacks;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.*;
import io.github.archipelagomw.parts.NetworkItem;
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
    private final LocationContainer goldLocations = new LocationContainer();
    private final LocationContainer eliteGoldLocations = new LocationContainer();
    private final LocationContainer bossGoldLocations = new LocationContainer();
    private final LocationContainer potionLocations = new LocationContainer();
    private final CampfireLocations campfireLocations = new CampfireLocations();
    private final LocationContainerMap shopLocations = new LocationContainerMap();
    private int floorIndex = 0;
    private int modVersion;

    private final Map<Long, NetworkItem> scoutedLocations = new HashMap<>();

    private boolean cardDraw;
    private long currentOffset = -1;
    private final List<Long> extraOffsets = new ArrayList<>();

    public void reset() {
        bossRelicLocations.index = 0;
        cardDrawLocations.index = 0;
        rareDrawLocations.index = 0;
        relicLocations.index = 0;
        goldLocations.index = 0;
        eliteGoldLocations.index = 0;
        bossGoldLocations.index = 0;
        potionLocations.index = 0;
        floorIndex = 0;
        cardDraw = false;
    }

//    public void loadFromSave(int cdIndex, int rdIndex, int relicIndex, int bossRelicLocation, int floorIndex, boolean cardDraw)
    public void loadFromSave(LocationMemento memento)
    {
        cardDrawLocations.index = memento.getDrawIndex();
        rareDrawLocations.index = memento.getRareDrawIndex();
        relicLocations.index = memento.getRelicIndex();
        bossRelicLocations.index = memento.getBossRelicIndex();
        this.floorIndex = memento.getFloorIndex();
        this.cardDraw = memento.isCardDraw();
        goldLocations.index = memento.getCombatGoldIndex();
        eliteGoldLocations.index = memento.getEliteGoldIndex();
        bossGoldLocations.index = memento.getBossGoldIndex();
        potionLocations.index = memento.getPotionIndex();
    }

    public LocationMemento getMemento()
    {
        return new LocationMemento()
                .setCardDraw(cardDraw)
                .setFloorIndex(floorIndex)
                .setDrawIndex(cardDrawLocations.index)
                .setRareDrawIndex(rareDrawLocations.index)
                .setRelicIndex(relicLocations.index)
                .setBossRelicIndex(bossRelicLocations.index)
                .setCombatGoldIndex(goldLocations.index)
                .setEliteGoldIndex(eliteGoldLocations.index)
                .setBossGoldIndex(bossGoldLocations.index)
                .setPotionIndex(potionLocations.index);
    }

    public void initialize(long charOffset, int modVersion, List<Long> extras)
    {
        currentOffset = charOffset;
        extraOffsets.clear();
        extraOffsets.addAll(extras);
        reset();
        logger.info("Intializing LocationTracker with {} and extras {}", charOffset, extraOffsets);

        this.modVersion = modVersion;
        getCardDrawLocations().initialize(101L, CARD_DRAW_NUM, charOffset);
        getCampfireLocations().initialize(121L, 6L, charOffset);
        getCampfireLocations().loadFromNetwork();
        getRareDrawLocations().initialize(131L, 2L, charOffset);
        getRelicLocations().initialize(141L, 10L, charOffset);
        getBossRelicLocations().initialize(161L, 2L, charOffset);
        if(modVersion == 2) {
            goldLocations.initialize(57L, 18L, charOffset);
            eliteGoldLocations.initialize(76L, 7L, charOffset);
        } else if (modVersion > 3)
        {
            goldLocations.initialize(57L, 25L, charOffset);
            eliteGoldLocations.initialize(0L, 0L, charOffset);
        }
        bossGoldLocations.initialize(83L, 2L, charOffset);
        potionLocations.initialize(85L, 9L, charOffset);
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

    public NetworkItem sendGoldReward()
    {
        AbstractRoom currRoom = AbstractDungeon.getCurrRoom();
        if(currRoom instanceof TreasureRoom)
        {
            return null;
        }
        long locationId = -1;
        int index = -1;
        String fallbackName = "MissingNo";
        if(currRoom instanceof MonsterRoom) {
            if(currRoom instanceof MonsterRoomElite && modVersion == 2)
            {

                if(eliteGoldLocations.isExhausted())
                {
                    return null;
                }
                locationId = eliteGoldLocations.getNext();
                index = eliteGoldLocations.index;
                fallbackName = "Elite Gold ";
            }
            else if (currRoom instanceof MonsterRoomBoss)
            {

                if(bossGoldLocations.isExhausted())
                {
                    return null;
                }
                locationId = bossGoldLocations.getNext();
                index = bossGoldLocations.index;
                fallbackName = "Boss Gold ";
            }
            else
            {
                if(goldLocations.isExhausted())
                {
                    return null;
                }
                locationId = goldLocations.getNext();
                index = goldLocations.index;
                fallbackName = "Combat Gold ";
            }
        }
        if(locationId <= 0)
        {
            return null;
        }
        APContext.getContext().getClient().checkLocations(getLocationIDs(locationId));
        NetworkItem item = scoutedLocations.get(locationId);
        logger.info("Sent locationId: {}; got item: {}", locationId, item);
        if(item == null)
        {
            item = new NetworkItem();
            item.itemName = fallbackName + index;
            item.playerName = "";
        }
        return item;
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

    public NetworkItem sendPotion()
    {
        if(potionLocations.isExhausted())
        {
            return null;
        }

        long locationID = potionLocations.getNext();
        APContext.getContext().getClient().checkLocations(getLocationIDs(locationID));
        NetworkItem item = scoutedLocations.get(locationID);
        if(item == null)
        {
            NetworkItem networkItem = new NetworkItem();
            networkItem.itemName = "Potion Drop " + (9 - potionLocations.locations.size());
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
        CharacterConfig currentCharacter = apContext.getCharacterManager().getCurrentCharacterConfig();
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
        if(slotData.goldSanity != 0) {
            allLocations.addAll(goldLocations.locations);
            allLocations.addAll(eliteGoldLocations.locations);
            allLocations.addAll(bossGoldLocations.locations);
        }
        if(slotData.potionSanity != 0)
        {
            allLocations.addAll(potionLocations.locations);
        }
        long maxFloors = 51;
        if(currentCharacter.finalAct)
        {
            maxFloors += 4;
        }
        if(currentCharacter.ascension >= 20)
        {
            maxFloors += 1;
        }
        for(long i = 1; i <= maxFloors; i++)
        {
            allLocations.add(i + (200L * currentOffset));
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
        if(data.goldSanity != 0)
        {
            locations.addAll(goldLocations.locations);
            locations.addAll(eliteGoldLocations.locations);
            locations.addAll(bossGoldLocations.locations);
        }
        if(data.potionSanity != 0)
        {
            locations.addAll(potionLocations.locations);
        }
        logger.info("Scouting locations: {}", locations);
        APContext.getContext().getClient().scoutLocations(locations);
    }

    public void addToScoutedLocations(ArrayList<NetworkItem> networkItems) {
        for (NetworkItem item : networkItems) {
            logger.info("Got scount for location {} id {} has item {}", item.locationName, item.locationID, item.itemName);
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

    public static class LocationMemento
    {
        private int drawIndex;
        private int rareDrawIndex;
        private int relicIndex;
        private int bossRelicIndex;
        private int floorIndex;
        private int combatGoldIndex;
        private int eliteGoldIndex;
        private int bossGoldIndex;
        private int potionIndex;
        private boolean cardDraw;

        public int getDrawIndex() {
            return drawIndex;
        }

        public LocationMemento setDrawIndex(int drawIndex) {
            this.drawIndex = drawIndex;
            return this;
        }

        public int getRareDrawIndex() {
            return rareDrawIndex;
        }

        public LocationMemento setRareDrawIndex(int rareDrawIndex) {
            this.rareDrawIndex = rareDrawIndex;
            return this;
        }

        public int getRelicIndex() {
            return relicIndex;
        }

        public LocationMemento setRelicIndex(int relicIndex) {
            this.relicIndex = relicIndex;
            return this;
        }

        public int getBossRelicIndex() {
            return bossRelicIndex;
        }

        public LocationMemento setBossRelicIndex(int bossRelicIndex) {
            this.bossRelicIndex = bossRelicIndex;
            return this;
        }

        public int getFloorIndex() {
            return floorIndex;
        }

        public LocationMemento setFloorIndex(int floorIndex) {
            this.floorIndex = floorIndex;
            return this;
        }

        public int getCombatGoldIndex() {
            return combatGoldIndex;
        }

        public LocationMemento setCombatGoldIndex(int combatGoldIndex) {
            this.combatGoldIndex = combatGoldIndex;
            return this;
        }

        public int getEliteGoldIndex() {
            return eliteGoldIndex;
        }

        public LocationMemento setEliteGoldIndex(int eliteGoldIndex) {
            this.eliteGoldIndex = eliteGoldIndex;
            return this;
        }

        public int getBossGoldIndex() {
            return bossGoldIndex;
        }

        public LocationMemento setBossGoldIndex(int bossGoldIndex) {
            this.bossGoldIndex = bossGoldIndex;
            return this;
        }

        public boolean isCardDraw() {
            return cardDraw;
        }

        public LocationMemento setCardDraw(boolean cardDraw) {
            this.cardDraw = cardDraw;
            return this;
        }

        public int getPotionIndex() {
            return potionIndex;
        }

        public LocationMemento setPotionIndex(int potionIndex) {
            this.potionIndex = potionIndex;
            return this;
        }
    }

}
