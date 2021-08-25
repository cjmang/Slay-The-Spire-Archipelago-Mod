package ArchipelagoMW;

import archipelagoClient.com.google.gson.annotations.SerializedName;

public class SlotData {

    @SerializedName("seed")
    public String seed;

    @SerializedName("character")
    public int character;

    @SerializedName("games")
    public int games;

    @SerializedName("ascension")
    public int ascension;

    @SerializedName("heart_run")
    public int heartRun;
}
