package ArchipelagoMW.game;

import ArchipelagoMW.client.config.ShopSanityConfig;
import ArchipelagoMW.client.config.SlotData;
import ArchipelagoMW.game.items.MiscItemTracker;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;

import java.util.List;

public class ShopManager {
    private static final int POTION_SLOTS = 3;
    private static final int RELIC_SLOTS = 3;
    private static final int CARD_SLOTS = 5;
    private static final int NEUTRAL_SLOTS = 2;

    private final MiscItemTracker itemTracker;
    private final boolean sanityEnabled;
    private final ShopSanityConfig config;

    public ShopManager(MiscItemTracker itemTracker, SlotData slotData) {
        this.itemTracker = itemTracker;
        this.sanityEnabled = slotData.shopSanity != 0;
        this.config = slotData.shopSanityConfig;
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
        return !sanityEnabled || !config.cardRemove || itemTracker.getCardRemoveCount() > 0;
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

    public void manglePotions(List<StorePotion> potions, ShopScreen shopScreen)
    {
        int availableSlots = getAvailablePotionSlots();
        mangleSlots(potions, POTION_SLOTS, availableSlots);
    }

    public void mangleRelics(List<StoreRelic> relics, ShopScreen shopScreen)
    {
        int availableSlots = getAvailableRelicSlots();
        mangleSlots(relics, RELIC_SLOTS, availableSlots);
    }

    public void mangleCards(List<AbstractCard> cards, ShopScreen shopScreen)
    {
        int availableSlots = getAvailableCardSlots();
        mangleSlots(cards, CARD_SLOTS, availableSlots);
    }

    public void mangleNeutralCards(List<AbstractCard> cards, ShopScreen shopScreen)
    {
        int availableSlots = getAvailableNeutralSlots();
        mangleSlots(cards, NEUTRAL_SLOTS, availableSlots);
    }
}
