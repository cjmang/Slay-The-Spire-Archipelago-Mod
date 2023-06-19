package ArchipelagoMW;

import ArchipelagoMW.patches.RewardItemPatch;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.RewardMenu.BossRelicRewardScreen;
import ArchipelagoMW.ui.hud.SideBar;
import ArchipelagoMW.ui.topPannel.ArchipelagoIcon;
import ArchipelagoMW.util.APRewardSave;
import basemod.BaseMod;
import basemod.abstracts.CustomSavableRaw;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.RewardSave;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;


@SpireInitializer
public class Archipelago implements
        EditStringsSubscriber,
        PostInitializeSubscriber {

    public static final Logger logger = LogManager.getLogger(Archipelago.class.getName());
    public static final String modID = "ArchipelagoMW";
    public static final String MODNAME = "Archipelago Multi-World";
    public static final String AUTHOR = "Kono Tyran & Mavelovent";
    public static final String DESCRIPTION = "An Archipelago multiworld mod.";

    public static BossRelicRewardScreen bossRelicRewardScreen;

    // Archipelago Client Varaiables
    public static String address;
    public static String slotName;
    public static String password;

    public static SideBar sideBar;


    public Archipelago() {
        logger.info("Subscribe to BaseMod hooks");
        BaseMod.subscribe(this);
        logger.info("Done subscribing");

        APSettings.loadSettings();
        Runtime.getRuntime().addShutdownHook(new Thread(TeamManager::leaveTeam));
    }

    public static String getModID() {
        return modID;
    }

    @SuppressWarnings("unused")
    public static void initialize() {
        logger.info("========================= Initializing Archipelago Multi-World Version 1.11 =========================");
        new Archipelago();
        logger.info("=========================  Archipelago Multi-World Initialized. =========================");
    }

    public static void setConnectionInfo(String addressField, String slotNameField, String passwordField) {
        address = addressField;
        slotName = slotNameField;
        password = passwordField;
    }

    @Override
    public void receivePostInitialize() {
        logger.info("Loading badge image and mod options");

        //initalize textures
        APTextures.initialize();

        //initialize Mod Menu and settings.
        APSettings.initialize();

        BaseMod.addTopPanelItem(new ArchipelagoIcon());
        //BaseMod.addTopPanelItem(new TestButton());

        Type rewardSaveList = new TypeToken<ArrayList<APRewardSave>>() {
        }.getType();

        BaseMod.addCustomScreen(new ArchipelagoRewardScreen());
        BaseMod.addSaveField("ap_rewards", new CustomSavableRaw() {
            @Override
            public JsonElement onSaveRaw() {
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

                save.add("rewards", gson.toJsonTree(rewards, rewardSaveList));


                save.add("rewards_remaining", new JsonPrimitive(ArchipelagoRewardScreen.rewardsQueued));
                save.add("received_index", new JsonPrimitive(ArchipelagoRewardScreen.receivedItemsIndex));
                save.add("card_draw_index", new JsonPrimitive(LocationTracker.cardDrawIndex));
                save.add("rare_card_draw_index", new JsonPrimitive(LocationTracker.rareCardIndex));
                save.add("relic_index", new JsonPrimitive(LocationTracker.relicIndex));

                return save;
            }

            @Override
            public void onLoadRaw(JsonElement jsonElement) {
                Gson gson = new Gson();

                JsonObject save = jsonElement.getAsJsonObject();

                ArrayList<APRewardSave> rewardSave = gson.fromJson(save.get("rewards"), rewardSaveList);
                ArrayList<RewardItem> rewards = new ArrayList<>();
                for (APRewardSave reward : rewardSave) {
                    switch (reward.type) {
                        case "GOLD":
                            rewards.add(new RewardItem(reward.amount));
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
                ArchipelagoRewardScreen.receivedItemsIndex = save.get("received_index").getAsInt();
                LocationTracker.cardDrawIndex = save.get("card_draw_index").getAsInt();
                LocationTracker.rareCardIndex = save.get("rare_card_draw_index").getAsInt();
                LocationTracker.relicIndex = save.get("relic_index").getAsInt();
            }
        });

        sideBar = new SideBar();
        BaseMod.removeRelic(RelicLibrary.getRelic("Calling Bell"));
        logger.info("Done loading badge Image and mod options");
    }

    @Override
    public void receiveEditStrings() {
        logger.info("Beginning to edit strings for mod with ID: " + getModID());

        // UIStrings
        BaseMod.loadCustomStringsFile(UIStrings.class,
                getModID() + "Resources/localization/eng/ArchipelagoMW-UI-Strings.json");

        logger.info("Done editing strings");
    }

    public static String makeID(String idText) {
        return getModID() + ":" + idText;
    }
}
