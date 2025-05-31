package ArchipelagoMW;


import io.cjmang.google.gson.annotations.SerializedName;

public class SlotData {
    public static final int EXPECTED_MOD_VERSION = 1;

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

    @SerializedName("mod_version")
    public int modVersion = 1;

    @SerializedName("death_link")
    public int deathLink = 0;
}
