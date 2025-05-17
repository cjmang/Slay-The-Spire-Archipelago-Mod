package ArchipelagoMW.client.config;

import io.cjmang.google.gson.annotations.SerializedName;

public class ShopSanityConfig {

    public static int FIXED_COST = 0;
    public static int SUPER_DISCOUNT_TIERED = 1;
    public static int DISCOUNTED_TIERED = 2;
    public static int TIERED_COST = 3;

    @SerializedName("card_slots")
    public int cardSlots = 0;
    @SerializedName("neutral_slots")
    public int neutralSlots = 0;
    @SerializedName("relic_slots")
    public int relicSlots = 0;
    @SerializedName("potion_slots")
    public int potionSlots = 0;
    @SerializedName("card_remove")
    public boolean cardRemove = false;
    @SerializedName("costs")
    public int costs = 2;
}
