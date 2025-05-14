package ArchipelagoMW.game.items;

import ArchipelagoMW.game.CharacterManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MiscItemTracker {
    private static final Long REST_ID = 7L;
    private static final Long SMITH_ID = 8L;
    private final Map<Long, Integer> itemCount = new ConcurrentHashMap<>();
    private final CharacterManager charManager;

    public MiscItemTracker(CharacterManager charManager) {
        this.charManager = charManager;
    }


    public void initialize(List<Long> itemIDs)
    {
        itemIDs.stream()
                .filter(charManager::isItemIDForCurrentCharacter)
                .filter(i -> i%20L == REST_ID || i%20L == SMITH_ID)
                .forEach(this::addItem);
    }

    public void addItem(Long itemID)
    {
        if(itemID %20L !=REST_ID && itemID % 20L != SMITH_ID)
        {
            return;
        }
        itemCount.merge(itemID % 20L, 1, Integer::sum);
    }

    public int getCount(Long itemID)
    {
        return itemCount.getOrDefault(itemID, 0);
    }

    public int getRestCount()
    {
        return getCount(REST_ID);
    }

    public int getSmithCount()
    {
        return getCount(SMITH_ID);
    }

}
