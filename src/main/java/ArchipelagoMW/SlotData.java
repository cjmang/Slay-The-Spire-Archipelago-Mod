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

    @SerializedName("heart_run")
    public int heartRun = 0;

    @SerializedName("downfall")
    public int downfall = 0;
}
