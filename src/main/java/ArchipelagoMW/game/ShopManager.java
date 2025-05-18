package ArchipelagoMW.game;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.ShopSanityConfig;
import ArchipelagoMW.client.config.SlotData;
import ArchipelagoMW.game.items.MiscItemTracker;
import ArchipelagoMW.game.locations.LocationTracker;
import ArchipelagoMW.game.locations.shop.APFakeCard;
import ArchipelagoMW.game.locations.shop.APFakePotion;
import ArchipelagoMW.game.locations.shop.APShopItem;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import dev.koifysh.archipelago.LocationManager;
import dev.koifysh.archipelago.network.client.CreateAsHint;
import dev.koifysh.archipelago.parts.NetworkItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ShopManager {
    private static final Logger logger = LogManager.getLogger(ShopManager.class);
    private static final int POTION_SLOTS = 3;
    private static final int RELIC_SLOTS = 3;
    private static final int CARD_SLOTS = 5;
    private static final int NEUTRAL_SLOTS = 2;

    private final MiscItemTracker itemTracker;
    private boolean sanityEnabled;
    private ShopSanityConfig config;
    private final LocationTracker locationTracker;
    private final LocationManager locationManager;
    private final CharacterManager characterManager;
    private ShopContext shopContext;

    public ShopManager(MiscItemTracker itemTracker, LocationManager locationManager, CharacterManager characterManager, LocationTracker locationTracker) {
        this.itemTracker = itemTracker;
        this.locationManager = locationManager;
        this.characterManager = characterManager;
        this.locationTracker = locationTracker;
    }

    public void initializeShop()
    {
        SlotData slotData = APContext.getContext().getSlotData();
        this.sanityEnabled = slotData.shopSanity != 0;
        this.config = slotData.shopSanityConfig;
        shopContext = new ShopContext(locationManager, AbstractDungeon.actNum, characterManager);
    }

    public int getTotalSlots()
    {
        if(!sanityEnabled)
        {
            return 0;
        }
        return config.cardSlots + config.potionSlots + config.relicSlots + config.neutralSlots + (config.cardRemove ? 3 : 0);
    }

    public int getAvailableCardSlots()
    {
        return sanityEnabled ? Math.min(CARD_SLOTS - config.cardSlots + itemTracker.getCardSlotCount(), CARD_SLOTS) : CARD_SLOTS;
    }

    public int getAvailableNeutralSlots()
    {
        return sanityEnabled ? Math.min(NEUTRAL_SLOTS - config.neutralSlots + itemTracker.getNeutralCardSlotCount(), NEUTRAL_SLOTS) : NEUTRAL_SLOTS;
    }

    public int getAvailableRelicSlots()
    {
        return sanityEnabled ? Math.min(RELIC_SLOTS - config.relicSlots + itemTracker.getRelicSlotCount(), RELIC_SLOTS) : RELIC_SLOTS;
    }

    public int getAvailablePotionSlots()
    {
        return sanityEnabled ? Math.min(POTION_SLOTS - config.potionSlots + itemTracker.getPotionSlotCount(), POTION_SLOTS) : POTION_SLOTS;
    }

    public boolean isCardRemoveAvailable()
    {
        logger.info("Sanity enabled: {}, card remove shuffled: {}, card remove count: {}, act: {}", sanityEnabled, config.cardRemove, itemTracker.getCardRemoveCount(), shopContext.act);
        // true && true && 0 < Math.min(1,3)
        return !(sanityEnabled && config.cardRemove && (itemTracker.getCardRemoveCount() < Math.min(shopContext.act,3)));
    }

    private <T> void mangleSlots(List<T> slots, int totalSlots, int currentSlots)
    {
        if(currentSlots == totalSlots)
        {
            return;
        }
        for(int i = 0; i < totalSlots - currentSlots; i++)
        {
            slots.remove(0);
        }
    }

    private <T> void addAPItem(List<T> slots, int emptySlots, BiFunction<Long, NetworkItem,T> createFunc)
    {
        ArrayList<Long> scoutMe = new ArrayList<>();
        if(emptySlots == 0 && shopContext.hasMore())
        {
            int max = slots.size();
            for(int i = 0; i < max && shopContext.hasMore(); i++) {
                slots.remove(0);
                Long locationId = shopContext.getNextLocation();
                NetworkItem item = locationTracker.getScoutedItemOrDefault(locationId);
                scoutMe.add(locationId);
                slots.add(createFunc.apply(locationId, item));
            }
        }
        else
        {
            for(int i = 0; i < emptySlots; i++)
            {
                Long locationId = shopContext.getNextLocation();
                if(locationId == null)
                {
                    return;
                }
                scoutMe.add(locationId);
                NetworkItem item = locationTracker.getScoutedItemOrDefault(locationId);
                slots.add(createFunc.apply(locationId, item));
            }
        }
        // TODO: pass in client
        APContext.getContext().getClient().scoutLocations(scoutMe, CreateAsHint.BROADCAST_NEW);
    }

    public void manglePotions(List<StorePotion> potions, ShopScreen shopScreen)
    {
        int availableSlots = getAvailablePotionSlots();
//        List<APFakePotion> fakePotionsToPlace = new ArrayList<>(3);
//        addAPItem(fakePotionsToPlace, availableSlots, APFakePotion::new);
//        for(int i = 0; i < fakePotionsToPlace.size(); i++)
//        {
//            potions.get(i).potion = fakePotionsToPlace.get(i);
//        }
//        if(fakePotionsToPlace.size() == POTION_SLOTS)
//        {
//            return;
//        }
//        for(int i = fakePotionsToPlace.size(); i < POTION_SLOTS - availableSlots; i++)
//        {
//            potions.remove(potions.size() - 1);
//        }
        logger.info("Available Potion Slots: {}", availableSlots);
        mangleSlots(potions, POTION_SLOTS, availableSlots);
    }

    public void mangleRelics(List<StoreRelic> relics, ShopScreen shopScreen)
    {
        int availableSlots = getAvailableRelicSlots();
        logger.info("Available Relic Slots: {}", availableSlots);
        mangleSlots(relics, RELIC_SLOTS, availableSlots);
    }

    public void mangleCards(List<AbstractCard> cards, ShopScreen shopScreen)
    {
        int availableSlots = getAvailableCardSlots();
        logger.info("Available Card Slots: {}", availableSlots);
        mangleSlots(cards, CARD_SLOTS, availableSlots);
        AbstractCard.CardColor color = characterManager.getCurrentCharacter().getCardColor();
        addAPItem(cards, CARD_SLOTS - availableSlots, (id,i) -> new APFakeCard(id,i,color));
    }

    public void mangleNeutralCards(List<AbstractCard> cards, ShopScreen shopScreen)
    {
        int availableSlots = getAvailableNeutralSlots();
        logger.info("Available Neutral Slots: {}", availableSlots);
        mangleSlots(cards, NEUTRAL_SLOTS, availableSlots);
        addAPItem(cards, NEUTRAL_SLOTS - availableSlots, (id,i) -> new APFakeCard(id,i, AbstractCard.CardColor.COLORLESS));
    }

    public void setPrice(AbstractCard card, float mult)
    {
        float price = 0;
        if(config.costs == 0)
        {
            card.price = 15;
            return;
        }
        switch(card.rarity)
        {
            default:
            case COMMON:
                price = 50;
                break;
            case UNCOMMON:
                price = 75;
                break;
            case RARE:
                price = 150;
                break;
        }
        price *= mult;
        if(config.costs == 1)
        {
            price /= 5;
        }
        else if(config.costs == 2)
        {
            price /= 2;
        }
        card.price = (int) price;
        for(AbstractRelic r : AbstractDungeon.player.relics)
        {
            r.onPreviewObtainCard(card);
        }
    }

    public int getPotionPrice(AbstractPotion.PotionRarity rarity)
    {
        float price = 0;
        if(config.costs == 0)
        {
            return 15;
        }
        switch(rarity)
        {
            default:
            case COMMON:
                price = 50;
                break;
            case UNCOMMON:
                price = 75;
                break;
            case RARE:
                price = 100;
                break;
        }
        if(config.costs == 1)
        {
            price /= 5;
        }
        else if(config.costs == 2)
        {
            price /= 2;
        }
        return (int)price;
    }

    private static class ShopContext
    {
        private static final Map<Integer, List<Long>> shopLocationsByAct = new HashMap<>();

        static {
            shopLocationsByAct.put(1, Arrays.asList(164L,165L,166L,167L,168L));
            shopLocationsByAct.put(2, Arrays.asList(169L,170L,171L,172L,173L));
            shopLocationsByAct.put(3, Arrays.asList(174L,175L,176L,177L,178L,179L));
        }

        private final int act;
        private int index;
        private final List<Long> missingLocations = new ArrayList<>();

        ShopContext(LocationManager locationManager, int act, CharacterManager charManager)
        {
            this.act = Math.min(act, 3);
            for(int i = 1; i <= act; i++) {
                shopLocationsByAct.get(this.act).stream()
                        .map(charManager::toCharacterLocationID)
                        .filter(locationManager.getMissingLocations()::contains)
                        .forEach(this.missingLocations::add);
            }
        }

        boolean hasMore()
        {
            return index < missingLocations.size();
        }

        Long getNextLocation()
        {
            if(index >= missingLocations.size())
            {
                return null;
            }
            return missingLocations.get(index++);
        }
    }

}
