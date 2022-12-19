package ArchipelagoMW;

import com.megacrit.cardcrawl.rewards.RewardItem;
import gg.archipelago.APClient.parts.NetworkItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationTracker {

    private static ArrayList<Long> cardDrawLocations;

    private static ArrayList<Long> relicLocations;

    private static ArrayList<Long> rareCardLocations;

    private static ArrayList<Long> bossRelicLocations;

    public static boolean cardDraw;

    public static HashMap<Long, NetworkItem> scoutedLocations = new HashMap<>();

    public static void reset() {
        cardDrawLocations = new ArrayList<Long>() {{
            add(19001L);
            add(19002L);
            add(19003L);
            add(19004L);
            add(19005L);
            add(19006L);
            add(19007L);
            add(19008L);
            add(19009L);
            add(19010L);
            add(19011L);
            add(19012L);
            add(19013L);
            add(19014L);
            add(19015L);
        }};

        relicLocations = new ArrayList<Long>() {{
            add(20001L);
            add(20002L);
            add(20003L);
            add(20004L);
            add(20005L);
            add(20006L);
            add(20007L);
            add(20008L);
            add(20009L);
            add(20010L);
        }};

         rareCardLocations = new ArrayList<Long>() {{
            add(21001L);
            add(21002L);
            add(21003L);
        }};

         bossRelicLocations = new ArrayList<Long>() {{
            add(22001L);
            add(22002L);
            add(22003L);
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
                long locationID = rareCardLocations.remove(0);
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
            long locationID = cardDrawLocations.remove(0);
            APClient.apClient.checkLocation(locationID);
            NetworkItem item = scoutedLocations.get(locationID);
            APClient.apClient.scoutLocations(new ArrayList<Long>() {{add(cardDrawLocations.get(0));}});
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

        long locationID = relicLocations.remove(0);
        APClient.apClient.checkLocation(locationID);
        NetworkItem item = scoutedLocations.get(locationID);
        APClient.apClient.scoutLocations(new ArrayList<Long>() {{add(relicLocations.get(0));}});
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
        long locationID = bossRelicLocations.remove(0);
        APClient.apClient.checkLocation(locationID);
        NetworkItem item = scoutedLocations.get(locationID);
        APClient.apClient.scoutLocations(new ArrayList<Long>() {{add(bossRelicLocations.get(0));}});
        if(item == null)
            return "Boss Relic " + (3 - bossRelicLocations.size());
        return item.itemName + " [] NL " + item.playerName + " [] NL Boss Relic";
    }

    public static void forfeit() {
        APClient ap = APClient.apClient;
        for (long location : cardDrawLocations) {
            ap.checkLocation(location);
        }
        for (long location : rareCardLocations) {
            ap.checkLocation(location);
        }
        for (long location : relicLocations) {
            ap.checkLocation(location);
        }
        for (long location : bossRelicLocations) {
            ap.checkLocation(location);
        }
    }

    public static void scoutFirstLocations() {
        ArrayList<Long> locations = new ArrayList<Long>() {{
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
