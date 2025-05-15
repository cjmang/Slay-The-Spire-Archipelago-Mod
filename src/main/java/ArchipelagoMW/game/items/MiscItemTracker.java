package ArchipelagoMW.game.items;

import ArchipelagoMW.game.CharacterManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MiscItemTracker {
    private static final Set<Long> validIds = Arrays.stream(APItemID.values())
            .map(i -> i.value)
            .filter(v -> v >=7 && v <= 13)
            .collect(Collectors.toSet());

    private final Map<Long, Integer> itemCount = new ConcurrentHashMap<>();
    private final CharacterManager charManager;

    public MiscItemTracker(CharacterManager charManager) {
        this.charManager = charManager;
    }

    public void initialize(List<Long> itemIDs)
    {
        itemIDs.stream()
                .filter(charManager::isItemIDForCurrentCharacter)
                .filter(i -> validIds.contains(i%20L))
                .forEach(this::addItem);
    }

    public void addItem(long itemID)
    {
        long remainder = itemID % 20L;
        if(!validIds.contains(remainder))
        {
            return;
        }
        itemCount.merge(remainder, 1, Integer::sum);
    }

    public int getCount(Long itemID)
    {
        return itemCount.getOrDefault(itemID % 20L, 0);
    }

    public int getCount(APItemID itemID)
    {
        return getCount(itemID.value);
    }

    public int getRestCount()
    {
        return getCount(APItemID.PROGRESSIVE_REST);
    }

    public int getSmithCount()
    {
        return getCount(APItemID.PROGRESSIVE_SMITH);
    }

    public int getCardSlotCount()
    {
        return getCount(APItemID.CARD_SLOT);
    }

    public int getNeutralCardSlotCount()
    {
        return getCount(APItemID.NEUTRAL_CARD_SLOT);
    }

    public int getRelicSlotCount()
    {
        return getCount(APItemID.RELIC_SLOT);
    }

    public int getPotionSlotCount()
    {
        return getCount(APItemID.POTION_SLOT);
    }

    public int getCardRemoveCount()
    {
        return getCount(APItemID.CARD_REMOVE_SLOT);
    }

}
