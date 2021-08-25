package ArchipelagoMW;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class LocationTracker {

    private static ArrayList<Integer> cardDrawLocations;

    private static ArrayList<Integer> relicLocations;

    private static ArrayList<Integer> rareCardLocations;

    private static ArrayList<Integer> bossRelicLocations;

    public static boolean cardDraw;

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
                APClient.apClient.checkLocation(rareCardLocations.remove(0));
                return "Rare Card Draw " + (3 - rareCardLocations.size());
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        cardDraw = !cardDraw;
        if(cardDraw) {
            APClient.apClient.checkLocation(cardDrawLocations.remove(0));
            return "Card Draw " + (15 - cardDrawLocations.size());
        }
        return "";
    }

    /**
     * sends the next relic location to AP
     */
    static public String sendRelic() {
        APClient.apClient.checkLocation(relicLocations.remove(0));
        return "Relic " + (10 - relicLocations.size());
    }

    /**
     * sends the next boss relic location to AP
     */
    static public String sendBossRelic() {
        APClient.apClient.checkLocation(bossRelicLocations.remove(0));
        return "Boss Relic " + (3 - bossRelicLocations.size());
    }

}
