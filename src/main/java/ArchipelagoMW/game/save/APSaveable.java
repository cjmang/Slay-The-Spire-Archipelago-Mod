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
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.RewardSave;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class APSaveable implements CustomSavableRaw {
    private static final Logger logger = LogManager.getLogger(APSaveable.class);

    private static final Type REWARD_SAVE_LIST_TYPE = new TypeToken<ArrayList<APRewardSave>>() {
    }.getType();

    @Override
    public JsonElement onSaveRaw() {
        LocationTracker locationTracker = APContext.getContext().getLocationTracker();
        Gson gson = new Gson();
        JsonObject save = new JsonObject();

        ArrayList<RewardSave> rewards = new ArrayList<>();
        for (RewardItem rewardItem : ArchipelagoRewardScreen.rewards) {
            if(rewardItem.type == RewardItemPatch.RewardType.BOSS_RELIC)
            {
                rewards.add(new APRewardSave(RewardItemPatch.CustomFields.bossRelics.get(rewardItem), rewardItem.type.toString()));
                continue;
            }
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
                    break;
            }
        }

        save.add("rewards", gson.toJsonTree(rewards, REWARD_SAVE_LIST_TYPE));
        save.add("rewards_remaining", new JsonPrimitive(ArchipelagoRewardScreen.rewardsQueued));
        save.add("received_index", new JsonPrimitive(ArchipelagoRewardScreen.getReceivedItemsIndex()));

        LocationTracker.LocationMemento memento = locationTracker.getMemento();

        save.add("card_draw_index", new JsonPrimitive(memento.getDrawIndex()));
        save.add("card_draw_toggle", new JsonPrimitive(memento.isCardDraw()));
        save.add("rare_card_draw_index", new JsonPrimitive(memento.getRareDrawIndex()));
        save.add("relic_index", new JsonPrimitive(memento.getRelicIndex()));
        save.add("boss_relic_index", new JsonPrimitive(memento.getBossRelicIndex()));
        save.add("floor_index", new JsonPrimitive(memento.getFloorIndex()));
        save.add("combat_gold_index", new JsonPrimitive(memento.getCombatGoldIndex()));
        save.add("elite_gold_index", new JsonPrimitive(memento.getEliteGoldIndex()));
        save.add("boss_gold_index", new JsonPrimitive(memento.getBossGoldIndex()));
        save.add("potion_index", new JsonPrimitive(memento.getPotionIndex()));
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
                {
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
                case "BOSS_RELIC":
                {
                    RewardItem item = new RewardItem(1);
                    item.goldAmt = 0;
                    item.type = RewardItemPatch.RewardType.BOSS_RELIC;
                    ArrayList<AbstractRelic> bossRelics = new ArrayList<>();
                    reward.relicIds.stream().map(r -> RelicLibrary.getRelic(r).makeCopy()).forEach(bossRelics::add);
                    RewardItemPatch.CustomFields.bossRelics.set(reward, bossRelics);
                    RewardItemPatch.CustomFields.apReward.set(item, true);
                    rewards.add(item);
                    break;
                }
            }
        }

        ArchipelagoRewardScreen.rewards = rewards;
        ArchipelagoRewardScreen.rewardsQueued = save.get("rewards_remaining").getAsInt();
        ArchipelagoRewardScreen.setReceivedItemsIndex(save.get("received_index").getAsInt());
        LocationTracker.LocationMemento memento = new LocationTracker.LocationMemento();
        if(save.has("combat_gold_index"))
        {
            memento.setCombatGoldIndex(save.get("combat_gold_index").getAsInt());
        }
        if(save.has("elite_gold_index"))
        {
            memento.setEliteGoldIndex(save.get("elite_gold_index").getAsInt());
        }
        if(save.has("boss_gold_index"))
        {
            memento.setBossGoldIndex(save.get("boss_gold_index").getAsInt());
        }
        if(save.has("potion_sanity"))
        {
            memento.setPotionIndex(save.get("potion_index").getAsInt());
        }
        locationTracker.loadFromSave(
                memento
                        .setDrawIndex(save.get("card_draw_index").getAsInt())
                        .setRareDrawIndex(save.get("rare_card_draw_index").getAsInt())
                        .setRelicIndex(save.get("relic_index").getAsInt())
                        .setBossRelicIndex(save.get("boss_relic_index").getAsInt())
                        .setFloorIndex(save.get("floor_index").getAsInt())
                        .setCardDraw(save.get("card_draw_toggle").getAsBoolean())
        );
    }
}
