package ArchipelagoMW;

import archipelagoClient.com.google.gson.annotations.SerializedName;

public class SlotData {

    @SerializedName("seed")
    public String seed;

    @SerializedName("character")
    public String character = "The Ironclad";

    @SerializedName("games")
    public int games;

    @SerializedName("ascension")
    public int ascension = 0;

    @SerializedName(value = "final_act", alternate = "heart_run")
    public int finalAct = 0;

    @SerializedName("downfall")
    public int downfall = 0;

    @SerializedName("death_link")
    public int deathLink = 0;
}
