package ArchipelagoMW.client.config;


import io.cjmang.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SlotData {
    public static int EXPECTED_MOD_VERSION = 2;

//    @SerializedName("character")
//    public List<String> character = new ArrayList<>();

    @SerializedName("ascension")
    public int ascension = 0;

    @SerializedName(value = "final_act", alternate = "heart_run")
    public int finalAct = 0;

    @SerializedName("downfall")
    public int downfall = 0;

    @SerializedName("death_link")
    public int deathLink = 0;

    @SerializedName("include_floor_checks")
    public int includeFloorChecks = 0;

    @SerializedName("chatty_mc")
    public int chattyMC = 1;

    @SerializedName("characters")
    public List<CharacterConfig> characters = new ArrayList<>();

    @SerializedName("campfire_sanity")
    public int campfireSanity = 0;

    @SerializedName("shop_sanity")
    public int shopSanity = 0;

    @SerializedName("mod_version")
    public int modVersion = 1;

    @SerializedName("gold_sanity")
    public int goldSanity = 0;

    @SerializedName("potion_sanity")
    public int potionSanity = 0;

    @SerializedName("num_chars_goal")
    public int numCharsGoal = 0;

    @SerializedName("shop_sanity_options")
    public ShopSanityConfig shopSanityConfig = new ShopSanityConfig();

}
