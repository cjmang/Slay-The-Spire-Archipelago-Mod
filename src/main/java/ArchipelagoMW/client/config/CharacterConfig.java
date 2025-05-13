package ArchipelagoMW.client.config;


import io.cjmang.google.gson.annotations.SerializedName;

public class CharacterConfig {

    @SerializedName("name")
    public String name;
    @SerializedName("option_name")
    public String optionName;
    @SerializedName("char_offset")
    public int charOffset;
    @SerializedName("official_name")
    public String officialName;
    @SerializedName("mod_num")
    public int modNum;
    @SerializedName("seed")
    public String seed;
    @SerializedName("ascension")
    public int ascension;
    @SerializedName("final_act")
    public boolean finalAct;
    @SerializedName("downfall")
    public boolean downfall;

    @Override
    public String toString() {
        return "CharacterConfig{" +
                "name='" + name + '\'' +
                ", optionName='" + optionName + '\'' +
                ", charOffset=" + charOffset +
                ", officialName='" + officialName + '\'' +
                ", modNum=" + modNum +
                ", seed='" + seed + '\'' +
                ", ascension=" + ascension +
                ", finalAct=" + finalAct +
                ", downfall=" + downfall +
                '}';
    }
}

