package ArchipelagoMW;

import com.megacrit.cardcrawl.rewards.RewardItem;
import gg.archipelago.APClient.parts.NetworkItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationTracker {

    private static ArrayList<Integer> cardDrawLocations;

    private static ArrayList<Integer> relicLocations;

    private static ArrayList<Integer> rareCardLocations;

    private static ArrayList<Integer> bossRelicLocations;

    public static boolean cardDraw;

    public static HashMap<Integer, NetworkItem> scoutedLocations = new HashMap<>();

    public static void reset() {
        cardDrawLocations = new ArrayList<Integer>() {{
            add(19001);
            add(19002);
            add(19003);
            add(19004);
            add(19005);
            add(19006);
            add(19007);
            add(19008);
            add(19009);
            add(19010);
            add(19011);
            add(19012);
            add(19013);
            add(19014);
            add(19015);
        }};

        relicLocations = new ArrayList<Integer>() {{
            add(20001);
            add(20002);
            add(20003);
            add(20004);
            add(20005);
            add(20006);
            add(20007);
            add(20008);
            add(20009);
            add(20010);
        }};

         rareCardLocations = new ArrayList<Integer>() {{
            add(21001);
            add(21002);
            add(21003);
        }};

         bossRelicLocations = new ArrayList<Integer>() {{
            add(22001);
            add(22002);
            add(22003);
        }};
    }

    /**
     * @return true if this card draw was sent to AP,
     * false if you should keep this card draw locally.
     */
    static public String sendCardDraw(RewardItem reward) {
        try {
            Field isBoss = RewardItem.class.getDeclaredField("isBoss");
            isBoss.setAccessible(true);
            if ((boolean)isBoss.get(reward)) {
                if(rareCardLocations.isEmpty())
                    return "";
                int locationID = rareCardLocations.remove(0);
                APClient.apClient.checkLocation(locationID);
                NetworkItem item = scoutedLocations.get(locationID);
                if(item == null)
                    return "Rare Card Draw "+ (3 - rareCardLocations.size());
                return item.itemName + " [] NL " + item.playerName + " [] NL Rare Card Draw";
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        cardDraw = !cardDraw;
        if(cardDraw) {
            if(cardDrawLocations.isEmpty())
                return "";
            int locationID = cardDrawLocations.remove(0);
            APClient.apClient.checkLocation(locationID);
            NetworkItem item = scoutedLocations.get(locationID);
            APClient.apClient.scoutLocations(new ArrayList<Integer>() {{add(cardDrawLocations.get(0));}});
            if(item == null)
                return "Card Draw "+ (15 - cardDrawLocations.size());
            return item.itemName + " [] NL " + item.playerName + " [] NL Card Draw";
        }
        return "";
    }

    /**
     * sends the next relic location to AP
     */
    static public String sendRelic() {
        if(relicLocations.isEmpty())
            return "";

        int locationID = relicLocations.remove(0);
        APClient.apClient.checkLocation(locationID);
        NetworkItem item = scoutedLocations.get(locationID);
        APClient.apClient.scoutLocations(new ArrayList<Integer>() {{add(relicLocations.get(0));}});
        if(item == null)
            return "Relic " + (10 - relicLocations.size());
        return item.itemName + " [] NL " + item.playerName + " [] NL Relic";
    }

    /**
     * sends the next boss relic location to AP
     */
    static public String sendBossRelic() {
        if(bossRelicLocations.isEmpty())
            return "";
        int locationID = bossRelicLocations.remove(0);
        APClient.apClient.checkLocation(locationID);
        NetworkItem item = scoutedLocations.get(locationID);
        APClient.apClient.scoutLocations(new ArrayList<Integer>() {{add(bossRelicLocations.get(0));}});
        if(item == null)
            return "Boss Relic " + (3 - bossRelicLocations.size());
        return item.itemName + " [] NL " + item.playerName + " [] NL Boss Relic";
    }

    public static void forfeit() {
        APClient ap = APClient.apClient;
        for (Integer location : cardDrawLocations) {
            ap.checkLocation(location);
        }
        for (Integer location : rareCardLocations) {
            ap.checkLocation(location);
        }
        for (Integer location : relicLocations) {
            ap.checkLocation(location);
        }
        for (Integer location : bossRelicLocations) {
            ap.checkLocation(location);
        }
    }

    public static void scoutFirstLocations() {
        ArrayList<Integer> locations = new ArrayList<Integer>() {{
            add(cardDrawLocations.get(0));
            add(relicLocations.get(0));
            add(rareCardLocations.get(0));
            add(bossRelicLocations.get(0));
        }};
        APClient.apClient.scoutLocations(locations);
    }

    public static void addToScoutedLocations(ArrayList<NetworkItem> networkItems) {
        for (NetworkItem item : networkItems) {
            scoutedLocations.put(item.locationID, item);
        }

    }
}
