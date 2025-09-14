package ArchipelagoMW.game.items;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.game.items.patches.RewardItemPatch;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.BloodPotion;
import com.megacrit.cardcrawl.relics.Strawberry;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.ui.panels.TopPanel;

import java.util.*;

public class AscensionManager {

    private final CharacterManager characterManager;
    private final MiscItemTracker itemTracker;

    private int currentAscension;
    private int ascensionItemsHandled = 0;


    public AscensionManager(APContext ctx)
    {
        characterManager = ctx.getCharacterManager();
        itemTracker = ctx.getItemTracker();
    }

    public void initializeRunStart()
    {
        APClient.logger.info("Initializing Ascension Manager");
        reset();
        int ascension = characterManager.getCurrentCharacterConfig().ascension;
        ascensionItemsHandled = itemTracker.getAscensionDownCount();
        ascension -= ascensionItemsHandled;
        if(ascension < 0)
        {
            ascension = 0;
        }
        currentAscension = ascension;
        AbstractDungeon.ascensionLevel = ascension;
        APClient.logger.info("Starting at ascension {}", ascension);
    }

    public void initialize(JsonObject saveData)
    {
        if(saveData != null)
        {
            currentAscension =  saveData.get("ascension").getAsInt();
            ascensionItemsHandled =  saveData.get("ascensions_handled").getAsInt();
        }
    }

    public Map<String, Object> toSaveData()
    {
        Map<String, Object> ret = new HashMap<>();
        ret.put("ascension", currentAscension);
        ret.put("ascensions_handled", ascensionItemsHandled);
        return ret;
    }

    public void reset()
    {
        currentAscension = 0;
        ascensionItemsHandled = 0;
    }

    public List<RewardItem> checkAndDecrementAscensions()
    {
        if(currentAscension <= 0)
        {
            return Collections.emptyList();
        }
        int ascensionDowns = itemTracker.getAscensionDownCount();
//        APClient.logger.info("Have {} ascension downs; handled {} of them", ascensionDowns, ascensionItemsHandled);
        if(ascensionDowns <= ascensionItemsHandled)
        {
            return Collections.emptyList();
        }
        int decrement = ascensionDowns - ascensionItemsHandled;
//        APClient.logger.info("Decrementing ascension {}", decrement);
        List<RewardItem> ret = new ArrayList<>();
        RewardItem item = null;
        for(int i = 0; i < decrement && currentAscension > 0; i++, currentAscension--, ascensionItemsHandled++) {
            switch (currentAscension) {
                case 5:
                    item = new RewardItem(new BloodPotion());
                    RewardItemPatch.CustomFields.apReward.set(item, true);
                    ret.add(item);
                case 10:
                    AbstractDungeon.player.masterDeck.removeCard("AscendersBane");
                    break;
                case 13:
                    item = new RewardItem(50);
                    ret.add(item);
                    break;
                case 14:
                    item = new RewardItem(new Strawberry());
                    RewardItemPatch.CustomFields.apReward.set(item, true);
                    ret.add(item);
                    break;
                default:
                    // fall through
            }
        }

        APClient.logger.info("Dropping ascension from {} to {}", AbstractDungeon.ascensionLevel, currentAscension);
        if(AbstractDungeon.ascensionLevel != currentAscension) {
            AbstractDungeon.ascensionLevel = currentAscension;
            TopPanel topPanel = AbstractDungeon.topPanel;
            if(topPanel != null)
            {
                topPanel.setupAscensionMode();
            }
        }

        return ret;
    }

}
