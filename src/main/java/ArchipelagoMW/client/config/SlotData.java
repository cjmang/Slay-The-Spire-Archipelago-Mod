package ArchipelagoMW.client.config;


import io.cjmang.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SlotData {

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

    @SerializedName("character_offset")
    public int character_offset = 0;

    @SerializedName("characters")
    public List<CharacterConfig> characters = new ArrayList<>();

}
