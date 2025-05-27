package ArchipelagoMW.game.save;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.locations.LocationTracker;
import ArchipelagoMW.game.items.patches.RewardItemPatch;
import ArchipelagoMW.game.items.ui.ArchipelagoRewardScreen;
import basemod.abstracts.CustomSavableRaw;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.RewardSave;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class APSaveable implements CustomSavableRaw {
    private static final Logger logger = LogManager.getLogger(APSaveable.class);

    private static final Type REWARD_SAVE_LIST_TYPE = new TypeToken<ArrayList<APRewardSave>>() {}.getType();
    @Override
    public JsonElement onSaveRaw() {
        LocationTracker locationTracker = APContext.getContext().getLocationTracker();
        Gson gson = new Gson();
        JsonObject save = new JsonObject();

        ArrayList<RewardSave> rewards = new ArrayList<>();
        for (RewardItem rewardItem : ArchipelagoRewardScreen.rewards) {
            switch (rewardItem.type) {
                case SAPPHIRE_KEY:
                case EMERALD_KEY:
                case CARD:
                    rewards.add(new APRewardSave(rewardItem.type.toString(), rewardItem.cards));
                    break;
                case GOLD:
                    rewards.add(new APRewardSave(rewardItem.type.toString(), null, rewardItem.goldAmt, rewardItem.bonusGold));
                    break;
                case POTION:
                    rewards.add(new APRewardSave(rewardItem.type.toString(), rewardItem.potion.ID));
                    break;
                case RELIC:
                    rewards.add(new APRewardSave(rewardItem.type.toString(), rewardItem.relic.relicId));
                    break;
                case STOLEN_GOLD:
                    rewards.add(new RewardSave(rewardItem.type.toString(), null, rewardItem.goldAmt, 0));
            }
        }

        save.add("rewards", gson.toJsonTree(rewards, REWARD_SAVE_LIST_TYPE));


        save.add("rewards_remaining", new JsonPrimitive(ArchipelagoRewardScreen.rewardsQueued));
        save.add("received_index", new JsonPrimitive(ArchipelagoRewardScreen.getReceivedItemsIndex()));
        save.add("card_draw_index", new JsonPrimitive(locationTracker.getCardDrawLocations().getIndex()));
        save.add("card_draw_toggle", new JsonPrimitive(locationTracker.getCardDrawToggle()));
        save.add("rare_card_draw_index", new JsonPrimitive(locationTracker.getRareDrawLocations().getIndex()));
        save.add("relic_index", new JsonPrimitive(locationTracker.getRelicLocations().getIndex()));
        save.add("character", new JsonPrimitive(APContext.getContext().getCharacterManager().getCurrentCharacter().chosenClass.name()));

        return save;
    }

    @Override
    public void onLoadRaw(JsonElement jsonElement) {
        logger.info("Loading save data");
        LocationTracker locationTracker = APContext.getContext().getLocationTracker();
        Gson gson = new Gson();

        JsonObject save = jsonElement.getAsJsonObject();

        ArrayList<APRewardSave> rewardSave = gson.fromJson(save.get("rewards"), REWARD_SAVE_LIST_TYPE);
        ArrayList<RewardItem> rewards = new ArrayList<>();
        for (APRewardSave reward : rewardSave) {
            switch (reward.type) {
                case "GOLD":
                    // TODO: cleanup
                    RewardItem gold = new RewardItem(null, RewardItem.RewardType.GOLD);
                    gold.goldAmt = reward.amount;
                    gold.text = gold.goldAmt + RewardItem.TEXT[1];
                    rewards.add(gold);
                    break;
                case "RELIC":
                    rewards.add(new RewardItem(RelicLibrary.getRelic(reward.id).makeCopy()));
                    break;
                case "POTION":
                    rewards.add(new RewardItem(PotionHelper.getPotion(reward.id)));
                    break;
                case "CARD":
                    RewardItem item = new RewardItem(0, true);
                    item.type = RewardItem.RewardType.CARD;
                    item.text = RewardItem.TEXT[2];
                    item.cards = new ArrayList<>();
                    RewardItemPatch.CustomFields.apReward.set(item, true);
                    for (String cardID : reward.cardIDs) {
                        item.cards.add(CardLibrary.getCard(cardID).makeCopy());
                    }
                    rewards.add(item);
                    break;
            }
        }

        ArchipelagoRewardScreen.rewards = rewards;
        ArchipelagoRewardScreen.rewardsQueued = save.get("rewards_remaining").getAsInt();
        ArchipelagoRewardScreen.setReceivedItemsIndex(save.get("received_index").getAsInt());
//        logger.info("ReceivedItems index from save: {}", ArchipelagoRewardScreen.getReceivedItemsIndex());
        locationTracker.loadFromSave(
                save.get("card_draw_index").getAsInt(),
                save.get("rare_card_draw_index").getAsInt(),
                save.get("relic_index").getAsInt(),
                save.get("card_draw_toggle").getAsBoolean()
        );
        // I don't think this code is needed, since we load the save after the character is selected now.
//        if(!APContext.getContext().getCharacterManager().selectCharacter(save.get("character").getAsString()))
//        {
//            throw new RuntimeException("Could not select character from save file " + save.get("character").getAsString());
//        }
    }
}
