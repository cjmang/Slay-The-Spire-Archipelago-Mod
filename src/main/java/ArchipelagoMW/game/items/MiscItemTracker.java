package ArchipelagoMW.game.items;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.game.CharacterManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
// TODO: refactor; I guess this needs to track whether the item in question has been given to the player
public class MiscItemTracker {
    private static final Set<Long> sanityIds = Arrays.stream(APItemID.values())
            .filter(v -> v.isSanity)
            .map(i -> i.value)
            .collect(Collectors.toSet());

    private final Map<Long, Integer> itemCount = new ConcurrentHashMap<>();
    private final CharacterManager charManager;

    public MiscItemTracker(CharacterManager charManager) {
        this.charManager = charManager;
    }

    public void initialize(List<Long> itemIDs)
    {
        itemCount.clear();
        itemIDs.stream()
                .filter(charManager::isItemIDForCurrentCharacter)
                .filter(i -> APItemID.isGeneric(i) || sanityIds.contains(i% charManager.getItemWindow()))
                .forEach(this::addSanityItem);
    }

    public void maybeAddDraw(long itemID)
    {
        long remainder = itemID % charManager.getItemWindow();
        if(remainder == APItemID.CARD_DRAW.value)
        {
            itemCount.merge(remainder, 1, Integer::sum);
        }
    }

    public void addSanityItem(long itemID) {
        long actualId = itemID;
        if (!APItemID.isGeneric(itemID)) {
            actualId = itemID % charManager.getItemWindow();
        }
        if(!sanityIds.contains(actualId))
        {
            return;
        }
        itemCount.merge(actualId, 1, Integer::sum);
    }


    public int getCount(Long itemID)
    {
        if(APItemID.isGeneric(itemID))
        {
            return itemCount.getOrDefault(itemID, 0);
        }
        return itemCount.getOrDefault(itemID % charManager.getItemWindow(), 0);
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

    public int getAscensionDownCount()
    {
        APClient.logger.info("Have {} ascension downs", getCount(APItemID.ASCENSION_DOWN));
        return getCount(APItemID.ASCENSION_DOWN);
    }
}
