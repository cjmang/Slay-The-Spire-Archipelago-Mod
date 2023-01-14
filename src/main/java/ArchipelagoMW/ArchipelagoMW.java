package ArchipelagoMW;

import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.ui.RewardMenu.BossRelicRewardScreen;
import ArchipelagoMW.ui.topPannel.ArchipelagoIcon;
import ArchipelagoMW.util.APRewardSave;
import basemod.BaseMod;
import basemod.ModLabel;
import basemod.ModPanel;
import basemod.abstracts.CustomSavableRaw;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.RewardSave;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.stream.Location;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;


@SpireInitializer
public class ArchipelagoMW implements
        EditStringsSubscriber,
        PostInitializeSubscriber {

    public static final Logger logger = LogManager.getLogger(ArchipelagoMW.class.getName());
    private static final String modID = "ArchipelagoMW";
    private static final String MODNAME = "Archipelago Multi-World";
    private static final String AUTHOR = "Kono Tyran & Mavelovent";
    private static final String DESCRIPTION = "An Archipelago multiworld mod.";

    public static final String BADGE_IMAGE = "ArchipelagoMWResources/images/Badge.png";
    public static BossRelicRewardScreen bossRelicRewardScreen;

    // Archipelago Client Varaiables
    public static String address;
    public static String slotName;
    public static String password;


    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/images/ui/" + resourcePath;
    }

    public ArchipelagoMW() {
        logger.info("Subscribe to BaseMod hooks");

        BaseMod.subscribe(this);

        logger.info("Done subscribing");
    }

    public static String getModID() {
        return modID;
    }

    public static void initialize() {
        logger.info("========================= Initializing Archipelago Multi-World. =========================");
        new ArchipelagoMW();
        logger.info("========================= Archipelago Multi-World Initialized. =========================");
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


        // Create the Mod Menu
        ModPanel settingsPanel = new ModPanel();

        int configPos = 800;
        int configStep = 40;
        configPos -= 90;
        ModLabel validLabel = new ModLabel("Valid Characters:", 350.0F, (float) configPos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (label) -> {
        });
        settingsPanel.addUIElement(validLabel);

        String[] titles = BaseMod.getModdedCharacters().stream().map(p -> p.title).toArray(String[]::new);

        int chunkSize = 4;
        int remainder = titles.length % chunkSize;
        int chunks = titles.length / chunkSize + (remainder > 1 ? 1 : 0);
        for (int i = 0; i <= chunks; i++) {
            configPos -= configStep;
            String[] line;
            if (i == chunks && remainder > 0) {
                line = Arrays.copyOfRange(titles, chunks * chunkSize, titles.length);
            } else {
                line = Arrays.copyOfRange(titles, i * chunkSize, i * chunkSize + chunkSize);
            }
            ModLabel lineLabel = new ModLabel("\"" + String.join("\", \"", line) + "\"", 350.0F, (float) configPos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (label) -> {
            });
            settingsPanel.addUIElement(lineLabel);
        }


        BaseMod.registerModBadge(APTextures.AP_BADGE, MODNAME, AUTHOR, DESCRIPTION, settingsPanel);
        BaseMod.addTopPanelItem(new ArchipelagoIcon());

        Type stringArrayList = new TypeToken<ArrayList<String>>() {}.getType();
        Type rewardSaveList = new TypeToken<ArrayList<APRewardSave>>() {}.getType();

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

                save.add("rewards", gson.toJsonTree(rewards,rewardSaveList));


                save.add("rewards_remaining", new JsonPrimitive(ArchipelagoRewardScreen.rewardsQueued));
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
                            for (String cardID : reward.cardIDs) {
                                item.cards.add(CardLibrary.getCard(cardID));
                            }
                            rewards.add(item);
                            break;
                    }

                    ArchipelagoRewardScreen.rewards = rewards;
                    ArchipelagoRewardScreen.rewardsQueued = save.get("rewards_remaining").getAsInt();
                    LocationTracker.cardDrawIndex = save.get("card_draw_index").getAsInt();
                    LocationTracker.rareCardIndex = save.get("rare_card_draw_index").getAsInt();
                    LocationTracker.relicIndex= save.get("relic_index").getAsInt();
                }
            }
        });

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
